/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.webconsole.utils.tprofile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.imunity.webadmin.tprofile.ActionParameterComponentProvider;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.translation.in.InputTranslationActionsRegistry;

/**
 * Factory for {@link TranslationProfileField}.
 * 
 * @author P.Piernik
 *
 */
@Component
public class InputTranslationProfileFieldFactory extends TranslationProfileFieldFactoryBase
{
	@Autowired
	InputTranslationProfileFieldFactory(UnityMessageSource msg,
			InputTranslationActionsRegistry inputActionsRegistry,
			ActionParameterComponentProvider actionComponentProvider)
	{

		super(msg.getMessage("InputTranslationProfileSection.caption"), msg, inputActionsRegistry,
				actionComponentProvider);
	}
}
