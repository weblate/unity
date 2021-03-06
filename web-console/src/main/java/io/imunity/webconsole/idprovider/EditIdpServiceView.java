/**
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.webconsole.idprovider;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.imunity.webconsole.WebConsoleNavigationInfoProviderBase;
import io.imunity.webconsole.idprovider.IdpServicesView.IdpServicesNavigationInfoProvider;
import io.imunity.webconsole.services.base.EditServiceViewBase;
import io.imunity.webelements.navigation.NavigationInfo;
import io.imunity.webelements.navigation.NavigationInfo.Type;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.utils.PrototypeComponent;

/**
 * Shows IDP service editor
 * 
 * @author P.Piernik
 *
 */
@PrototypeComponent
class EditIdpServiceView extends EditServiceViewBase
{
	public static final String VIEW_NAME = "EditIdpService";

	@Autowired
	EditIdpServiceView(UnityMessageSource msg, IdpServicesController controller)
	{
		super(msg, controller, IdpServicesView.VIEW_NAME);
	}

	@Override
	public String getViewName()
	{
		return VIEW_NAME;
	}

	@Component
	public static class EditServiceNavigationInfoProvider extends WebConsoleNavigationInfoProviderBase
	{

		@Autowired
		public EditServiceNavigationInfoProvider(IdpServicesNavigationInfoProvider parent,
				ObjectFactory<EditIdpServiceView> factory)
		{
			super(new NavigationInfo.NavigationInfoBuilder(VIEW_NAME, Type.ParameterizedViewWithSubviews)
					.withParent(parent.getNavigationInfo()).withObjectFactory(factory).build());

		}
	}
}
