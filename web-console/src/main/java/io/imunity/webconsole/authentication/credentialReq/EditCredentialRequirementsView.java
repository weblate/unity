/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.webconsole.authentication.credentialReq;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import io.imunity.webadmin.credentialRequirements.CredentialRequirementEditor;
import io.imunity.webconsole.WebConsoleNavigationInfoProviderBase;
import io.imunity.webconsole.authentication.credentialReq.CredentialRequirementsView.CredentialsRequirementsNavigationInfoProvider;
import io.imunity.webelements.helpers.NavigationHelper;
import io.imunity.webelements.helpers.NavigationHelper.CommonViewParam;
import io.imunity.webelements.navigation.NavigationInfo;
import io.imunity.webelements.navigation.NavigationInfo.Type;
import io.imunity.webelements.navigation.UnityView;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.utils.PrototypeComponent;
import pl.edu.icm.unity.exceptions.IllegalCredentialException;
import pl.edu.icm.unity.types.authn.CredentialRequirements;
import pl.edu.icm.unity.webui.WebSession;
import pl.edu.icm.unity.webui.bus.EventsBus;
import pl.edu.icm.unity.webui.common.NotificationPopup;
import pl.edu.icm.unity.webui.common.StandardButtonsHelper;
import pl.edu.icm.unity.webui.exceptions.ControllerException;

/**
 * Edit credential requirements view
 * 
 * @author P.Piernik
 *
 */
@PrototypeComponent
class EditCredentialRequirementsView extends CustomComponent implements UnityView
{
	public static final String VIEW_NAME = "EditCredentialRequirements";

	private CredentialRequirementsController controller;
	private UnityMessageSource msg;
	private CredentialRequirementEditor editor;
	private EventsBus bus;

	private String credReqName;

	@Autowired
	EditCredentialRequirementsView(UnityMessageSource msg, CredentialRequirementsController controller)
	{
		this.controller = controller;
		this.msg = msg;
		this.bus = WebSession.getCurrent().getEventBus();
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		credReqName = NavigationHelper.getParam(event, CommonViewParam.name.toString());

		try
		{
			editor = controller.getEditor(controller.getCredentialRequirements(credReqName));
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
			NavigationHelper.goToView(CredentialRequirementsView.VIEW_NAME);
			return;
		}

		VerticalLayout main = new VerticalLayout();
		main.setMargin(false);
		main.addComponent(editor);
		main.addComponent(StandardButtonsHelper.buildConfirmEditButtonsBar(msg, () -> onConfirm(),
				() -> onCancel()));
		setCompositionRoot(main);
	}

	private void onConfirm()
	{

		CredentialRequirements cred;
		try
		{
			cred = editor.getCredentialRequirements();
		} catch (IllegalCredentialException e)
		{
			return;
		}

		try
		{
			controller.updateCredentialRequirements(cred, bus);

		} catch (ControllerException e)
		{

			NotificationPopup.showError(msg, e);
			return;
		}

		NavigationHelper.goToView(CredentialRequirementsView.VIEW_NAME);

	}

	private void onCancel()
	{
		NavigationHelper.goToView(CredentialRequirementsView.VIEW_NAME);

	}

	@Override
	public String getViewName()
	{
		return VIEW_NAME;
	}

	@Override
	public String getDisplayedName()
	{
		return credReqName;
	}

	@Component
	public static class EditCredentialRequirementsNavigationInfoProvider
			extends WebConsoleNavigationInfoProviderBase
	{

		@Autowired
		public EditCredentialRequirementsNavigationInfoProvider(
				CredentialsRequirementsNavigationInfoProvider parent,
				ObjectFactory<EditCredentialRequirementsView> factory)
		{
			super(new NavigationInfo.NavigationInfoBuilder(VIEW_NAME, Type.ParameterizedView)
					.withParent(parent.getNavigationInfo()).withObjectFactory(factory).build());

		}
	}
}
