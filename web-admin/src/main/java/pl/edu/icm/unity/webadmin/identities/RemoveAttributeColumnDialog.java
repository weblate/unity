/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.identities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.webui.common.AbstractDialog;
import pl.edu.icm.unity.webui.common.CompactFormLayout;

/**
 * Allows to choose an attribute type to be removed from identities table columns.
 * @author K. Benedyczak
 */
public class RemoveAttributeColumnDialog extends AbstractDialog
{
	protected Callback callback;
	private Set<String> alreadyUsedInRoot;
	private Set<String> alreadyUsedInCurrent;
	private String currentGroup;
	private Map<String, String> labelsToAttr;
	
	private ComboBox<String> attributeType;
	
	
	public RemoveAttributeColumnDialog(UnityMessageSource msg, Set<String> alreadyUsedInRoot, 
			Set<String> alreadyUsedInCurrent, String currentGroup, Callback callback)
	{
		super(msg, msg.getMessage("RemoveAttributeColumnDialog.caption"));
		this.alreadyUsedInCurrent = alreadyUsedInCurrent;
		this.alreadyUsedInRoot = alreadyUsedInRoot;
		this.callback = callback;
		this.currentGroup = currentGroup;
		setSizeMode(SizeMode.SMALL);
	}

	@Override
	protected FormLayout getContents()
	{
		labelsToAttr = new HashMap<>();
		Label info = new Label(msg.getMessage("RemoveAttributeColumnDialog.info"));
		attributeType = new ComboBox<>(msg.getMessage("RemoveAttributeColumnDialog.attribute"));
		List<String> values = new ArrayList<>();
		for (String at: alreadyUsedInRoot)
		{
			String key = at + "@/";
			values.add(key);
			labelsToAttr.put(key, at + "@//" );
		}
		for (String at: alreadyUsedInCurrent)
		{
			String key = at + "@" + currentGroup;
			values.add(key);
			labelsToAttr.put(key, at + "@/" + currentGroup );
		}
		attributeType.setItems(values);
		if (alreadyUsedInRoot.size() > 0)
			attributeType.setSelectedItem(alreadyUsedInRoot.iterator().next() + "@/");
		else if (alreadyUsedInCurrent.size() > 0)
			attributeType.setSelectedItem(alreadyUsedInCurrent.iterator().next() + "@" + currentGroup);

		attributeType.setEmptySelectionAllowed(false);
		FormLayout main = new CompactFormLayout();
		main.addComponents(info, attributeType);
		main.setSizeFull();
		return main;
	}

	@Override
	protected void onConfirm()
	{
		String selected = (String)attributeType.getValue();
		if (selected == null)
		{
			close();
			return;
		}
		String parsable = labelsToAttr.get(selected);
		int split = parsable.lastIndexOf("@/");
		String group = parsable.substring(split+2);
		String attr = parsable.substring(0, split);
		callback.onChosen(attr, group);
		close();
	}
	
	public interface Callback 
	{
		public void onChosen(String attributeType, String group);
	}
}
