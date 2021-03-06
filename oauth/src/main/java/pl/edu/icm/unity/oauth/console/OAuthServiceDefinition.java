/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.oauth.console;

import java.util.List;

import pl.edu.icm.unity.oauth.as.token.OAuthTokenEndpoint;
import pl.edu.icm.unity.oauth.as.webauthz.OAuthAuthzWebEndpoint;
import pl.edu.icm.unity.types.endpoint.Endpoint.EndpointState;
import pl.edu.icm.unity.webui.console.services.DefaultServiceDefinition;
import pl.edu.icm.unity.webui.console.services.ServiceDefinition;

/**
 * Contains information of {@link OAuthAuthzWebEndpoint} and
 * {@link OAuthTokenEndpoint} configurations and OAuth clients.
 * 
 * @author P.Piernik
 *
 */
class OAuthServiceDefinition implements ServiceDefinition
{
	private DefaultServiceDefinition webAuthzService;
	private DefaultServiceDefinition tokenService;
	private List<OAuthClient> clients;
	private String autoGeneratedClientsGroup;

	OAuthServiceDefinition(DefaultServiceDefinition oauthWebAuthService, DefaultServiceDefinition tokenService)
	{
		this.webAuthzService = oauthWebAuthService;
		this.tokenService = tokenService;
	}

	@Override
	public String getName()
	{
		return webAuthzService.getName();
	}

	@Override
	public EndpointState getState()
	{
		return webAuthzService.getState();
	}

	@Override
	public String getType()
	{
		return webAuthzService.getType();
	}

	@Override
	public String getBinding()
	{
		return webAuthzService.getBinding();
	}

	public DefaultServiceDefinition getWebAuthzService()
	{
		return webAuthzService;
	}

	public DefaultServiceDefinition getTokenService()
	{
		return tokenService;
	}

	public List<OAuthClient> getClients()
	{
		return clients;
	}

	public void setClients(List<OAuthClient> clients)
	{
		this.clients = clients;
	}

	public String getAutoGeneratedClientsGroup()
	{
		return autoGeneratedClientsGroup;
	}

	public void setAutoGeneratedClientsGroup(String autoGeneratedGroup)
	{
		this.autoGeneratedClientsGroup = autoGeneratedGroup;
	}

}
