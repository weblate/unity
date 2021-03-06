/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.saml.idp.console;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.vaadin.risto.stepper.IntStepper;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import io.imunity.webconsole.utils.tprofile.OutputTranslationProfileFieldFactory;
import pl.edu.icm.unity.engine.api.config.UnityServerConfiguration;
import pl.edu.icm.unity.engine.api.endpoint.EndpointPathValidator;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.server.NetworkServer;
import pl.edu.icm.unity.exceptions.WrongArgumentException;
import pl.edu.icm.unity.saml.idp.SamlIdpProperties.AssertionSigningPolicy;
import pl.edu.icm.unity.saml.idp.SamlIdpProperties.RequestAcceptancePolicy;
import pl.edu.icm.unity.saml.idp.SamlIdpProperties.ResponseSigningPolicy;
import pl.edu.icm.unity.saml.idp.console.common.SAMLIdentityMapping;
import pl.edu.icm.unity.types.basic.IdentityType;
import pl.edu.icm.unity.webui.common.CollapsibleLayout;
import pl.edu.icm.unity.webui.common.FieldSizeConstans;
import pl.edu.icm.unity.webui.common.FormLayoutWithFixedCaptionWidth;
import pl.edu.icm.unity.webui.common.GridWithEditor;
import pl.edu.icm.unity.webui.common.Images;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.file.FileField;
import pl.edu.icm.unity.webui.common.i18n.I18nTextField;
import pl.edu.icm.unity.webui.common.webElements.SubViewSwitcher;
import pl.edu.icm.unity.webui.console.services.DefaultServiceDefinition;
import pl.edu.icm.unity.webui.console.services.ServiceEditorBase.EditorTab;
import pl.edu.icm.unity.webui.console.services.ServiceEditorComponent.ServiceEditorTab;
import xmlbeans.org.oasis.saml2.metadata.EntityDescriptorDocument;

/**
 * SAML service editor general tab
 * 
 * @author P.Piernik
 *
 */
public class SAMLEditorGeneralTab extends CustomComponent implements EditorTab
{
	private UnityMessageSource msg;
	private Binder<DefaultServiceDefinition> samlServiceBinder;
	private Binder<SAMLServiceConfiguration> configBinder;
	private OutputTranslationProfileFieldFactory profileFieldFactory;
	private UnityServerConfiguration serverConfig;
	private SubViewSwitcher subViewSwitcher;
	private Set<String> credentials;
	private Set<String> truststores;
	private List<String> usedPaths;
	private String serverPrefix;
	private Collection<IdentityType> idTypes;
	private boolean editMode;
	private CheckBox signMetadata;
	private HorizontalLayout infoLayout;
	private boolean initialValidation;
	private HorizontalLayout metaLinkButtonWrapper;
	private Label metaOffInfo;

	public SAMLEditorGeneralTab(UnityMessageSource msg, NetworkServer server, UnityServerConfiguration serverConfig,
			SubViewSwitcher subViewSwitcher, OutputTranslationProfileFieldFactory profileFieldFactory,
			Binder<DefaultServiceDefinition> samlServiceBinder,
			Binder<SAMLServiceConfiguration> configBinder, boolean editMode, List<String> usedPaths,
			Set<String> credentials, Set<String> truststores, Collection<IdentityType> idTypes)
	{
		this.msg = msg;
		this.serverConfig = serverConfig;
		this.samlServiceBinder = samlServiceBinder;
		this.configBinder = configBinder;
		this.editMode = editMode;
		this.subViewSwitcher = subViewSwitcher;
		this.profileFieldFactory = profileFieldFactory;
		this.usedPaths = usedPaths;
		this.credentials = credentials;
		this.truststores = truststores;
		this.idTypes = idTypes;
		serverPrefix = server.getAdvertisedAddress().toString();
		initUI();
	}

	private void initUI()
	{
		setCaption(msg.getMessage("ServiceEditorBase.general"));
		setIcon(Images.cogs.getResource());
		VerticalLayout main = new VerticalLayout();
		main.setMargin(false);
		main.addComponent(buildHeaderSection());
		main.addComponent(buildMetadataSection());
		main.addComponent(buildAdvancedSection());
		main.addComponent(buildIdenityTypeMappingSection());
		main.addComponent(profileFieldFactory.getWrappedFieldInstance(subViewSwitcher, configBinder,
				"translationProfile"));
		setCompositionRoot(main);
	}

