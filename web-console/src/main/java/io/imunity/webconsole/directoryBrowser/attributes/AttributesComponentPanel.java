/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.webconsole.directoryBrowser.attributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.imunity.webadmin.identities.EntityChangedEvent;
import pl.edu.icm.unity.engine.api.AttributesManagement;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.webui.WebSession;
import pl.edu.icm.unity.webui.bus.EventListener;
import pl.edu.icm.unity.webui.bus.EventsBus;
import pl.edu.icm.unity.webui.common.EntityWithLabel;
import pl.edu.icm.unity.webui.common.ErrorComponent;
import pl.edu.icm.unity.webui.common.ErrorComponent.Level;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.safehtml.SafePanel;
import pl.edu.icm.unity.webui.exceptions.ControllerException;

/**
 * Component used for displaying and managing attributes of a single entity.
 * 
 * @author P.Piernik
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AttributesComponentPanel extends SafePanel
{
	private UnityMessageSource msg;
	private AttributesGrid main;

	@Autowired
	public AttributesComponentPanel(UnityMessageSource msg, AttributesGrid main,
			AttributesManagement attributesManagement)
	{
		this.msg = msg;
		this.main = main;

		setStyleName(Styles.vPanelLight.toString());
		setSizeFull();

		EventsBus bus = WebSession.getCurrent().getEventBus();
		bus.addListener(new EventListener<EntityChangedEvent>()
		{
			@Override
			public void handleEvent(EntityChangedEvent event)
			{
				setInput(event.getEntity() == null ? null : event.getEntity(), event.getGroup());
			}
		}, EntityChangedEvent.class);
		setInput(null, "/");
	}

	private void setInput(EntityWithLabel owner, String groupPath)
	{
		if (owner == null)
		{
			setCaption(msg.getMessage("Attribute.captionNoEntity"));
			setProblem(msg.getMessage("Attribute.noEntitySelected"), Level.warning);
			return;
		}

		setCaption(msg.getMessage("Attribute.caption", owner, groupPath));
		try
		{

			main.setInput(owner, groupPath);
			setContent(main);
		} catch (ControllerException e)
		{
			setProblem(e.getMessage(), Level.error);
		}
	}

	private void setProblem(String message, Level level)
	{
		ErrorComponent errorC = new ErrorComponent();
		errorC.setMessage(message, level);
		setContent(errorC);
	}
}
