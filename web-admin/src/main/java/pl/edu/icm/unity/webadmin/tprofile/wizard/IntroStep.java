/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin.tprofile.wizard;

import org.vaadin.teemu.wizards.WizardStep;

import pl.edu.icm.unity.server.utils.UnityMessageSource;

import com.vaadin.ui.Component;

/**
 * First wizard step with introduction - used in {@link WizardDialogComponent}.
 * 
 * @author Roman Krysinski
 */
public class IntroStep implements WizardStep 
{

	public IntroStep(UnityMessageSource msg) 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCaption() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getContent() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onAdvance() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onBack() 
	{
		// TODO Auto-generated method stub
		return false;
	}

}