	private Component buildHeaderSection()
	{
		HorizontalLayout main = new HorizontalLayout();
		main.setMargin(new MarginInfo(true, false));

		FormLayoutWithFixedCaptionWidth mainGeneralLayout = new FormLayoutWithFixedCaptionWidth();
		main.addComponent(mainGeneralLayout);

		Button metaLinkButton = new Button();
		metaOffInfo = new Label();
		metaOffInfo.setCaption(msg.getMessage("SAMLEditorGeneralTab.metadataOff"));
		
		infoLayout = new HorizontalLayout();
		infoLayout.setMargin(new MarginInfo(false, true, false, true));
		infoLayout.setStyleName("u-marginLeftMinus30");
		infoLayout.addStyleName("u-border");
		VerticalLayout wrapper = new VerticalLayout();
		wrapper.setMargin(false);
		infoLayout.addComponent(wrapper);
		wrapper.addComponent(new Label(msg.getMessage("SAMLEditorGeneralTab.importantURLs")));
		FormLayout infoLayoutWrapper = new FormLayout();
		infoLayoutWrapper.setSpacing(false);
		infoLayoutWrapper.setMargin(false);
		wrapper.addComponent(infoLayoutWrapper);
		metaLinkButtonWrapper = new HorizontalLayout();
		metaLinkButtonWrapper.setCaption(msg.getMessage("SAMLEditorGeneralTab.metadataLink"));
		metaLinkButton.setStyleName(Styles.vButtonLink.toString());
		metaLinkButtonWrapper.addComponent(metaLinkButton);
		infoLayoutWrapper.addComponent(metaLinkButtonWrapper);
		infoLayoutWrapper.addComponent(metaOffInfo);
		metaLinkButton.addClickListener(e -> {
			Page.getCurrent().open(metaLinkButton.getCaption(), "_blank", false);
		});
		main.addComponent(infoLayout);
		infoLayout.setVisible(editMode);

		TextField name = new TextField();
		name.setCaption(msg.getMessage("ServiceEditorBase.name"));
		name.setReadOnly(editMode);
		samlServiceBinder.forField(name).asRequired().bind("name");
		mainGeneralLayout.addComponent(name);

		TextField contextPath = new TextField();
		contextPath.setPlaceholder("/saml-idp");
		contextPath.setRequiredIndicatorVisible(true);
		contextPath.setCaption(msg.getMessage("SAMLEditorGeneralTab.contextPath"));
		contextPath.setReadOnly(editMode);
		samlServiceBinder.forField(contextPath).withValidator((v, c) -> {

			if (!editMode && v != null && usedPaths.contains(v))
			{
				return ValidationResult.error(msg.getMessage("ServiceEditorBase.usedContextPath"));
			}

			try
			{
				EndpointPathValidator.validateEndpointPath(v);
				metaLinkButton.setCaption(serverPrefix + v + "/metadata");
			} catch (WrongArgumentException e)
			{
				return ValidationResult.error(msg.getMessage("ServiceEditorBase.invalidContextPath"));
			}

			return ValidationResult.ok();

		}).bind("address");
		mainGeneralLayout.addComponent(contextPath);

		I18nTextField displayedName = new I18nTextField(msg);
		displayedName.setCaption(msg.getMessage("ServiceEditorBase.displayedName"));
		samlServiceBinder.forField(displayedName).bind("displayedName");
		mainGeneralLayout.addComponent(displayedName);

		TextField description = new TextField();
		description.setCaption(msg.getMessage("ServiceEditorBase.description"));
		samlServiceBinder.forField(description).bind("description");
		mainGeneralLayout.addComponent(description);

		TextField issuerURI = new TextField();
		issuerURI.setWidth(FieldSizeConstans.LINK_FIELD_WIDTH, FieldSizeConstans.LINK_FIELD_WIDTH_UNIT);
		issuerURI.setCaption(msg.getMessage("SAMLEditorGeneralTab.issuerURI"));
		configBinder.forField(issuerURI).asRequired().bind("issuerURI");
		mainGeneralLayout.addComponent(issuerURI);

		ComboBox<AssertionSigningPolicy> signAssertionPolicy = new ComboBox<>();
		signAssertionPolicy.setItems(AssertionSigningPolicy.values());
		signAssertionPolicy.setEmptySelectionAllowed(false);
		signAssertionPolicy.setCaption(msg.getMessage("SAMLEditorGeneralTab.signAssertionPolicy"));
		configBinder.forField(signAssertionPolicy).asRequired().bind("signAssertionPolicy");
		mainGeneralLayout.addComponent(signAssertionPolicy);

		ComboBox<ResponseSigningPolicy> signResponcePolicy = new ComboBox<>();
		signResponcePolicy.setItems(ResponseSigningPolicy.values());
		signResponcePolicy.setEmptySelectionAllowed(false);
		signResponcePolicy.setCaption(msg.getMessage("SAMLEditorGeneralTab.signResponcePolicy"));
		configBinder.forField(signResponcePolicy).asRequired().bind("signResponcePolicy");
		mainGeneralLayout.addComponent(signResponcePolicy);

		ComboBox<String> signResponseCredential = new ComboBox<>();
		signResponseCredential.setCaption(msg.getMessage("SAMLEditorGeneralTab.signResponseCredential"));
		signResponseCredential.setItems(credentials);
		configBinder.forField(signResponseCredential)
				.asRequired((v, c) -> ((v == null || v.isEmpty()))
						? ValidationResult.error(msg.getMessage("fieldRequired"))
						: ValidationResult.ok())
				.bind("signResponseCredential");
		mainGeneralLayout.addComponent(signResponseCredential);

		ComboBox<String> httpsTruststore = new ComboBox<>(
				msg.getMessage("SAMLEditorGeneralTab.httpsTruststore"));
		httpsTruststore.setItems(truststores);
		configBinder.forField(httpsTruststore).bind("httpsTruststore");
		mainGeneralLayout.addComponent(httpsTruststore);

		CheckBox skipConsentScreen = new CheckBox(msg.getMessage("SAMLEditorGeneralTab.skipConsentScreen"));
		configBinder.forField(skipConsentScreen).bind("skipConsentScreen");
		mainGeneralLayout.addComponent(skipConsentScreen);

		CheckBox editableConsentScreen = new CheckBox(
				msg.getMessage("SAMLEditorGeneralTab.editableConsentScreen"));
		configBinder.forField(editableConsentScreen).bind("editableConsentScreen");
		mainGeneralLayout.addComponent(editableConsentScreen);

		skipConsentScreen.addValueChangeListener(e -> editableConsentScreen.setEnabled(!e.getValue()));

		ComboBox<RequestAcceptancePolicy> acceptPolicy = new ComboBox<>();
		acceptPolicy.setItems(RequestAcceptancePolicy.values());
		acceptPolicy.setEmptySelectionAllowed(false);
		acceptPolicy.setCaption(msg.getMessage("SAMLEditorGeneralTab.acceptPolicy"));
		configBinder.forField(acceptPolicy).asRequired().bind("requestAcceptancePolicy");
		mainGeneralLayout.addComponent(acceptPolicy);

		return main;
	}

