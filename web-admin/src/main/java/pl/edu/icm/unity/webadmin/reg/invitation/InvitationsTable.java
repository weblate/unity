/*
 * Copyright (c) 2015, Jirav All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.reg.invitation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.data.ValueProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.Orientation;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;

import io.imunity.webadmin.reg.invitations.InvitationEditor;
import io.imunity.webadmin.reg.invitations.InvitationEntry;
import io.imunity.webadmin.reg.invitations.InvitationSelectionListener;
import pl.edu.icm.unity.engine.api.AttributeTypeManagement;
import pl.edu.icm.unity.engine.api.EnquiryManagement;
import pl.edu.icm.unity.engine.api.GroupsManagement;
import pl.edu.icm.unity.engine.api.InvitationManagement;
import pl.edu.icm.unity.engine.api.MessageTemplateManagement;
import pl.edu.icm.unity.engine.api.RegistrationsManagement;
import pl.edu.icm.unity.engine.api.attributes.AttributeSupport;
import pl.edu.icm.unity.engine.api.bulk.BulkGroupQueryService;
import pl.edu.icm.unity.engine.api.bulk.GroupMembershipData;
import pl.edu.icm.unity.engine.api.bulk.GroupMembershipInfo;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.notification.NotificationProducer;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.exceptions.WrongArgumentException;
import pl.edu.icm.unity.stdext.utils.EntityNameMetadataProvider;
import pl.edu.icm.unity.types.basic.AttributeType;
import pl.edu.icm.unity.types.registration.EnquiryForm;
import pl.edu.icm.unity.types.registration.RegistrationForm;
import pl.edu.icm.unity.types.registration.invite.InvitationParam;
import pl.edu.icm.unity.webui.common.ComponentWithToolbar;
import pl.edu.icm.unity.webui.common.ConfirmDialog;
import pl.edu.icm.unity.webui.common.ErrorComponent;
import pl.edu.icm.unity.webui.common.GridContextMenuSupport;
import pl.edu.icm.unity.webui.common.GridSelectionSupport;
import pl.edu.icm.unity.webui.common.Images;
import pl.edu.icm.unity.webui.common.NotificationPopup;
import pl.edu.icm.unity.webui.common.SingleActionHandler;
import pl.edu.icm.unity.webui.common.SmallGrid;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.Toolbar;
import pl.edu.icm.unity.webui.common.attributes.AttributeHandlerRegistry;
import pl.edu.icm.unity.webui.common.identities.IdentityEditorRegistry;

/**
 * Table showing invitations, with actions.
 * @author Krzysztof Benedyczak
 */
public class InvitationsTable extends CustomComponent
{
	private UnityMessageSource msg;
	private Grid<InvitationEntry> invitationsTable;
	private RegistrationsManagement registrationManagement;
	private EnquiryManagement enquiryManagement;
	private InvitationManagement invitationManagement;
	private IdentityEditorRegistry identityEditorRegistry;
	private AttributeHandlerRegistry attrHandlersRegistry;
	private AttributeTypeManagement attributesManagement;
	private MessageTemplateManagement msgTemplateManagement;
	private GroupsManagement groupsManagement;
	private NotificationProducer notificationsProducer;
	private BulkGroupQueryService bulkQuery;
	private AttributeSupport attributeSupport;
	
	
	public InvitationsTable(UnityMessageSource msg,
			RegistrationsManagement registrationManagement,
			EnquiryManagement enquiryManagement,
			InvitationManagement invitationManagement,
			AttributeTypeManagement attributesManagement,
			IdentityEditorRegistry identityEditorRegistry,
			AttributeHandlerRegistry attrHandlersRegistry,
			MessageTemplateManagement msgTemplateManagement,
			GroupsManagement groupsManagement,
			NotificationProducer notificationsProducer,
			BulkGroupQueryService bulkQuery,
			AttributeSupport attributeSupport)
	{
		this.msg = msg;
		this.registrationManagement = registrationManagement;
		this.enquiryManagement = enquiryManagement;
		this.invitationManagement = invitationManagement;
		this.attributesManagement = attributesManagement;
		this.identityEditorRegistry = identityEditorRegistry;
		this.attrHandlersRegistry = attrHandlersRegistry;
		this.msgTemplateManagement = msgTemplateManagement;
		this.groupsManagement = groupsManagement;
		this.notificationsProducer = notificationsProducer;
		this.bulkQuery = bulkQuery;
		this.attributeSupport = attributeSupport;
		initUI();
	}

