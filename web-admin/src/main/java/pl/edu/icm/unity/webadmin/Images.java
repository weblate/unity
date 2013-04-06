/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webadmin;

import com.vaadin.server.ClassResource;
import com.vaadin.server.Resource;

/**
 * Allows to easily get image resources.
 * @author K. Benedyczak
 */
public enum Images
{
	add		(I.P + "add.png"),
	delete		(I.P + "delete.png"),
	refresh		(I.P + "refresh.png"),
	noAuthzGrp	(I.P + "noauthzGrp.gif"),
	collapse	(I.P + "collapse.gif"),
	folder		(I.P + "folder.gif");
	
	
	private final String classpath;
	
	private Images(String classpath)
	{
		this.classpath = classpath;
	}
	
	public Resource getResource()
	{
		return new ClassResource(classpath);
	}

	/**
	 * Trick - otherwise we won't be able to use P in enum constructor arguments
	 * @author K. Benedyczak
	 */
	private static interface I
	{
		public static final String P = "/pl/edu/icm/unity/webadmin/img/"; 
	}
}
