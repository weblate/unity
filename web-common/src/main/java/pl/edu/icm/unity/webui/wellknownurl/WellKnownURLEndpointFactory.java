/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webui.wellknownurl;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.engine.api.endpoint.EndpointFactory;
import pl.edu.icm.unity.engine.api.endpoint.EndpointInstance;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.server.NetworkServer;
import pl.edu.icm.unity.engine.api.wellknown.SecuredWellKnownURLServlet;
import pl.edu.icm.unity.types.endpoint.EndpointTypeDescription;
import pl.edu.icm.unity.webui.VaadinEndpoint;
import pl.edu.icm.unity.webui.authn.VaadinAuthentication;

@Component
public class WellKnownURLEndpointFactory implements EndpointFactory
{
	public static final String NAME = "WellKnownLinksHandler";

	public static final EndpointTypeDescription TYPE = new EndpointTypeDescription(NAME,
			"Provides access to public links which can be used to access parts of " + "Unity UIs directly",
			VaadinAuthentication.NAME,
			Collections.singletonMap(SecuredWellKnownURLServlet.SERVLET_PATH, "Well known links endpoint"));
	private ApplicationContext applicationContext;
	private NetworkServer server;
	private UnityMessageSource msg;

	@Autowired
	public WellKnownURLEndpointFactory(ApplicationContext applicationContext, NetworkServer server,
			UnityMessageSource msg)
	{
		this.applicationContext = applicationContext;
		this.server = server;
		this.msg = msg;
	}

	@Override
	public EndpointTypeDescription getDescription()
	{
		return TYPE;
	}

	@Override
	public EndpointInstance newInstance()
	{
		return new VaadinEndpoint(server, msg, applicationContext, SecuredNavigationUI.class.getSimpleName(),
				SecuredWellKnownURLServlet.SERVLET_PATH);
	}

}
