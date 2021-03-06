/*
 * Copyright (c) 2016 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.attribute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.JsonUtil;
import pl.edu.icm.unity.store.impl.attributetype.AttributeTypeRDBMSStore;
import pl.edu.icm.unity.store.impl.groups.GroupRDBMSStore;
import pl.edu.icm.unity.store.rdbms.RDBMSObjectSerializer;
import pl.edu.icm.unity.store.types.StoredAttribute;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;


/**
 * Serializes {@link Attribute} to/from RDBMS {@link AttributeBean}.
 * @author K. Benedyczak
 */
@Component
public class AttributeRDBMSSerializer implements RDBMSObjectSerializer<StoredAttribute, AttributeBean>
{
	@Autowired
	private AttributeTypeRDBMSStore atDAO;
	@Autowired
	private GroupRDBMSStore groupDAO;
	
	@Override
	public AttributeBean toDB(StoredAttribute object)
	{
		AttributeBean bean = new AttributeBean();
		bean.setEntityId(object.getEntityId());
		long groupId = groupDAO.getKeyForName(object.getAttribute().getGroupPath());
		bean.setGroupId(groupId);
		long typeId = atDAO.getKeyForName(object.getAttribute().getName());
		bean.setTypeId(typeId);
		bean.setValues(JsonUtil.serialize2Bytes(object.getAttribute().toJsonBase()));
		return bean;
	}

	@Override
	public StoredAttribute fromDB(AttributeBean bean)
	{
		AttributeExt attr = new AttributeExt(bean.getName(), bean.getValueSyntaxId(), bean.getGroup(), 
				JsonUtil.parse(bean.getValues()));
		return new StoredAttribute(attr, bean.getEntityId());
	}
}
