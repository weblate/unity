/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.webconsole.service;

import org.springframework.stereotype.Component;

import io.imunity.webconsole.WebConsoleEndpointFactory;
import pl.edu.icm.unity.engine.api.AuthenticationFlowManagement;
import pl.edu.icm.unity.engine.api.AuthenticatorManagement;
import pl.edu.icm.unity.engine.api.EndpointManagement;
import pl.edu.icm.unity.engine.api.RealmsManagement;
import pl.edu.icm.unity.engine.api.RegistrationsManagement;
import pl.edu.icm.unity.engine.api.authn.AuthenticatorSupportService;
import pl.edu.icm.unity.engine.api.config.UnityServerConfiguration;
import pl.edu.icm.unity.engine.api.files.FileStorageService;
import pl.edu.icm.unity.engine.api.files.URIAccessService;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.webui.console.services.WebServiceControllerBase;

/**
 * Console service controller. Based on the standard web service editor
 * 
 * @author P.Piernik
 *
 */
@Component
class ConsoleServiceController extends WebServiceControllerBase
{
	ConsoleServiceController(UnityMessageSource msg, EndpointManagement endpointMan, RealmsManagement realmsMan,
			AuthenticationFlowManagement flowsMan, AuthenticatorManagement authMan,
			RegistrationsManagement registrationMan, URIAccessService uriAccessService,
			FileStorageService fileStorageService, UnityServerConfiguration serverConfig,
			AuthenticatorSupportService authenticatorSupportService)
	{
		super(WebConsoleEndpointFactory.TYPE, msg, endpointMan, realmsMan, flowsMan, authMan, registrationMan,
				uriAccessService, fileStorageService, serverConfig, authenticatorSupportService);
	}
}
