package pl.edu.icm.unity.types.basic;

import org.junit.Test;
import pl.edu.icm.unity.JsonUtil;

import java.util.Arrays;
import java.util.Date;

public class AuditEventTest {

	@Test
	public void shouldPassValidation() {
		getEvent().assertValid();
	}

	@Test
	public void shouldBeValidWithAllFields() {
		AuditEvent event = getEvent();
		event.assertValid();
	}

	@Test
	public void shouldBeValidWithNulls() {
		AuditEvent event = getEvent();
		event.setSubject(null);
		event.setJsonDetails(null);
		event.setTags(null);
		event.assertValid();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullName() {
		AuditEvent event = getEvent();
		event.setName(null);
		event.assertValid();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullType() {
		AuditEvent event = getEvent();
		event.setType(null);
		event.assertValid();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullTimestamp() {
		AuditEvent event = getEvent();
		event.setTimestamp(null);
		event.assertValid();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullInitiator() {
		AuditEvent event = getEvent();
		event.setInitiator(null);
		event.assertValid();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullAuditEntityId() {
		AuditEvent event = getEvent();
		event.getInitiator().setEntityId(null);
		event.assertValid();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullAuditEntityInitatorName() {
		AuditEvent event = getEvent();
		event.getInitiator().setName(null);
		event.assertValid();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullAuditEntitySubjectName() {
		AuditEvent event = getEvent();
		event.getSubject().setName(null);
		event.assertValid();
	}


	private AuditEvent getEvent() {
		return new AuditEvent("Name", AuditEvent.EventType.IDENTITY, new Date(), AuditEvent.EventAction.ADD, JsonUtil.parse("{}"),
				new AuditEvent.AuditEntity(1l, "Subject name", "subject@example.com"),
				new AuditEvent.AuditEntity(0l, "Initiator name", "initiator@example.com"),
				Arrays.asList("T1", "T2"));
	}
}
