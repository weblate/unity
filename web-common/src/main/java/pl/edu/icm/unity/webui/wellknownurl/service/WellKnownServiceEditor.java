/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.webui.wellknownurl.service;

import java.util.List;

import com.vaadin.data.Binder;

import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.types.authn.AuthenticationFlowDefinition;
import pl.edu.icm.unity.types.authn.AuthenticatorInfo;
import pl.edu.icm.unity.webui.common.FormValidationException;
import pl.edu.icm.unity.webui.console.services.DefaultServiceDefinition;
import pl.edu.icm.unity.webui.console.services.ServiceDefinition;
import pl.edu.icm.unity.webui.console.services.ServiceEditor;
import pl.edu.icm.unity.webui.console.services.ServiceEditorBase;
import pl.edu.icm.unity.webui.console.services.ServiceEditorComponent;
import pl.edu.icm.unity.webui.console.services.tabs.AuthenticationTab;
import pl.edu.icm.unity.webui.console.services.tabs.GeneralTab;
import pl.edu.icm.unity.webui.wellknownurl.WellKnownURLEndpointFactory;

/**
 * 
 * @author P.Piernik
 *
 */
public class WellKnownServiceEditor implements ServiceEditor
{
	private UnityMessageSource msg;
	private List<String> allRealms;
	private List<AuthenticationFlowDefinition> flows;
	private List<AuthenticatorInfo> authenticators;
	private WellKnownServiceEditorComponent editor;
	private List<String> usedPaths;

	public WellKnownServiceEditor(UnityMessageSource msg, List<String> allRealms,
			List<AuthenticationFlowDefinition> flows, List<AuthenticatorInfo> authenticators,  List<String> usedPaths)
	{
		this.msg = msg;
		this.allRealms = allRealms;
		this.authenticators = authenticators;
		this.flows = flows;
		this.usedPaths = usedPaths;
	}

	@Override
	public ServiceEditorComponent getEditor(ServiceDefinition endpoint)
	{
		editor = new WellKnownServiceEditorComponent(msg, (DefaultServiceDefinition) endpoint, allRealms, flows,
				authenticators);
		return editor;
	}

	@Override
	public ServiceDefinition getEndpointDefiniton() throws FormValidationException
	{
		return editor.getServiceDefiniton();
	}

	private class WellKnownServiceEditorComponent extends ServiceEditorBase
	{

		private Binder<DefaultServiceDefinition> serviceBinder;

		public WellKnownServiceEditorComponent(UnityMessageSource msg, DefaultServiceDefinition toEdit,
				List<String> allRealms, List<AuthenticationFlowDefinition> flows,
				List<AuthenticatorInfo> authenticators)
		{
			super(msg);
			boolean editMode = toEdit != null;
			serviceBinder = new Binder<>(DefaultServiceDefinition.class);

			registerTab(new GeneralTab(msg, serviceBinder, WellKnownURLEndpointFactory.TYPE, usedPaths, editMode));
			registerTab(new AuthenticationTab(msg, flows, authenticators, allRealms,
					WellKnownURLEndpointFactory.TYPE.getSupportedBinding(), serviceBinder));
			serviceBinder.setBean(editMode ? toEdit
					: new DefaultServiceDefinition(WellKnownURLEndpointFactory.TYPE.getName()));

		}

		public ServiceDefinition getServiceDefiniton() throws FormValidationException
		{
			boolean hasErrors = serviceBinder.validate().hasErrors();
			if (hasErrors)
			{
				setErrorInTabs();
				throw new FormValidationException();
			}

			DefaultServiceDefinition service = serviceBinder.getBean();
			service.setConfiguration("");
			return service;
		}

	}

}
