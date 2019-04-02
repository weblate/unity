/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.store.impl.audit;

import pl.edu.icm.unity.store.rdbms.BaseBean;

import java.util.Date;
import java.util.List;


/**
 * In DB audit event representation.
 * @author R. Ledzinski
 */
public class AuditEventBean extends BaseBean
{
	private String type;
	private Date timestamp;
	private Long subjectId;
	private String subjectName;
	private String subjectEmail;
	private Long initiatorId;
	private String initiatorName;
	private String initiatorEmail;
	private String action;
	private List<String> tags;

	public AuditEventBean(final String name, final byte[] contents, final String type, final Date timestamp, final Long subjectId, final Long initiatorId, final String action) {
		super(name, contents);
		this.type = type;
		this.timestamp = timestamp;
		this.subjectId = subjectId;
		this.initiatorId = initiatorId;
		this.action = action;
	}

	public AuditEventBean() {
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

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(final Long subjectId) {
		this.subjectId = subjectId;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(final String subjectName) {
		this.subjectName = subjectName;
	}

	public String getSubjectEmail() {
		return subjectEmail;
	}

	public void setSubjectEmail(final String subjectEmail) {
		this.subjectEmail = subjectEmail;
	}

	public Long getInitiatorId() {
		return initiatorId;
	}

	public void setInitiatorId(final Long initiatorId) {
		this.initiatorId = initiatorId;
	}

	public String getInitiatorName() {
		return initiatorName;
	}

	public void setInitiatorName(final String initiatorName) {
		this.initiatorName = initiatorName;
	}

	public String getInitiatorEmail() {
		return initiatorEmail;
	}

	public void setInitiatorEmail(final String initiatorEmail) {
		this.initiatorEmail = initiatorEmail;
	}

	public String getAction() {
		return action;
	}

	public void setAction(final String action) {
		this.action = action;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(final List<String> tags) {
		this.tags = tags;
	}
}
