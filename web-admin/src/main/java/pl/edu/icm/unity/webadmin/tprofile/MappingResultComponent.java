/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.tprofile;

import java.util.Collections;
import java.util.List;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;

import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.translation.in.MappedAttribute;
import pl.edu.icm.unity.engine.api.translation.in.MappedGroup;
import pl.edu.icm.unity.engine.api.translation.in.MappedIdentity;
import pl.edu.icm.unity.engine.api.translation.in.MappingResult;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.safehtml.HtmlLabel;

/**
 * Component that displays Mapping Result.
 * 
 * @author Roman Krysinski
 */
public class MappingResultComponent extends CustomComponent 
{

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private VerticalLayout mappingResultWrap;
	@AutoGenerated
	private VerticalLayout groupsWrap;
	@AutoGenerated
	private Label groupsTitleLabel;
	@AutoGenerated
	private VerticalLayout attrsWrap;
	@AutoGenerated
	private Table attrsTable;
	private Table groupsTable;
	@AutoGenerated
	private Label attrsTitleLabel;
	@AutoGenerated
	private VerticalLayout idsWrap;
	@AutoGenerated
	private Table idsTable;
	@AutoGenerated
	private Label idsTitleLabel;
	@AutoGenerated
	private HorizontalLayout titleWrap;
	@AutoGenerated
	private Label noneLabel;
	@AutoGenerated
	private HtmlLabel titleLabel;
	private UnityMessageSource msg;
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 * @param msg 
	 */
	public MappingResultComponent(UnityMessageSource msg) 
	{
		this.msg = msg;

		buildMainLayout();
		setCompositionRoot(mainLayout);
		
		mappingResultWrap.setStyleName(Styles.smallMargin.toString());
		setVisible(false);
		initLabels();
		initTables();
	}

	private void initLabels() 
	{
		titleLabel.setHtmlValue("MappingResultComponent.title");
		idsTitleLabel.setValue(msg.getMessage("MappingResultComponent.idsTitle"));
		attrsTitleLabel.setValue(msg.getMessage("MappingResultComponent.attrsTitle"));
		groupsTitleLabel.setValue(msg.getMessage("MappingResultComponent.groupsTitle"));
		noneLabel.setValue(msg.getMessage("MappingResultComponent.none"));
	}
	
	private void initTables() 
	{
		idsTable.addContainerProperty(msg.getMessage("MappingResultComponent.mode"), String.class, null);
		idsTable.addContainerProperty(msg.getMessage("MappingResultComponent.idsTable.type"), String.class, null);
		idsTable.addContainerProperty(msg.getMessage("MappingResultComponent.idsTable.value"), String.class, null);
		
		attrsTable.addContainerProperty(msg.getMessage("MappingResultComponent.mode"), String.class, null);
		attrsTable.addContainerProperty(msg.getMessage("MappingResultComponent.attrsTable.name"), String.class, null);
		attrsTable.addContainerProperty(msg.getMessage("MappingResultComponent.attrsTable.value"), String.class, null);
		
		groupsTable.addContainerProperty(msg.getMessage("MappingResultComponent.mode"), String.class, null);
		groupsTable.addContainerProperty(msg.getMessage("MappingResultComponent.groupsTable.group"), String.class, null);
	}
	

	public void displayMappingResult(MappingResult mappingResult, String inputTranslationProfile) 
	{
		titleLabel.setHtmlValue("DryRun.MappingResultComponent.title", inputTranslationProfile);
		noneLabel.setValue("");
		displayMappingResult(mappingResult);
	}	
	
	public void displayMappingResult(MappingResult mappingResult)
	{
		if (mappingResult == null 
				|| (mappingResult.getIdentities().isEmpty()
					&& mappingResult.getAttributes().isEmpty()
					&& mappingResult.getGroups().isEmpty()))
		{
			displayItsTables(Collections.<MappedIdentity>emptyList());
			displayAttrsTable(Collections.<MappedAttribute>emptyList());
			displayGroups(Collections.<MappedGroup>emptyList());
			noneLabel.setVisible(true);
		} else
		{
			displayItsTables(mappingResult.getIdentities());
			displayAttrsTable(mappingResult.getAttributes());
			displayGroups(mappingResult.getGroups());
			noneLabel.setVisible(false);
		}
		setVisible(true);
	}

	private void displayItsTables(List<MappedIdentity> identities) 
	{
		idsTable.removeAllItems();
		if (identities.isEmpty())
		{
			idsWrap.setVisible(false);
		} else
		{
			idsWrap.setVisible(true);
			for (int i=0; i < identities.size(); ++i)
			{
				MappedIdentity identity = identities.get(i);
				idsTable.addItem(new Object[] {
								identity.getMode().toString(),
								identity.getIdentity().getTypeId().toString(),
								identity.getIdentity().getValue().toString()}, i);
			}
			
			idsTable.setPageLength(identities.size());
	
			idsTable.refreshRowCache();
		}
	}
	
	private void displayAttrsTable(List<MappedAttribute> attributes) 
	{
		attrsTable.removeAllItems();
		if (attributes.isEmpty())
		{
			attrsWrap.setVisible(false);
		} else
		{
			attrsWrap.setVisible(true);
			for (int i=0; i < attributes.size(); ++i)
			{
				MappedAttribute attr = attributes.get(i);
				attrsTable.addItem(new Object[] {
						attr.getMode().toString(),
						attr.getAttribute().getName().toString(),
						attr.getAttribute().getValues().toString()}, i);
			}
			attrsTable.setPageLength(attributes.size());
			attrsTable.refreshRowCache();
		}
	}
	
