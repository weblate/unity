/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.engine.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.engine.api.attributes.AttributeSupport;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.store.api.AttributeTypeDAO;
import pl.edu.icm.unity.store.api.tx.Transactional;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.AttributeType;
import pl.edu.icm.unity.types.basic.EntityParam;

/**
 * Implementation of {@link AttributeSupport}
 * @author K. Benedyczak
 */
@Component
public class AttributeSupportImpl implements AttributeSupport
{
	private AttributeTypeDAO aTypeDAO;
	private AttributesHelper attributesHelper;
	
	@Autowired
	public AttributeSupportImpl(AttributesHelper attributesHelper, AttributeTypeDAO aTypeDAO)
	{
		this.attributesHelper = attributesHelper;
		this.aTypeDAO = aTypeDAO;
	}

	@Transactional
	@Override
	public AttributeType getAttributeTypeWithSingeltonMetadata(String metadataId)
			throws EngineException
	{
		return attributesHelper.getAttributeTypeWithSingeltonMetadata(metadataId);
	}

	@Transactional
	@Override
	public List<AttributeType> getAttributeTypeWithMetadata(String metadataId)
			throws EngineException
	{
		Collection<AttributeType> existingAts = aTypeDAO.getAll();
		List<AttributeType> ret = new ArrayList<>();
		for (AttributeType at: existingAts)
			if (at.getMetadata().containsKey(metadataId))
				ret.add(at);
		return ret;
	}
	
	@Transactional
	@Override
	public AttributeExt getAttributeByMetadata(EntityParam entity, String group,
			String metadataId) throws EngineException
	{
		entity.validateInitialization();
		return attributesHelper.getAttributeByMetadata(entity, group, metadataId);
	}

	@Transactional
	public Map<String, AttributeType> getAttributeTypesAsMap() throws EngineException
	{
		return aTypeDAO.getAllAsMap();
	}

}
