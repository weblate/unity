/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */


package pl.edu.icm.unity.webui.console.services.authnlayout.configuration.elements;

import java.util.Optional;

import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.webui.VaadinEndpointProperties;
import pl.edu.icm.unity.webui.authn.column.AuthnOptionsColumns;

public class ExpandConfig implements AuthnElementConfiguration
{
	@Override
	public PropertiesRepresentation toProperties(UnityMessageSource msg)
	{
		return new PropertiesRepresentation(AuthnOptionsColumns.SPECIAL_ENTRY_EXPAND);
	}
	
	public static class ExpandConfigFactory implements AuthnElementConfigurationFactory
	{

		@Override
		public Optional<AuthnElementConfiguration> getConfigurationElement(UnityMessageSource msg, VaadinEndpointProperties properties, String specEntry)
		{
			if (!specEntry.equals(AuthnOptionsColumns.SPECIAL_ENTRY_EXPAND))
			{
				return Optional.empty();
			}
			
			return Optional.of(new ExpandConfig());
		}	
	}
}