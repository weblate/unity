/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.saml.idp.console;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.imunity.webconsole.utils.tprofile.OutputTranslationProfileFieldFactory;
import pl.edu.icm.unity.engine.api.PKIManagement;
import pl.edu.icm.unity.engine.api.authn.AuthenticatorSupportService;
import pl.edu.icm.unity.engine.api.config.UnityServerConfiguration;
import pl.edu.icm.unity.engine.api.files.FileStorageService;
import pl.edu.icm.unity.engine.api.files.URIAccessService;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.server.NetworkServer;
import pl.edu.icm.unity.types.authn.AuthenticationFlowDefinition;
import pl.edu.icm.unity.types.authn.AuthenticatorInfo;
import pl.edu.icm.unity.types.basic.Group;
import pl.edu.icm.unity.types.basic.IdentityType;
import pl.edu.icm.unity.types.endpoint.EndpointTypeDescription;
import pl.edu.icm.unity.webui.common.FormValidationException;
import pl.edu.icm.unity.webui.common.webElements.SubViewSwitcher;
import pl.edu.icm.unity.webui.console.services.ServiceDefinition;
import pl.edu.icm.unity.webui.console.services.ServiceEditor;
import pl.edu.icm.unity.webui.console.services.ServiceEditorComponent;
import pl.edu.icm.unity.webui.console.services.idp.IdpUser;

/**
 * SAML Service editor
 * 
 * @author P.Piernik
 *
 */
public class SAMLServiceEditor implements ServiceEditor
{
	private UnityMessageSource msg;
	private EndpointTypeDescription type;
	private PKIManagement pkiMan;
	private List<String> allRealms;
	private List<AuthenticationFlowDefinition> flows;
	private List<AuthenticatorInfo> authenticators;
	private SAMLServiceEditorComponent editor;
	private List<String> allAttributes;
	private List<Group> allGroups;
	private List<IdpUser> allUsers;
	private List<String> registrationForms;
	private Set<String> credentials;
	private Set<String> truststores;
	private URIAccessService uriAccessService;
	private FileStorageService fileStorageService;
	private UnityServerConfiguration serverConfig;
	private AuthenticatorSupportService authenticatorSupportService;
	private NetworkServer server;
	private Collection<IdentityType> idTypes;
	private SubViewSwitcher subViewSwitcher;
	private OutputTranslationProfileFieldFactory outputTranslationProfileFieldFactory;
	private List<String> usedPaths;

	public SAMLServiceEditor(UnityMessageSource msg, EndpointTypeDescription type, PKIManagement pkiMan,
			SubViewSwitcher subViewSwitcher,
			OutputTranslationProfileFieldFactory outputTranslationProfileFieldFactory, NetworkServer server,
			URIAccessService uriAccessService, FileStorageService fileStorageService,
			UnityServerConfiguration serverConfig, List<String> allRealms,
			List<AuthenticationFlowDefinition> flows, List<AuthenticatorInfo> authenticators,
			List<String> allAttributes, List<Group> allGroups, List<IdpUser> allUsers,
			List<String> registrationForms, Set<String> credentials, Set<String> truststores,
			AuthenticatorSupportService authenticatorSupportService, Collection<IdentityType> idTypes,
			List<String> usedPaths)
	{
		this.msg = msg;
		this.type = type;
		this.allRealms = allRealms;
		this.authenticators = authenticators;
		this.flows = flows;
		this.allAttributes = allAttributes;
		this.allGroups = allGroups;
		this.registrationForms = registrationForms;
		this.uriAccessService = uriAccessService;
		this.fileStorageService = fileStorageService;
		this.serverConfig = serverConfig;
		this.authenticatorSupportService = authenticatorSupportService;
		this.credentials = credentials;
		this.server = server;
		this.idTypes = idTypes;
		this.subViewSwitcher = subViewSwitcher;
		this.outputTranslationProfileFieldFactory = outputTranslationProfileFieldFactory;
		this.usedPaths = usedPaths;
		this.allUsers = allUsers;
		this.truststores = truststores;
		this.pkiMan = pkiMan;
	}

	@Override
	public ServiceEditorComponent getEditor(ServiceDefinition endpoint)
	{
		editor = new SAMLServiceEditorComponent(msg, type, pkiMan, subViewSwitcher, server, uriAccessService,
				fileStorageService, serverConfig, outputTranslationProfileFieldFactory, endpoint,
				allRealms, flows, authenticators, allGroups, allUsers, registrationForms, credentials,
				truststores, authenticatorSupportService, idTypes, allAttributes, usedPaths);
		return editor;
	}

	@Override
	public ServiceDefinition getEndpointDefiniton() throws FormValidationException
	{
		return editor.getServiceDefiniton();
	}
}
