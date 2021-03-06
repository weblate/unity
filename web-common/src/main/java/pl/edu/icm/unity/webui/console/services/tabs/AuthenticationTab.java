/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.webui.console.services.tabs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;

import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.types.authn.AuthenticationFlowDefinition;
import pl.edu.icm.unity.types.authn.AuthenticatorInfo;
import pl.edu.icm.unity.webui.common.FormLayoutWithFixedCaptionWidth;
import pl.edu.icm.unity.webui.common.Images;
import pl.edu.icm.unity.webui.common.chips.GroupedValuesChipsWithDropdown;
import pl.edu.icm.unity.webui.console.services.DefaultServiceDefinition;
import pl.edu.icm.unity.webui.console.services.ServiceEditorBase.EditorTab;
import pl.edu.icm.unity.webui.console.services.ServiceEditorComponent.ServiceEditorTab;

public class AuthenticationTab extends CustomComponent implements EditorTab
{
	private UnityMessageSource msg;
	private List<String> allRealms;
	private List<String> flows;
	private List<String> authenticators;

	private Binder<DefaultServiceDefinition> binder;

	public AuthenticationTab(UnityMessageSource msg, List<AuthenticationFlowDefinition> flows,
			List<AuthenticatorInfo> authenticators, List<String> allRealms, String binding,
			Binder<DefaultServiceDefinition> binder)

	{
		this.msg = msg;
		this.allRealms = allRealms;
		this.flows = WebServiceAuthenticationTab.filterAuthenticationFlow(flows, authenticators, binding);
		this.authenticators = authenticators.stream().filter(a -> a.getSupportedBindings().contains(binding))
				.map(a -> a.getId()).collect(Collectors.toList());
		this.binder = binder;
		initUI();
	}

	private void initUI()
	{
		setCaption(msg.getMessage("ServiceEditorBase.authentication"));
		setIcon(Images.sign_in.getResource());
		
		FormLayoutWithFixedCaptionWidth mainAuthenticationLayout = new FormLayoutWithFixedCaptionWidth();
		ComboBox<String> realm = new ComboBox<>();
		realm.setCaption(msg.getMessage("ServiceEditorBase.realm"));
		realm.setItems(allRealms);
		realm.setEmptySelectionAllowed(false);
		binder.forField(realm).asRequired().bind("realm");
		mainAuthenticationLayout.addComponent(realm);

		Map<String, List<String>> labels = new HashMap<>();
		labels.put(msg.getMessage("ServiceEditorBase.flows"), flows);
		labels.put(msg.getMessage("ServiceEditorBase.authenticators"), authenticators);
		GroupedValuesChipsWithDropdown authAndFlows = new GroupedValuesChipsWithDropdown(labels);
		authAndFlows.setCaption(msg.getMessage("ServiceEditorBase.authenticatorsAndFlows"));
		binder.forField(authAndFlows).withValidator((v, c) -> {
			if (v == null || v.isEmpty())
			{
				return ValidationResult.error(msg.getMessage("fieldRequired"));
			}

			return ValidationResult.ok();

		}).bind("authenticationOptions");
		authAndFlows.setRequiredIndicatorVisible(true);
		mainAuthenticationLayout.addComponent(authAndFlows);
		setCompositionRoot(mainAuthenticationLayout);
	}

	@Override
	public ServiceEditorTab getType()
	{
		return ServiceEditorTab.AUTHENTICATION;
	}

	@Override
	public CustomComponent getComponent()
	{
		return this;
	}
}
