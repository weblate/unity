/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.webconsole.directorySetup.automation;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import io.imunity.webadmin.bulk.ScheduledRuleParamEditorImpl;
import io.imunity.webconsole.WebConsoleNavigationInfoProviderBase;
import io.imunity.webconsole.directorySetup.automation.AutomationView.AutomationNavigationInfoProvider;
import io.imunity.webelements.helpers.NavigationHelper;
import io.imunity.webelements.helpers.NavigationHelper.CommonViewParam;
import io.imunity.webelements.navigation.NavigationInfo;
import io.imunity.webelements.navigation.NavigationInfo.Type;
import io.imunity.webelements.navigation.UnityView;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.utils.PrototypeComponent;
import pl.edu.icm.unity.types.bulkops.ScheduledProcessingRuleParam;
import pl.edu.icm.unity.webui.common.FormValidationException;
import pl.edu.icm.unity.webui.common.NotificationPopup;
import pl.edu.icm.unity.webui.common.StandardButtonsHelper;
import pl.edu.icm.unity.webui.exceptions.ControllerException;

/**
 * Edit schedule rule view
 * 
 * @author P.Piernik
 *
 */
@PrototypeComponent
class EditAutomationView extends CustomComponent implements UnityView
{
	public static final String VIEW_NAME = "EditAutomation";

	private AutomationController controller;
	private UnityMessageSource msg;
	private ScheduledRuleParamEditorImpl editor;

	private String ruleId;

	EditAutomationView(AutomationController controller, UnityMessageSource msg)
	{
		this.controller = controller;
		this.msg = msg;
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		ruleId = NavigationHelper.getParam(event, CommonViewParam.id.toString());

		try
		{
			editor = controller.getScheduleRuleEditor(controller.getScheduledRule(ruleId));
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
			NavigationHelper.goToView(AutomationView.VIEW_NAME);
			return;
		}

		VerticalLayout main = new VerticalLayout();
		main.setMargin(false);
		main.addComponent(editor);
		main.addComponent(StandardButtonsHelper.buildConfirmEditButtonsBar(msg, () -> onConfirm(ruleId),
				() -> onCancel()));
		setCompositionRoot(main);
	}

	private void onConfirm(String oldId)
	{
		ScheduledProcessingRuleParam rule;
		try
		{
			rule = editor.getRule();
		} catch (FormValidationException e)
		{
			return;
		}

		try
		{
			controller.updateScheduledRule(oldId, rule);
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
			return;
		}

		NavigationHelper.goToView(AutomationView.VIEW_NAME);

	}

	private void onCancel()
	{
		NavigationHelper.goToView(AutomationView.VIEW_NAME);

	}

	@Override
	public String getViewName()
	{
		return VIEW_NAME;
	}

	@Override
	public String getDisplayedName()
	{
		return msg.getMessage("edit");
	}

	@Component
	public static class EditAutomationNavigationInfoProvider extends WebConsoleNavigationInfoProviderBase
	{
		@Autowired
		public EditAutomationNavigationInfoProvider(AutomationNavigationInfoProvider parent,
				ObjectFactory<EditAutomationView> factory)
		{
			super(new NavigationInfo.NavigationInfoBuilder(VIEW_NAME, Type.ParameterizedView)
					.withParent(parent.getNavigationInfo()).withObjectFactory(factory).build());

		}
	}

}
