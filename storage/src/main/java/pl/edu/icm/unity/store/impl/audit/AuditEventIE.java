/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.store.api.AttributeTypeDAO;
import pl.edu.icm.unity.store.api.AuditEventDAO;
import pl.edu.icm.unity.store.export.AbstractIEBase;
import pl.edu.icm.unity.types.basic.AttributeType;
import pl.edu.icm.unity.types.basic.AuditEvent;

import java.util.List;

/**
 * Handles import/export of attribute types table.
 * @author R. Ledzinski
 */
@Component
public class AuditEventIE extends AbstractIEBase<AuditEvent>
{
	private static final Logger log = Log.getLegacyLogger(Log.U_SERVER_CFG, AuditEventIE.class);
	private AuditEventDAO dao;

	@Autowired
	public AuditEventIE(AuditEventDAO dao)
	{
		super(7, "auditEvents");
		this.dao = dao;
	}
	
	@Override
	protected List<AuditEvent> getAllToExport()
	{
		return dao.getAll();
	}

	@Override
	protected ObjectNode toJsonSingle(AuditEvent exportedObj)
	{
		return exportedObj.toJson();
	}

	@Override
	protected void createSingle(AuditEvent toCreate)
	{
		dao.create(toCreate);
	}

	@Override
	protected AuditEvent fromJsonSingle(ObjectNode src)
	{
		try {
			return new AuditEvent(src);
		} catch (JsonProcessingException e) {
			log.error("Failed to deserialize AuditEvent object:", e);
		}
		return null;
	}
}








