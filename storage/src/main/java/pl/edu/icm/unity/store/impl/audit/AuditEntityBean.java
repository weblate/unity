/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

/**
 * In DB audit event representation.
 * @author R. Ledzinski
 */
public class AuditEntityBean
{
	private Long entityId;
	private String name;
	private String email;

	public AuditEntityBean(final Long entityId, final String name, final String email) {
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
}
