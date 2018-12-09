/*
 * Copyright (c) 2018 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.upman.members;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.imunity.upman.common.ServerFaultException;
import io.imunity.upman.utils.GroupIndentHelper;
import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.engine.api.GroupsManagement;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.engine.api.project.DelegatedGroup;
import pl.edu.icm.unity.engine.api.project.DelegatedGroupContents;
import pl.edu.icm.unity.engine.api.project.DelegatedGroupManagement;
import pl.edu.icm.unity.engine.api.project.DelegatedGroupMember;
import pl.edu.icm.unity.engine.api.project.GroupAuthorizationRole;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.webui.common.attributes.AttributeHandlerRegistry;
import pl.edu.icm.unity.webui.common.attributes.CachedAttributeHandlers;
import pl.edu.icm.unity.webui.exceptions.ControllerException;

/**
 * Group members controller
 * 
 * @author P.Piernik
 *
 */
@Component
public class GroupMembersController
{
	private static final Logger log = Log.getLogger(Log.U_SERVER, GroupMembersController.class);

	private DelegatedGroupManagement delGroupMan;
	private CachedAttributeHandlers cachedAttrHandlerRegistry;
	private UnityMessageSource msg;

	@Autowired
	public GroupMembersController(UnityMessageSource msg, GroupsManagement groupMan,
			AttributeHandlerRegistry attrHandlerRegistry, DelegatedGroupManagement delGroupMan)
	{
		this.msg = msg;
		this.delGroupMan = delGroupMan;
		this.cachedAttrHandlerRegistry = new CachedAttributeHandlers(attrHandlerRegistry);
	}

	public List<GroupMemberEntry> getGroupMembers(String projectPath, String groupPath) throws ControllerException
	{

		List<GroupMemberEntry> ret = new ArrayList<>();

		List<DelegatedGroupMember> members = null;
		try
		{
			members = delGroupMan.getDelegatedGroupMemebers(projectPath, groupPath);
		} catch (Exception e)
		{
			log.debug("Can not get memebers of group " + projectPath, e);
			throw new ServerFaultException(msg);
		}

		if (members == null || members.isEmpty())
			return ret;

		for (DelegatedGroupMember member : members)
		{
			Map<String, String> additionalAttributes = new HashMap<>();

			for (Attribute attr : member.attributes)
			{
				additionalAttributes.put(attr.getName(), cachedAttrHandlerRegistry
						.getSimplifiedAttributeValuesRepresentation(attr));
			}

			GroupMemberEntry entry = new GroupMemberEntry(member, additionalAttributes);
			ret.add(entry);
		}

		return ret;
	}

	public Map<String, String> getProjectIndentGroupsMap(String projectPath) throws ControllerException
	{
		Map<String, DelegatedGroupContents> groupAndSubgroups;
		try
		{
			groupAndSubgroups = delGroupMan.getGroupAndSubgroups(projectPath, projectPath);
		} catch (Exception e)
		{
			log.debug("Can not get group " + projectPath, e);
			throw new ServerFaultException(msg);
		}
		return GroupIndentHelper.getProjectIndentGroupsMap(projectPath, groupAndSubgroups);
	}

	public Map<String, String> getAdditionalAttributeNamesForProject(String projectPath) throws ControllerException
	{

		Map<String, String> attrs = new LinkedHashMap<>();
		try
		{
			DelegatedGroup group = delGroupMan.getContents(projectPath, projectPath).group;
			if (group == null)
				return attrs;

			List<String> groupAttrs = group.delegationConfiguration.attributes;

			if (groupAttrs == null || groupAttrs.isEmpty())
				return attrs;

			for (String attr : groupAttrs)
			{
				attrs.put(attr, delGroupMan.getAttributeDisplayedName(projectPath, attr));
			}
		} catch (Exception e)
		{
			log.debug("Can not get attribute names for project " + projectPath, e);
			throw new ServerFaultException(msg);
		}
		return attrs;
	}

	public void addToGroup(String projectPath, String groupPath, Set<GroupMemberEntry> items)
			throws ControllerException
	{
		List<String> added = new ArrayList<>();

		try
		{
			for (GroupMemberEntry member : items)
			{
				delGroupMan.addMemberToGroup(projectPath, groupPath, member.getEntityId());
				added.add(member.getName());
			}
		} catch (Exception e)
		{
			log.debug("Can not add member to group " + groupPath, e);
			if (added.isEmpty())
			{
				throw new ControllerException(msg.getMessage("GroupMembersController.addToGroupError"),
						msg.getMessage("GroupMembersController.notAdded"), null);
			} else
			{
				throw new ControllerException(msg.getMessage("GroupMembersController.addToGroupError"),
						msg.getMessage("GroupMembersController.partiallyAdded", added), null);
			}
		}
	}

	public void removeFromGroup(String projectPath, String groupPath, Set<GroupMemberEntry> items)
			throws ControllerException
	{
		List<String> removed = new ArrayList<>();

		try
		{
			for (GroupMemberEntry member : items)
			{
				delGroupMan.removeMemberFromGroup(projectPath, groupPath, member.getEntityId());
				removed.add(member.getName());

			}
		} catch (Exception e)
		{
			log.debug("Can not remove member from group " + groupPath, e);
			if (removed.isEmpty())
			{
				throw new ControllerException(
						msg.getMessage("GroupMembersController.removeFromGroupError"),
						msg.getMessage("GroupMembersController.notRemoved"), null);
			} else
			{
				throw new ControllerException(
						msg.getMessage("GroupMembersController.removeFromGroupError"),
						msg.getMessage("GroupMembersController.partiallyRemoved", removed),
						null);
			}
		}
	}

	public void addManagerPrivileges(String groupPath, Set<GroupMemberEntry> items) throws ControllerException
	{
		updatePrivileges(groupPath, GroupAuthorizationRole.manager, items);
	}

	public void revokeManagerPrivileges(String groupPath, Set<GroupMemberEntry> items) throws ControllerException
	{
		updatePrivileges(groupPath, GroupAuthorizationRole.regular, items);
	}

	private void updatePrivileges(String groupPath, GroupAuthorizationRole role, Set<GroupMemberEntry> items)
			throws ControllerException
	{

		List<String> updated = new ArrayList<>();

		try
		{
			for (GroupMemberEntry member : items)
			{
				delGroupMan.setGroupAuthorizationRole(groupPath, member.getEntityId(), role);
				updated.add(member.getName());
			}
		} catch (Exception e)
		{
			log.debug("Can not update group authorization role", e);
			if (updated.isEmpty())
			{
				throw new ControllerException(
						msg.getMessage("GroupMembersController.updatePrivilegesError"),
						msg.getMessage("GroupMembersController.notUpdated"), null);
			} else
			{
				throw new ControllerException(msg.getMessage("GroupMembersController.addToGroupError"),
						msg.getMessage("GroupMembersController.partiallyUpdated", updated),
						null);
			}
		}
	}
}