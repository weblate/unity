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

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

/**
 * RDBMS storage of {@link AuditEvent}
 * @author R. Ledzinski
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
		obj.assertValid();
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

		long id = super.create(obj);
		if (obj.getTags() != null && obj.getTags().size() > 0) {
			insertTags(id, obj.getTags());
		}

		return id;
	}

	@Override
	public void updateByKey(final long key, final AuditEvent obj) {
		obj.assertValid();
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

	@Override
	public AuditEvent getByKey(final long id) {
		AuditEvent event = super.getByKey(id);
		event.setTags(getTags(id));
		return event;
	}

	@Override
	public List<AuditEvent> getAll() {
		AuditEventMapper mapper = SQLTransactionTL.getSql().getMapper(AuditEventMapper.class);
		List<AuditEventBean> allInDB = mapper.getAll();
		List<AuditEvent> ret = new ArrayList<>(allInDB.size());
		for(AuditEventBean bean : allInDB)
		{
			AuditEvent event = jsonSerializer.fromDB(bean);
			event.setTags(getTags(bean.getId()));
			ret.add(event);
		}
		return ret;
	}

	private boolean isAuditEntityExists(long entityId)
	{
		AuditEventMapper mapper = SQLTransactionTL.getSql().getMapper(AuditEventMapper.class);
		return mapper.auditEntityExists(entityId);
	}

	private long createAuditEntity(AuditEntityBean entityBean)
	{
		AuditEventMapper mapper = SQLTransactionTL.getSql().getMapper(AuditEventMapper.class);
		mapper.createAuditEntity(entityBean);
		return entityBean.getEntityId();
	}

	private List<String> getTags(long eventId)
	{
		AuditEventMapper mapper = SQLTransactionTL.getSql().getMapper(AuditEventMapper.class);
		return mapper.getTags(eventId);
	}

	private void insertTags(long eventId, List<String> tagList)
	{
		AuditEventMapper mapper = SQLTransactionTL.getSql().getMapper(AuditEventMapper.class);
		mapper.insertTags(eventId, tagList);
	}
}
