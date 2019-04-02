/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

import org.apache.ibatis.annotations.Param;
import pl.edu.icm.unity.store.rdbms.BasicCRUDMapper;

import java.util.List;


/**
 * Access to the AuditEvent.xml operations.
 * @author R.Ledzinski
 */
public interface AuditEventMapper extends BasicCRUDMapper<AuditEventBean>
{
	boolean auditEntityExists(long id);
	long createAuditEntity(AuditEntityBean bean);
	List<String> getTags(long eventId);
	void insertTags(@Param("eventId") long eventId, @Param("tagList") List<String> tagList);
}