	private CollapsibleLayout buildAdvancedSection()
	{
		FormLayoutWithFixedCaptionWidth advancedLayout = new FormLayoutWithFixedCaptionWidth();
		advancedLayout.setMargin(false);

		IntStepper authenticationTimeout = new IntStepper();
		authenticationTimeout.setWidth(5, Unit.EM);
		authenticationTimeout.setCaption(msg.getMessage("SAMLEditorGeneralTab.authenticationTimeout"));
		configBinder.forField(authenticationTimeout).asRequired(msg.getMessage("notAPositiveNumber"))
				.withValidator(new IntegerRangeValidator(msg.getMessage("notAPositiveNumber"), 1, null))
				.bind("authenticationTimeout");
		advancedLayout.addComponent(authenticationTimeout);

		IntStepper requestValidity = new IntStepper();
		requestValidity.setWidth(5, Unit.EM);
		requestValidity.setCaption(msg.getMessage("SAMLEditorGeneralTab.requestValidity"));
		configBinder.forField(requestValidity).asRequired(msg.getMessage("notAPositiveNumber"))
				.withValidator(new IntegerRangeValidator(msg.getMessage("notAPositiveNumber"), 1, null))
				.bind("requestValidity");
		advancedLayout.addComponent(requestValidity);

		IntStepper attrAssertionValidity = new IntStepper();
		attrAssertionValidity.setWidth(5, Unit.EM);
		attrAssertionValidity.setCaption(msg.getMessage("SAMLEditorGeneralTab.attributeAssertionValidity"));
		configBinder.forField(attrAssertionValidity).asRequired(msg.getMessage("notAPositiveNumber"))
				.withValidator(new IntegerRangeValidator(msg.getMessage("notAPositiveNumber"), 1, null))
				.bind("attrAssertionValidity");
		advancedLayout.addComponent(attrAssertionValidity);

		CheckBox returnSingleAssertion = new CheckBox(
				msg.getMessage("SAMLEditorGeneralTab.returnSingleAssertion"));
		configBinder.forField(returnSingleAssertion).bind("returnSingleAssertion");
		advancedLayout.addComponent(returnSingleAssertion);

		return new CollapsibleLayout(msg.getMessage("SAMLEditorGeneralTab.advanced"), advancedLayout);
	}

