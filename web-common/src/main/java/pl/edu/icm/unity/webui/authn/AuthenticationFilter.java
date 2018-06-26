/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webui.authn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;

import com.vaadin.shared.ApplicationConstants;

import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.engine.api.authn.AuthenticationException;
import pl.edu.icm.unity.engine.api.authn.LoginSession;
import pl.edu.icm.unity.engine.api.authn.UnsuccessfulAuthenticationCounter;
import pl.edu.icm.unity.engine.api.session.LoginToHttpSessionBinder;
import pl.edu.icm.unity.engine.api.session.SessionManagement;
import pl.edu.icm.unity.engine.api.utils.HiddenResourcesFilter;
import pl.edu.icm.unity.types.authn.AuthenticationRealm;
import pl.edu.icm.unity.types.authn.RememberMePolicy;
import pl.edu.icm.unity.webui.CookieHelper;
import pl.edu.icm.unity.webui.authn.RememberMeHelper.RememberMeCookie;

/**
 * Servlet filter forwarding unauthenticated requests to the protected authentication servlet.
 * @author K. Benedyczak
 */
public class AuthenticationFilter implements Filter
{
	private static final Logger log = Log.getLogger(Log.U_SERVER_WEB, AuthenticationFilter.class);

	private List<String> protectedServletPaths;
	private String authnServletPath;
	private final String sessionCookie;
	private UnsuccessfulAuthenticationCounter dosGauard;
	private SessionManagement sessionMan;
	private LoginToHttpSessionBinder sessionBinder;
	private RememberMeHelper rememberMeHelper;
	private AuthenticationRealm realm;
	
	public AuthenticationFilter(List<String> protectedServletPaths, String authnServletPath, 
			AuthenticationRealm realm,
			SessionManagement sessionMan, LoginToHttpSessionBinder sessionBinder, RememberMeHelper rememberMeHelper)
	{
		this.protectedServletPaths = new ArrayList<>(protectedServletPaths);
		this.authnServletPath = authnServletPath;
		dosGauard = new UnsuccessfulAuthenticationCounter(realm.getBlockAfterUnsuccessfulLogins(), 
				realm.getBlockFor()*1000);
		sessionCookie = StandardWebAuthenticationProcessor.getSessionCookieName(realm.getName());
		this.sessionMan = sessionMan;
		this.sessionBinder = sessionBinder;
		this.rememberMeHelper = rememberMeHelper;
		this.realm = realm;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String servletPath = httpRequest.getServletPath();
		
		if (!HiddenResourcesFilter.hasPathPrefix(servletPath, protectedServletPaths))
		{
			gotoNotProtectedResource(httpRequest, response, chain);
			return;
		}
		
		HttpSession httpSession = httpRequest.getSession(false);
		String loginSessionId;
		
		String clientIp = request.getRemoteAddr();
		
		if (httpSession != null)
		{
			LoginSession loginSession = (LoginSession) httpSession.getAttribute(
					LoginToHttpSessionBinder.USER_SESSION_KEY);
			if (loginSession != null)
			{
				dosGauard.successfulAttempt(clientIp);
				if (!loginSession.isUsedOutdatedCredential())
				{
					loginSessionId = loginSession.getId();
					try
					{
						if (!HiddenResourcesFilter.hasPathPrefix(httpRequest.getPathInfo(), 
								ApplicationConstants.HEARTBEAT_PATH + '/'))
						{
							log.trace("Update session activity for " + loginSessionId);
							sessionMan.updateSessionActivity(loginSessionId);
						}
						gotoProtectedResource(httpRequest, response, chain);
						return;
					} catch (IllegalArgumentException e)
					{
						log.debug("Can't update session activity ts for " + loginSessionId + 
							" - expired(?), HTTP session " + httpSession.getId(), e);
					}
				} else
				{
					log.trace("Outdated credential used - redirect to authN");
					forwardtoAuthn(httpRequest, httpResponse);
					return;
				}
			}
		}
		
		long blockedTime = dosGauard.getRemainingBlockedTime(clientIp); 
		if (blockedTime > 0)
		{
			log.debug("Blocked potential DoS/brute force authN attack from " + clientIp);
			httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access is blocked for " + 
					TimeUnit.MILLISECONDS.toSeconds(blockedTime) + 
					"s more, due to sending too many invalid session cookies.");
			return;
		}
		
