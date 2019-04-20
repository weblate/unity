/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.webconsole.utils.tprofile;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import io.imunity.webadmin.tprofile.ActionParameterComponentProvider;
import io.imunity.webadmin.tprofile.TranslationProfileEditor;
import io.imunity.webelements.helpers.StandardButtonsHelper;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.translation.TranslationActionFactory;
import pl.edu.icm.unity.engine.api.utils.TypesRegistryBase;
import pl.edu.icm.unity.types.translation.ProfileType;
import pl.edu.icm.unity.types.translation.TranslationProfile;
import pl.edu.icm.unity.webui.common.FormValidationException;
import pl.edu.icm.unity.webui.common.webElements.UnitySubView;

/**
 * Subview for editing translation profile
 * @author P.Piernik
 *
 */
public class EditTranslationProfileSubView extends CustomComponent implements UnitySubView
{
	private UnityMessageSource msg;
	private TranslationProfileEditor editor;

	private Consumer<TranslationProfile> onConfirm;
	private Runnable onCancel;

	public EditTranslationProfileSubView(UnityMessageSource msg,
			TypesRegistryBase<? extends TranslationActionFactory<?>> registry, ProfileType type,
			ActionParameterComponentProvider actionComponentProvider,
			Consumer<TranslationProfile> onConfirm, Runnable onCancel)
	{

		this.msg = msg;
		this.onConfirm = onConfirm;
		this.onCancel = onCancel;
		editor = new TranslationProfileEditor(msg, registry, type, actionComponentProvider);
		editor.setMargin(false);
		editor.rulesOnlyMode();
		initUI();
	}

	private void initUI()
	{
		VerticalLayout main = new VerticalLayout();
		main.addComponent(editor);
		main.addComponent(StandardButtonsHelper.buildConfirmEditButtonsBar(msg, () -> {
			try
			{
				onConfirm.accept(editor.getProfile());
			} catch (FormValidationException e)
			{
				// ok
			}
		}, onCancel));
		setCompositionRoot(main);
	}

	public void setInput(TranslationProfile profile)
	{
		editor.setValue(profile);
		editor.focusFirst();
	}

	@Override
	public List<String> getBredcrumbs()
	{
		return Arrays.asList(msg.getMessage("RemoteDataMapping.caption"), msg.getMessage("edit"));
	}

}