	private void displayGroups(List<MappedGroup> groups) 
	{
		if (groups.isEmpty())
		{
			groupsWrap.setVisible(false);
		} else
		{
			groupsWrap.setVisible(true);
			for (int i=0; i < groups.size(); ++i)
			{
				MappedGroup mg = groups.get(i);
				groupsTable.addItem(new Object[] {
						mg.getCreateIfMissing().toString(),
						mg.getGroup()} , 
						i);
			}
			groupsTable.setPageLength(groups.size());
			groupsTable.refreshRowCache();
		}
	}
	
	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// titleWrap
		titleWrap = buildTitleWrap();
		mainLayout.addComponent(titleWrap);
		
		// mappingResultWrap
		mappingResultWrap = buildMappingResultWrap();
		mainLayout.addComponent(mappingResultWrap);
		mainLayout.setExpandRatio(mappingResultWrap, 1.0f);
		
		return mainLayout;
	}


	@AutoGenerated
	private HorizontalLayout buildTitleWrap() {
		// common part: create layout
		titleWrap = new HorizontalLayout();
		titleWrap.setWidth("-1px");
		titleWrap.setHeight("-1px");
		titleWrap.setMargin(false);
		titleWrap.setSpacing(true);
		
		// titleLabel
		titleLabel = new HtmlLabel(msg);
		titleLabel.setImmediate(false);
		titleLabel.setWidth("-1px");
		titleLabel.setHeight("-1px");
		titleWrap.addComponent(titleLabel);
		
		// noneLabel
		noneLabel = new Label();
		noneLabel.setImmediate(false);
		noneLabel.setWidth("-1px");
		noneLabel.setHeight("-1px");
		noneLabel.setValue("Label");
		titleWrap.addComponent(noneLabel);
		
		return titleWrap;
	}


	@AutoGenerated
	private VerticalLayout buildMappingResultWrap() {
		// common part: create layout
		mappingResultWrap = new VerticalLayout();
		mappingResultWrap.setWidth("100.0%");
		mappingResultWrap.setHeight("-1px");
		mappingResultWrap.setMargin(true);
		mappingResultWrap.setSpacing(true);
		
		// idsWrap
		idsWrap = buildIdsWrap();
		mappingResultWrap.addComponent(idsWrap);
		
		// attrsWrap
		attrsWrap = buildAttrsWrap();
		mappingResultWrap.addComponent(attrsWrap);
		
		// groupsWrap
		groupsWrap = buildGroupsWrap();
		mappingResultWrap.addComponent(groupsWrap);
		
		return mappingResultWrap;
	}


	@AutoGenerated
	private VerticalLayout buildIdsWrap() {
		// common part: create layout
		idsWrap = new VerticalLayout();
		idsWrap.setWidth("100.0%");
		idsWrap.setHeight("-1px");
		idsWrap.setMargin(false);
		
		// idsTitleLabel
		idsTitleLabel = new Label();
		idsTitleLabel.setImmediate(false);
		idsTitleLabel.setWidth("-1px");
		idsTitleLabel.setHeight("-1px");
		idsTitleLabel.setValue("Label");
		idsWrap.addComponent(idsTitleLabel);
		
		// idsTable
		idsTable = new Table();
		idsTable.setImmediate(false);
		idsTable.setWidth("100.0%");
		idsTable.setHeight("-1px");
		idsWrap.addComponent(idsTable);
		
		return idsWrap;
	}


	@AutoGenerated
	private VerticalLayout buildAttrsWrap() {
		// common part: create layout
		attrsWrap = new VerticalLayout();
		attrsWrap.setWidth("100.0%");
		attrsWrap.setHeight("-1px");
		attrsWrap.setMargin(false);
		
		// attrsTitleLabel
		attrsTitleLabel = new Label();
		attrsTitleLabel.setImmediate(false);
		attrsTitleLabel.setWidth("-1px");
		attrsTitleLabel.setHeight("-1px");
		attrsTitleLabel.setValue("Label");
		attrsWrap.addComponent(attrsTitleLabel);
		
		// attrsTable
		attrsTable = new Table();
		attrsTable.setImmediate(false);
		attrsTable.setWidth("100.0%");
		attrsTable.setHeight("-1px");
		attrsWrap.addComponent(attrsTable);
		
		return attrsWrap;
	}


	@AutoGenerated
	private VerticalLayout buildGroupsWrap() {
		// common part: create layout
		groupsWrap = new VerticalLayout();
		groupsWrap.setWidth("100.0%");
		groupsWrap.setHeight("-1px");
		groupsWrap.setMargin(false);
		
		// groupsTitleLabel
		groupsTitleLabel = new Label();
		groupsTitleLabel.setImmediate(false);
		groupsTitleLabel.setWidth("-1px");
		groupsTitleLabel.setHeight("-1px");
		groupsTitleLabel.setValue("Label");
		groupsWrap.addComponent(groupsTitleLabel);
		
		// groupsLabel
		groupsTable = new Table();
		groupsTable.setImmediate(false);
		groupsTable.setWidth("100.0%");
		groupsTable.setHeight("-1px");
		groupsWrap.addComponent(groupsTable);
		
		return groupsWrap;
	}
}
