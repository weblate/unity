/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pl.edu.icm.unity.store.api.AuditEventDAO;
import pl.edu.icm.unity.store.rdbms.GenericRDBMSCRUD;
import pl.edu.icm.unity.store.rdbms.tx.SQLTransactionTL;
import pl.edu.icm.unity.types.basic.AuditEvent;

import static java.util.Objects.nonNull;


/**
 * RDBMS storage of {@link AuditEvent}
 * @author K. Benedyczak
 */
@Repository(AuditEventRDBMSStore.BEAN)
public class AuditEventRDBMSStore extends GenericRDBMSCRUD<AuditEvent, AuditEventBean>
					implements AuditEventDAO
{
	public static final String BEAN = DAO_ID + "rdbms";

	@Autowired
	public AuditEventRDBMSStore(AuditEventJsonSerializer jsonSerializer)
	{
		super(AuditEventMapper.class, jsonSerializer, NAME);
	}

	@Override
	public long create(AuditEvent obj)
	{
		if (nonNull(obj.getSubject()) && !isAuditEntityExists(obj.getSubject().getEntityId())) {
			createAuditEntity(new AuditEntityBean(obj.getSubject().getEntityId(),
					obj.getSubject().getName(),
					obj.getSubject().getEmail()));
		}

		if (nonNull(obj.getInitiator()) && !isAuditEntityExists(obj.getInitiator().getEntityId())) {
			createAuditEntity(new AuditEntityBean(obj.getInitiator().getEntityId(),
					obj.getInitiator().getName(),
					obj.getInitiator().getEmail()));
		}

		return super.create(obj);
	}

	@Override
	public void updateByKey(final long key, final AuditEvent obj) {
		if (nonNull(obj.getSubject()) && !isAuditEntityExists(obj.getSubject().getEntityId())) {
			createAuditEntity(new AuditEntityBean(obj.getSubject().getEntityId(),
					obj.getSubject().getName(),
					obj.getSubject().getEmail()));
		}

		if (nonNull(obj.getInitiator()) && !isAuditEntityExists(obj.getInitiator().getEntityId())) {
			createAuditEntity(new AuditEntityBean(obj.getInitiator().getEntityId(),
					obj.getInitiator().getName(),
					obj.getInitiator().getEmail()));
		}

		super.updateByKey(key, obj);
	}

	boolean isAuditEntityExists(long entityId)
	{
		AuditEventMapper mapper = SQLTransactionTL.getSql().getMapper(AuditEventMapper.class);
		return mapper.auditEntityExists(entityId);
	}

	long createAuditEntity(AuditEntityBean entityBean)
	{
		AuditEventMapper mapper = SQLTransactionTL.getSql().getMapper(AuditEventMapper.class);
		mapper.createAuditEntity(entityBean);
		return entityBean.getEntityId();
	}
}
