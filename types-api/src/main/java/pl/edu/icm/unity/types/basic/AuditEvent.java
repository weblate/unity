/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.types.basic;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.Objects;

/**
 * Holds information about single event that occur in the system.
 * @author R. Ledzinski
 */
public class AuditEvent
{
	private String name;
	private String type;
	private Date timestamp;
	private AuditEntity subject;
	private AuditEntity initiator;
	private String action;
	private JsonNode jsonDetails;

	public AuditEvent(@NonNull final String name, @NonNull final String type, @NonNull final Date timestamp, @NonNull final String action, final JsonNode jsonDetails,
					  final AuditEntity subject, @NonNull final AuditEntity initiator) {
		this.name = name;
		this.type = type;
		this.timestamp = timestamp;
		this.action = action;
		this.jsonDetails = jsonDetails;
		this.subject = subject;
		this.initiator = initiator;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	public AuditEntity getSubject() {
		return subject;
	}

	public void setSubject(final AuditEntity subject) {
		this.subject = subject;
	}

	public AuditEntity getInitiator() {
		return initiator;
	}

	public void setInitiator(final AuditEntity initiator) {
		this.initiator = initiator;
	}

	public String getAction() {
		return action;
	}

	public void setAction(final String action) {
		this.action = action;
	}

	public JsonNode getJsonDetails() {
		return jsonDetails;
	}

	public void setJsonDetails(final JsonNode jsonDetails) {
		this.jsonDetails = jsonDetails;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final AuditEvent that = (AuditEvent) o;
		return name.equals(that.name) &&
				type.equals(that.type) &&
				timestamp.equals(that.timestamp) &&
				Objects.equals(subject, that.subject) &&
				initiator.equals(that.initiator) &&
				action.equals(that.action) &&
				Objects.equals(jsonDetails, that.jsonDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, timestamp);
	}

	@Override
	public String toString() {
		return "AuditEvent{" +
				"name='" + name + '\'' +
				", type='" + type + '\'' +
				", timestamp=" + timestamp +
				", subject=" + subject +
				", initiator=" + initiator +
				", action='" + action + '\'' +
				", jsonDetails=" + jsonDetails +
				'}';
	}

	public static class AuditEntity {
		private Long entityId;
		private String name;
		private String email;

		public AuditEntity(@NonNull final Long entityId, @NonNull final String name, @NonNull final String email) {
			this.entityId = entityId;
			this.name = name;
			this.email = email;
		}

		public Long getEntityId() {
			return entityId;
		}

		public void setEntityId(final Long entityId) {
			this.entityId = entityId;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(final String email) {
			this.email = email;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final AuditEntity that = (AuditEntity) o;
			return entityId.equals(that.entityId) &&
					name.equals(that.name) &&
					email.equals(that.email);
		}

		@Override
		public int hashCode() {
			return Objects.hash(entityId);
		}

		@Override
		public String toString() {
			return "AuditEntity{" +
					"entityId=" + entityId +
					", name='" + name + '\'' +
					", email='" + email + '\'' +
					'}';
		}
	}
}