	private void initUI()
	{
		invitationsTable = new SmallGrid<>();
		invitationsTable.setSizeFull();
		invitationsTable.setSelectionMode(SelectionMode.MULTI);
		
		invitationsTable.addColumn(InvitationEntry::getType, ValueProvider.identity())
		.setCaption(msg.getMessage("InvitationsTable.type"))
		.setId("type");
		
		invitationsTable.addColumn(InvitationEntry::getForm, ValueProvider.identity())
			.setCaption(msg.getMessage("InvitationsTable.form"))
			.setId("form");
		invitationsTable.addColumn(InvitationEntry::getAddress, ValueProvider.identity())
			.setCaption(msg.getMessage("InvitationsTable.contactAddress"))
			.setId("contactAddress");
		invitationsTable.addColumn(InvitationEntry::getCode, ValueProvider.identity())
			.setCaption(msg.getMessage("InvitationsTable.code"))
			.setId("code");
		invitationsTable.addColumn(InvitationEntry::getExpiration, ValueProvider.identity())
			.setCaption(msg.getMessage("InvitationsTable.expiration"))
			.setStyleGenerator(invitation -> invitation.isExpired() ? Styles.error.toString() : null)
			.setId("expiration");
		
		invitationsTable.sort("contactAddress", SortDirection.ASCENDING);
		
		GridContextMenuSupport<InvitationEntry> contextMenu = new GridContextMenuSupport<>(invitationsTable);
		contextMenu.addActionHandler(getRefreshAction());
		contextMenu.addActionHandler(getAddAction());
		contextMenu.addActionHandler(getSendAction());
		contextMenu.addActionHandler(getDeleteAction());
		GridSelectionSupport.installClickListener(invitationsTable);
		
		Toolbar<InvitationEntry> toolbar = new Toolbar<>(Orientation.HORIZONTAL);
		invitationsTable.addSelectionListener(toolbar.getSelectionListener());
		toolbar.addActionHandlers(contextMenu.getActionHandlers());
		
		ComponentWithToolbar tableWithToolbar = new ComponentWithToolbar(invitationsTable, toolbar);
		tableWithToolbar.setSizeFull();
		
		setCompositionRoot(tableWithToolbar);
		refresh();
	}
	
	public void addValueChangeListener(final InvitationSelectionListener listener)
	{
		invitationsTable.addSelectionListener(event ->
		{
			InvitationEntry selected = getOnlyOneSelected();
			listener.invitationChanged(selected == null ? null : selected.invitationWithCode);
		});
	}
	
	private InvitationEntry getOnlyOneSelected()
	{
		Collection<InvitationEntry> beans = invitationsTable.getSelectedItems();
		return beans == null || beans.isEmpty() || beans.size() > 1 ? 
				null : ((InvitationEntry)beans.iterator().next());
	}
	
	private boolean addInvitation(InvitationParam invitation, boolean send)
	{
		String code;
		try
		{
			code = invitationManagement.addInvitation(invitation);
			refresh();
		} catch (Exception e)
		{
			String info = msg.getMessage("InvitationsTable.errorAdd");
			NotificationPopup.showError(msg, info, e);
			return false;
		}
		if (send)
		{
			try
			{
				invitationManagement.sendInvitation(code);
			} catch (EngineException e)
			{
				String info = msg.getMessage("InvitationsTable.errorSend");
				NotificationPopup.showError(msg, info, e);
			}
		}
		return true;
	}

	private void removeInvitation(Set<InvitationEntry> items)
	{
		try
		{
			for (InvitationEntry item: items)
			{
				invitationManagement.removeInvitation(item.getCode());
			}
			refresh();
		} catch (Exception e)
		{
			String info = msg.getMessage("InvitationsTable.errorDelete");
			NotificationPopup.showError(msg, info, e);
		}
	}


	private SingleActionHandler<InvitationEntry> getSendAction()
	{
		return SingleActionHandler.builder(InvitationEntry.class)
			.withCaption(msg.getMessage("InvitationsTable.sendCodeAction"))
			.withIcon(Images.messageSend.getResource())
			.multiTarget()
			.withHandler(this::sendInvitation)
			.build();
	}
	
