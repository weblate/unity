/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

import pl.edu.icm.unity.store.rdbms.BasicCRUDMapper;


/**
 * Access to the AuditEvent.xml operations.
 * @author R.Ledzinski
 */
public interface AuditEventMapper extends BasicCRUDMapper<AuditEventBean>
{
	boolean auditEntityExists(long id);
	long createAuditEntity(AuditEntityBean bean);
}
