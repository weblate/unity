/*
 * Copyright (c) 2018 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.webconsole.authentication.authenticators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.imunity.webconsole.WebConsoleNavigationInfoProviderBase;
import io.imunity.webconsole.authentication.facilities.AuthenticationFacilitiesView.AuthenticationFacilitiesNavigationInfoProvider;
import io.imunity.webelements.navigation.NavigationInfo;
import io.imunity.webelements.navigation.NavigationInfo.Type;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;

/**
 * Provides @{link {@link NavigationInfo} about authenticators
 * 
 * @author P.Piernik
 *
 */
@Component
class AuthenticatorsNavigationInfoProvider extends WebConsoleNavigationInfoProviderBase
{
	public static final String ID = "Authenticators";

	@Autowired
	AuthenticatorsNavigationInfoProvider(UnityMessageSource msg,
			AuthenticationFacilitiesNavigationInfoProvider parent)
	{
		super(new NavigationInfo.NavigationInfoBuilder(ID, Type.ViewGroup)
				.withParent(parent.getNavigationInfo())
				.withCaption(msg.getMessage("Authenticators.navCaption"))
				.build());

	}

}