	private void sendInvitation(Set<InvitationEntry> items)
	{
		try
		{
			for (InvitationEntry item: items)
			{
				invitationManagement.sendInvitation(item.getCode());
			}
			refresh();
		} catch (Exception e)
		{
			String info = msg.getMessage("InvitationsTable.errorSend");
			NotificationPopup.showError(msg, info, e);
		}
	}
	
	private Collection<EnquiryForm> getEnquiryForms() throws EngineException
	{
		return enquiryManagement.getEnquires();
	}
	
	private Collection<RegistrationForm> getRegistrationForms() throws EngineException
	{
		return registrationManagement.getForms();
	}
	
	private SingleActionHandler<InvitationEntry> getRefreshAction()
	{
		return SingleActionHandler
			.builder4Refresh(msg, InvitationEntry.class)
			.withHandler(selection -> refresh())
			.build();
	}
	
	void refresh()
	{
		try
		{
			InvitationEntry selected = getOnlyOneSelected();
			List<InvitationEntry> invitations = invitationManagement.getInvitations()
					.stream()
					.map(invitation -> new InvitationEntry(msg, invitation))
					.collect(Collectors.toList());
			invitationsTable.setItems(invitations);
			if (selected != null)
			{
				String selectedCode = selected.getCode();
				invitations.stream()
					.filter(invitation -> selectedCode.equals(invitation.getCode()))
					.findFirst()
					.ifPresent(invitation -> invitationsTable.select(invitation));
			}
		} catch (Exception e)
		{
			ErrorComponent error = new ErrorComponent();
			error.setError(msg.getMessage("InvitationsTable.errorGetInvitations"), e);
			setCompositionRoot(error);
		}
	}
	
	private SingleActionHandler<InvitationEntry> getAddAction()
	{
		return SingleActionHandler.builder(InvitationEntry.class)
			.withCaption(msg.getMessage("InvitationsTable.addInvitationAction"))
			.withIcon(Images.add.getResource())
			.dontRequireTarget()
			.withHandler(this::handleAdd)
			.build();
	}

	private void handleAdd(Set<InvitationEntry> items)
	{
		InvitationEditor editor;
		try
		{
			editor = new InvitationEditor(msg, identityEditorRegistry, attrHandlersRegistry,
					msgTemplateManagement.listTemplates(), getRegistrationForms(),
					getEnquiryForms(), attributesManagement.getAttributeTypesAsMap(),
					notificationsProducer, getEntities(), getNameAttribute(), groupsManagement.getGroupsByWildcard("/**"),
					msgTemplateManagement);
		} catch (WrongArgumentException e)
		{
			NotificationPopup.showError(msg.getMessage("InvitationsTable.noValidForms"), 
					msg.getMessage("InvitationsTable.noValidFormsDesc"));
			return;
		} catch (EngineException e)
		{
			NotificationPopup.showError(msg, msg.getMessage("InvitationsTable.errorGetData"), e);
			return;
		}
		InvitationEditDialog dialog = new InvitationEditDialog(msg, 
				msg.getMessage("InvitationsTable.addInvitationAction"), editor, 
				(invitation, sendInvitation) -> addInvitation(invitation, sendInvitation));
		dialog.show();
	}

	
	private Map<Long, GroupMembershipInfo> getEntities() throws EngineException
	{
		GroupMembershipData bulkMembershipData = bulkQuery.getBulkMembershipData("/");
		return bulkQuery.getMembershipInfo(bulkMembershipData);
	}
	
	private String getNameAttribute() throws EngineException
	{
		AttributeType type = attributeSupport.getAttributeTypeWithSingeltonMetadata(EntityNameMetadataProvider.NAME);
		if (type == null)
			return null;
		return type.getName();
	}
	
	private SingleActionHandler<InvitationEntry> getDeleteAction()
	{
		return SingleActionHandler.builder4Delete(msg, InvitationEntry.class)
			.withHandler(this::handleDelete)
			.build();
	}
	
	public void handleDelete(Set<InvitationEntry> items)
	{
		new ConfirmDialog(msg, msg.getMessage(
				"InvitationsTable.confirmDelete", items.size()),
				new ConfirmDialog.Callback()
				{
					@Override
					public void onConfirm()
					{
						removeInvitation(items);
					}
				}).show();
	}
}
