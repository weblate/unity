/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

import org.springframework.stereotype.Component;
import pl.edu.icm.unity.JsonUtil;
import pl.edu.icm.unity.store.rdbms.RDBMSObjectSerializer;
import pl.edu.icm.unity.types.basic.AuditEvent;

import static java.util.Objects.isNull;

/**
 * Serializes {@link AuditEvent} to/from DB form.
 * @author K. Benedyczak
 */
@Component
public class AuditEventJsonSerializer implements RDBMSObjectSerializer<AuditEvent, AuditEventBean>
{
	@Override
	public AuditEventBean toDB(AuditEvent object)
	{
		AuditEventBean bean = new AuditEventBean(
				object.getName(),
				JsonUtil.serialize2Bytes(object.getJsonDetails()),
				object.getType(),
				object.getTimestamp(),
				isNull(object.getSubject()) ? null: object.getSubject().getEntityId(),
				isNull(object.getInitiator()) ? null: object.getInitiator().getEntityId(),
				object.getAction());
		return bean;
	}

	@Override
	public AuditEvent fromDB(AuditEventBean bean)
	{
		AuditEvent event = new AuditEvent(bean.getName(),
				bean.getType(),
				bean.getTimestamp(),
				bean.getAction(),
				JsonUtil.parse(bean.getContents()),
				isNull(bean.getSubjectId()) ? null : new AuditEvent.AuditEntity(bean.getSubjectId(), bean.getSubjectName(), bean.getSubjectEmail()),
				isNull(bean.getInitiatorId()) ? null : new AuditEvent.AuditEntity(bean.getInitiatorId(), bean.getInitiatorName(), bean.getInitiatorEmail())
				);
		return event;
	}

}
