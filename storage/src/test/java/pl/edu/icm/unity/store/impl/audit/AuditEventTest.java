/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.icm.unity.JsonUtil;
import pl.edu.icm.unity.store.api.AuditEventDAO;
import pl.edu.icm.unity.store.impl.AbstractBasicDAOTest;
import pl.edu.icm.unity.store.tx.TransactionTL;
import pl.edu.icm.unity.types.basic.AuditEvent;
import pl.edu.icm.unity.types.basic.AuditEvent.AuditEntity;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuditEventTest extends AbstractBasicDAOTest<AuditEvent>
{
	@Autowired
	private AuditEventDAO dao;
	
	@Override
	protected AuditEventDAO getDAO()
	{
		return dao;
	}

	@Override
	protected AuditEvent getObject(String id)
	{
		AuditEvent event = new AuditEvent("name",
				"IDENTITY",
				new Date(),
				"ADD",
				JsonUtil.parse("{\"comment\" : \"No comment\"}"),
				new AuditEntity(101l, "Subject", "subject@example.com"),
				new AuditEntity(100l, "Initiator", "initiator@example.com"));
		return event;
	}

	@Override
	protected AuditEvent mutateObject(AuditEvent src)
	{
		AuditEvent event = new AuditEvent("name2",
				"ENTITY",
				new Date(),
				"UPDATE",
				JsonUtil.parse("{\"comment\" : \"No new comment\"}"),
				new AuditEntity(102l, "Subject2", "subject2@example.com"),
				src.getInitiator());
		return event;
	}
	
	@Override
	public void importExportIsIdempotent()
	{
		//REFIX what is that ? as of now events are not exported
	}

	@Test
	public void getAllEventsTest()
	{
		AuditEvent event1 = new AuditEvent("Identity name",
				"IDENTITY",
				new Date(),
				"ADD",
				JsonUtil.parse("{\"comment\" : \"No comment\"}"),
				new AuditEntity(101l, "Subject", "subject@example.com"),
				new AuditEntity(100l, "Initiator", "initiator@example.com"));
		AuditEvent event2 = new AuditEvent("Identity name",
				"IDENTITY",
				new Date(),
				"UPDATE",
				JsonUtil.parse("{\"comment\" : \"No comment\"}"),
				new AuditEntity(101l, "Subject", "subject@example.com"),
				new AuditEntity(100l, "Initiator", "initiator@example.com"));
		AuditEvent event3 = new AuditEvent("Group name",
				"GROUP",
				new Date(),
				"ADD",
				JsonUtil.parse("{\"comment\" : \"No comment\"}"),
				null,
				new AuditEntity(100l, "Initiator", "initiator@example.com"));

		tx.runInTransaction(() -> {
			dao.create(event1);
			dao.create(event2);
			dao.create(event3);

			TransactionTL.manualCommit();

			List<AuditEvent> events = dao.getAll();

			assertEquals(3, events.size());
			assertTrue(events.contains(event1));
			assertTrue(events.contains(event2));
			assertTrue(events.contains(event3));
			AuditEntity initiator = new AuditEntity(100l, "Initiator", "initiator@example.com");
			assertTrue(events.stream().allMatch((event) -> event.getInitiator().equals(initiator)));
		});
	}
}
