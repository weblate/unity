/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.webadmin.msgtemplate;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

import pl.edu.icm.unity.base.msgtemplates.MessageTemplateDefinition;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.msgtemplate.MessageTemplateConsumersRegistry;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.MessageTemplate;
import pl.edu.icm.unity.webui.common.CompactFormLayout;
import pl.edu.icm.unity.webui.common.i18n.I18nLabelWithPreview;

/**
 * Component presenting a short information about message template.
 * 
 * @author P. Piernik
 */
public class SimpleMessageTemplateViewer extends CustomComponent
{
	private UnityMessageSource msg;
	private FormLayout main;
	private Label description;
	private Label consumer;

	private MessageTemplateConsumersRegistry registry;

	public SimpleMessageTemplateViewer(UnityMessageSource msg, MessageTemplateConsumersRegistry registry)
	{
		this.msg = msg;
		this.registry = registry;
		initUI();
	}

	private void initUI()
	{
		main = new CompactFormLayout();
		main.setMargin(false);
		main.setSpacing(false);
		setCompositionRoot(main);
		main.setMargin(false);
		main.setSpacing(true);
		description = new Label();
		description.setCaption(msg.getMessage("MessageTemplateViewer.description"));
		consumer = new Label();
		consumer.setCaption(msg.getMessage("MessageTemplateViewer.consumer"));
	}

	public void setInput(MessageTemplate template)
	{
		main.removeAllComponents();
		if (template == null)
		{
			return;
		}
		main.addComponent(description);
		main.addComponent(consumer);

		I18nString subjectContent = template.getMessage().getSubject();
		I18nString bodyContent = template.getMessage().getBody();

		main.setVisible(true);

		description.setValue(template.getDescription());

		String cons = template.getConsumer();
		if (cons != null)
		{
			try
			{
				MessageTemplateDefinition cn = registry.getByName(cons);
				consumer.setValue(msg.getMessage(cn.getDescriptionKey()));
			} catch (IllegalArgumentException e)
			{
				consumer.setValue(template.getConsumer());
			}
		}

		if (!subjectContent.isEmpty())
		{
			I18nLabelWithPreview subject = I18nLabelWithPreview
					.builder(msg, msg.getMessage("MessageTemplateViewer.subject"))
					.buildWithValue(subjectContent);
			main.addComponents(subject);
		}

		if (!bodyContent.isEmpty())
		{
			I18nLabelWithPreview body = I18nLabelWithPreview
					.builder(msg, msg.getMessage("MessageTemplateViewer.body"))
					.buildWithValue(bodyContent);
			main.addComponents(body);
		}
	}
}
