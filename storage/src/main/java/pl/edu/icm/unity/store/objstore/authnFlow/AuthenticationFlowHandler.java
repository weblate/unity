/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.objstore.authnFlow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pl.edu.icm.unity.exceptions.InternalException;
import pl.edu.icm.unity.store.impl.objstore.GenericObjectBean;
import pl.edu.icm.unity.store.objstore.DefaultEntityHandler;
import pl.edu.icm.unity.types.authn.AuthenticationFlowDefinition;

/**
 * Handler for {@link AuthenticationFlowDefinition}
 * @author P.Piernik
 */
@Component
public class AuthenticationFlowHandler extends DefaultEntityHandler<AuthenticationFlowDefinition>
{
	public static final String CREDENTIAL_OBJECT_TYPE = "authenticatorsFlow";
	
	@Autowired
	public AuthenticationFlowHandler(ObjectMapper jsonMapper)
	{
		super(jsonMapper, CREDENTIAL_OBJECT_TYPE, AuthenticationFlowDefinition.class);
	}

	@Override
	public GenericObjectBean toBlob(AuthenticationFlowDefinition value)
	{
		try
		{
			byte[] contents = jsonMapper.writeValueAsBytes(value);
			return new GenericObjectBean(value.getName(), contents, supportedType);
		} catch (JsonProcessingException e)
		{
			throw new InternalException("Can't serialize authenticators flow JSON", e);
		}
	}

	@Override
	public AuthenticationFlowDefinition fromBlob(GenericObjectBean blob)
	{
		try
		{
			return jsonMapper.readValue(blob.getContents(), AuthenticationFlowDefinition.class);
		} catch (Exception e)
		{
			throw new InternalException("Can't deserialize authenticators flow from JSON", e);
		}
	}
}