	private CollapsibleLayout buildMetadataSection()
	{
		FormLayoutWithFixedCaptionWidth metadataPublishing = new FormLayoutWithFixedCaptionWidth();
		metadataPublishing.setMargin(false);

		CheckBox publishMetadata = new CheckBox(msg.getMessage("SAMLEditorGeneralTab.publishMetadata"));
		configBinder.forField(publishMetadata).withValidator((v, c) -> {
			if (!initialValidation)
			{
				if (v)
				{
					metaLinkButtonWrapper.setVisible(true);
					metaOffInfo.setVisible(false);
				} else
				{
					metaLinkButtonWrapper.setVisible(false);
					metaOffInfo.setVisible(true);
				}
				initialValidation = true;
			}
			return ValidationResult.ok();

		}).bind("publishMetadata");
		metadataPublishing.addComponent(publishMetadata);

		signMetadata = new CheckBox(msg.getMessage("SAMLEditorGeneralTab.signMetadata"));
		configBinder.forField(signMetadata).bind("signMetadata");
		signMetadata.setEnabled(false);
		metadataPublishing.addComponent(signMetadata);

		CheckBox autoGenerateMetadata = new CheckBox(
				msg.getMessage("SAMLEditorGeneralTab.autoGenerateMetadata"));
		configBinder.forField(autoGenerateMetadata).bind("autoGenerateMetadata");
		autoGenerateMetadata.setEnabled(false);
		metadataPublishing.addComponent(autoGenerateMetadata);

		FileField metadataSource = new FileField(msg, "text/xml", "metadata.xml",
				serverConfig.getFileSizeLimit());
		metadataSource.setCaption(msg.getMessage("SAMLEditorGeneralTab.metadataFile"));
		metadataSource.configureBinding(configBinder, "metadataSource", Optional.of((value, context) -> {
			if (value != null && value.getLocal() != null)
			{
				try
				{
					EntityDescriptorDocument.Factory
							.parse(new ByteArrayInputStream(value.getLocal()));
				} catch (Exception e)
				{
					return ValidationResult.error(
							msg.getMessage("SAMLEditorGeneralTab.invalidMetadataFile"));
				}
			}

			boolean isEmpty = value == null || (value.getLocal() == null
					&& (value.getRemote() == null || value.getRemote().isEmpty()));

			if (publishMetadata.getValue() && (!autoGenerateMetadata.getValue() && isEmpty))
			{
				return ValidationResult.error(msg.getMessage("SAMLEditorGeneralTab.idpMetaEmpty"));
			}

			return ValidationResult.ok();

		}));
		metadataSource.setEnabled(false);
		metadataPublishing.addComponent(metadataSource);
		publishMetadata.addValueChangeListener(e -> {
			boolean v = e.getValue();
			signMetadata.setEnabled(v);
			autoGenerateMetadata.setEnabled(v);
			metadataSource.setEnabled(!autoGenerateMetadata.getValue() && v);
		});

		autoGenerateMetadata.addValueChangeListener(e -> {
			metadataSource.setEnabled(!e.getValue() && publishMetadata.getValue());
		});

		return new CollapsibleLayout(msg.getMessage("SAMLEditorGeneralTab.metadata"), metadataPublishing);
	}

	private CollapsibleLayout buildIdenityTypeMappingSection()
	{
		VerticalLayout idTypeMappingLayout = new VerticalLayout();
		idTypeMappingLayout.setMargin(false);

		GridWithEditor<SAMLIdentityMapping> idMappings = new GridWithEditor<>(msg, SAMLIdentityMapping.class);
		idTypeMappingLayout.addComponent(idMappings);
		idMappings.addComboColumn(s -> s.getUnityId(), (t, v) -> t.setUnityId(v),
				msg.getMessage("SAMLEditorGeneralTab.idMappings.unityId"),
				idTypes.stream().map(t -> t.getName()).collect(Collectors.toList()), 30, false);
		idMappings.addTextColumn(s -> s.getSamlId(), (t, v) -> t.setSamlId(v),
				msg.getMessage("SAMLEditorGeneralTab.idMappings.samlId"), 70, false);

		idMappings.setWidth(FieldSizeConstans.MEDIUM_FIELD_WIDTH, FieldSizeConstans.MEDIUM_FIELD_WIDTH_UNIT);
		configBinder.forField(idMappings).bind("identityMapping");

		return new CollapsibleLayout(msg.getMessage("SAMLEditorGeneralTab.idenityTypeMapping"),
				idTypeMappingLayout);
	}

	@Override
	public ServiceEditorTab getType()
	{
		return ServiceEditorTab.GENERAL;
	}

	@Override
	public CustomComponent getComponent()
	{
		return this;
	}

}