		loginSessionId = CookieHelper.getCookie(httpRequest, sessionCookie);
		if (loginSessionId == null)
		{
			handleRememberMe(httpRequest, httpResponse, chain, clientIp);
			return;
		}
		
		LoginSession ls;
		try
		{
			ls = sessionMan.getSession(loginSessionId);
		} catch (IllegalArgumentException e)
		{
			log.trace("Got request with invalid login session id " + loginSessionId + " to " +
					httpRequest.getRequestURI() );
			dosGauard.unsuccessfulAttempt(clientIp);
			clearSessionCookie(httpResponse);
			handleRememberMe(httpRequest, httpResponse, chain, clientIp);
			return;
		}
		dosGauard.successfulAttempt(clientIp);
		if (httpSession == null)
			httpSession = httpRequest.getSession(true);

		sessionBinder.bindHttpSession(httpSession, ls);
		
		gotoProtectedResource(httpRequest, response, chain);
		


	}

	private void handleRememberMe(HttpServletRequest httpRequest, ServletResponse response,
			FilterChain chain, String clientIp) throws IOException, ServletException
	{
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		Optional<LoginSession> loginSessionFromRememberMe = Optional.empty();
		Optional<RememberMeCookie> rememberMeCookie = Optional.empty();
		if (realm.getRememberMePolicy().equals(RememberMePolicy.allowForWholeAuthn))
		{

			try
			{
				rememberMeCookie = rememberMeHelper.getRememberMeUnityCookie(
						httpRequest, realm.getName());
				if (rememberMeCookie.isPresent())
				{
					loginSessionFromRememberMe = rememberMeHelper
							.getLoginSessionFromRememberMeToken(
									rememberMeCookie.get(),
									realm);
				}
			} catch (AuthenticationException e)
			{
				dosGauard.unsuccessfulAttempt(clientIp);
			}

		} else
		{
			forwardtoAuthn(httpRequest, httpResponse);
			return;
		}

		if (!loginSessionFromRememberMe.isPresent())
		{
			if (rememberMeCookie.isPresent())
				rememberMeHelper.clearRememberMeCookieAndUnityToken(realm.getName(),
						httpRequest, httpResponse);
			forwardtoAuthn(httpRequest, httpResponse);
			return;
		}

		log.debug("Whole authn is remembered by entity "
				+ loginSessionFromRememberMe.get().getEntityId() + ", skipping it");
		rememberMeHelper.updateRememberMeCookieAndUnityToken(rememberMeCookie.get(), realm,
				httpResponse);
		dosGauard.successfulAttempt(clientIp);
		sessionBinder.bindHttpSession(httpRequest.getSession(true),
				loginSessionFromRememberMe.get());
		gotoProtectedResource(httpRequest, response, chain);
	}
	
	private void forwardtoAuthn(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException, ServletException
	{
		String forwardURI = authnServletPath;
		if (httpRequest.getPathInfo() != null) 
		{
			forwardURI += httpRequest.getPathInfo();
		}
		if (log.isTraceEnabled()) 
		{
			log.trace("Request to protected address, forward: " + 
					httpRequest.getRequestURI() + " -> " + httpRequest.getContextPath() + forwardURI);
		}
		RequestDispatcher dispatcher = httpRequest.getRequestDispatcher(forwardURI);
		dispatcher.forward(httpRequest, response);
	}

	private void gotoProtectedResource(HttpServletRequest httpRequest, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		if (log.isTraceEnabled())
			log.trace("Request to protected address, user is authenticated: " + 
					httpRequest.getRequestURI());
		chain.doFilter(httpRequest, response);
	}
	
	private void gotoNotProtectedResource(HttpServletRequest httpRequest,
			ServletResponse response, FilterChain chain) throws IOException, ServletException 
	{
		if (log.isTraceEnabled())
			log.trace("Request to not protected address: " + httpRequest.getRequestURI());
		chain.doFilter(httpRequest, response);
	}
	
	private void clearSessionCookie(HttpServletResponse response)
	{
		Cookie unitySessionCookie = new Cookie(sessionCookie, "");
		unitySessionCookie.setPath("/");
		unitySessionCookie.setSecure(true);
		unitySessionCookie.setMaxAge(0);
		unitySessionCookie.setHttpOnly(true);
		response.addCookie(unitySessionCookie);
	}
	
	@Override
	public void destroy()
	{
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}
	
	public void addProtectedPath(String path)
	{
		protectedServletPaths.add(path);
	}

}
