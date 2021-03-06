/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.webconsole.directoryBrowser.groupbrowser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.CollapseEvent;
import com.vaadin.event.CollapseEvent.CollapseListener;
import com.vaadin.event.ExpandEvent;
import com.vaadin.event.ExpandEvent.ExpandListener;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.ui.Orientation;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.components.grid.GridDragSource;
import com.vaadin.ui.components.grid.MultiSelectionModel;
import com.vaadin.ui.components.grid.TreeGridDropTarget;
import com.vaadin.ui.renderers.HtmlRenderer;

import io.imunity.webadmin.directoryBrowser.GroupChangedEvent;
import io.imunity.webadmin.groupbrowser.GroupEditDialog;
import io.imunity.webadmin.groupbrowser.TreeNode;
import io.imunity.webconsole.directoryBrowser.identities.IdentitiesTreeGrid;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.exceptions.AuthorizationException;
import pl.edu.icm.unity.types.basic.Group;
import pl.edu.icm.unity.types.basic.GroupContents;
import pl.edu.icm.unity.webui.WebSession;
import pl.edu.icm.unity.webui.bus.EventsBus;
import pl.edu.icm.unity.webui.common.ConfirmWithOptionDialog;
import pl.edu.icm.unity.webui.common.DnDGridUtils;
import pl.edu.icm.unity.webui.common.EntityWithLabel;
import pl.edu.icm.unity.webui.common.GridSelectionSupport;
import pl.edu.icm.unity.webui.common.HamburgerMenu;
import pl.edu.icm.unity.webui.common.Images;
import pl.edu.icm.unity.webui.common.NotificationPopup;
import pl.edu.icm.unity.webui.common.SearchField;
import pl.edu.icm.unity.webui.common.SidebarStyles;
import pl.edu.icm.unity.webui.common.SingleActionHandler;
import pl.edu.icm.unity.webui.common.Styles;
import pl.edu.icm.unity.webui.common.Toolbar;
import pl.edu.icm.unity.webui.common.grid.FilterableGridHelper;
import pl.edu.icm.unity.webui.exceptions.ControllerException;

/**
 * Tree with groups obtained dynamically from the engine.
 * 
 * @author K. Benedyczak
 */

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupsTreeGrid extends TreeGrid<TreeNode>
{
	private UnityMessageSource msg;
	private EventsBus bus;
	private TreeData<TreeNode> treeData;
	private TreeDataProvider<TreeNode> dataProvider;
	private Toolbar<TreeNode> toolbar;
	private GroupBrowserController controller;

	@Autowired
	public GroupsTreeGrid(UnityMessageSource msg, GroupBrowserController controller)
	{

		this.msg = msg;
		this.controller = controller;

		addExpandListener(new GroupExpandListener());
		addCollapseListener(new GroupCollapseListener());
		setSelectionMode(SelectionMode.MULTI);
		GridSelectionSupport.installClickListener(this);
		((MultiSelectionModel<TreeNode>) getSelectionModel())
				.addMultiSelectionListener(event -> selectionChanged(event.getAllSelectedItems()));

		SearchField search = FilterableGridHelper.getRowSearchField(msg);
		search.addValueChangeListener(event -> {
			deselectAll();
			String searched = event.getValue();
			this.clearFilters();
			if (event.getValue() == null || event.getValue().isEmpty())
			{
				return;
			}
			this.addFilter(e -> e.anyFieldsOrChildContains(searched, msg, treeData));
		});

		toolbar = new Toolbar<>(Orientation.HORIZONTAL);
		addSelectionListener(toolbar.getSelectionListener());

		HamburgerMenu<TreeNode> hamburgerMenu = new HamburgerMenu<>();
		addSelectionListener(hamburgerMenu.getSelectionListener());

		SingleActionHandler<TreeNode> expandAllAction = getExpandAllAction();
		SingleActionHandler<TreeNode> collapseAllAction = getCollapseAllAction();
		
		SingleActionHandler<TreeNode> deleteAction = getDeleteAction();

		hamburgerMenu.addStyleName(SidebarStyles.sidebar.toString());
		hamburgerMenu.addActionHandler(expandAllAction);
		hamburgerMenu.addActionHandler(collapseAllAction);
		hamburgerMenu.addActionHandler(deleteAction);

		toolbar.addHamburger(hamburgerMenu);
		toolbar.addSearch(search, Alignment.BOTTOM_RIGHT);
		toolbar.setWidth(100, Unit.PERCENTAGE);

		this.bus = WebSession.getCurrent().getEventBus();

		treeData = new TreeData<>();
		dataProvider = new TreeDataProvider<>(treeData);
		setDataProvider(dataProvider);
		dataProvider.setSortComparator((g1, g2) -> g1.getPath().compareTo(g2.getPath()));

		addColumn(n -> n.getIcon() + " " + n.toString(), new HtmlRenderer()).setExpandRatio(10);
		addComponentColumn(n -> getRowHamburgerMenuComponent(Sets.newHashSet(n))).setExpandRatio(0);
		setHeaderVisible(false);
		setPrimaryStyleName(Styles.vGroupBrowser.toString());
		setRowHeight(34);

		setSizeFull();

		setupDragNDrop();

		try
		{
			loadNode("/", null);
			expand(treeData.getRootItems());
			if( treeData.getRootItems().size() > 0)
			{
				select(treeData.getRootItems().get(0));
			}

		} catch (ControllerException e)
		{
			// // this will show error node
			TreeNode parent = new TreeNode(msg, new Group("/"), Images.noAuthzGrp.getHtml());
			treeData.addItems(null, parent);
			dataProvider.refreshAll();
		}

	}

	
	
	
	private MenuBar getRowHamburgerMenuComponent(Set<TreeNode> target)
	{
		SingleActionHandler<TreeNode> addAction = getAddAction();
		SingleActionHandler<TreeNode> editAction = getEditAction();
		SingleActionHandler<TreeNode> editACAction = getEditACsAction();
		SingleActionHandler<TreeNode> editDelegationConfigAction = getEditDelegationConfigAction();

		SingleActionHandler<TreeNode> expandAllAction = getExpandAction();
		SingleActionHandler<TreeNode> collapseAllAction = getCollapseAction();
		SingleActionHandler<TreeNode> deleteAction = getDeleteAction();
		
		HamburgerMenu<TreeNode> menu = new HamburgerMenu<TreeNode>();
		menu.setTarget(target);
		menu.addActionHandlers(Arrays.asList(addAction, expandAllAction, collapseAllAction, deleteAction, editAction, editACAction, editDelegationConfigAction));
		menu.addStyleName(SidebarStyles.sidebar.toString());

		return menu;
	}

	@SuppressWarnings("unchecked")
	private void setupDragNDrop()
	{
		TreeGridDropTarget<TreeNode> dropTarget = new TreeGridDropTarget<>(this, DropMode.ON_TOP);
		dropTarget.setDropEffect(DropEffect.MOVE);
		dropTarget.setDropCriteriaScript(
				DnDGridUtils.getTypedCriteriaScript(IdentitiesTreeGrid.ENTITY_DND_TYPE));
		dropTarget.addGridDropListener(e -> {
			e.getDragSourceExtension().ifPresent(source -> {
				if (source instanceof GridDragSource && e.getDropTargetRow().isPresent()
						&& source.getDragData() != null)
				{
					Set<EntityWithLabel> dragData = (Set<EntityWithLabel>) source.getDragData();
					try
					{
						controller.bulkAddToGroup(e.getDropTargetRow().get(), dragData);
					} catch (ControllerException ex)
					{
						NotificationPopup.showError(msg, ex);
					}
				}
			});
		});
	}

	private void selectionChanged(Set<TreeNode> allSelectedItems)
	{
		final TreeNode node = getSingleSelection();
		bus.fireEvent(new GroupChangedEvent(node == null ? null : node.getPath()));
	}

	public Toolbar<TreeNode> getToolbar()
	{
		return toolbar;
	}

	void assetAuthorizationAuthorizationException(ControllerException e) throws ControllerException
	{
		if (!(e.getCause() instanceof AuthorizationException))
		{
			throw e;
		}

	}

	public void refresh()
	{
		for (TreeNode rootItem : treeData.getRootItems())
			refreshNode(rootItem);
	}

	private void loadNode(String path, TreeNode parent) throws ControllerException
	{
		Map<String, List<Group>> groupTree;

		groupTree = controller.getAllGroupWithSubgroups(path);

		List<Group> rootGrs = groupTree.get(null);
		if (rootGrs == null)
			return;
		for (Group rootGr : rootGrs)
		{
			TreeNode rootNode = new TreeNode(msg, rootGr, Images.folder_close.getHtml(), parent);
			treeData.addItem(parent, rootNode);
			addChilds(rootNode, groupTree);
		}
	}

	public void refreshNode(TreeNode node)// throws ControllerException
	{
		treeData.removeItem(node);
		try
		{
			loadNode(node.getPath(), node.getParentNode());
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
		}
		getDataProvider().refreshAll();

	}

	public List<TreeNode> getChildren(TreeNode node)
	{
		return treeData.getChildren(node);
	}

	private void addChilds(TreeNode parentNode, Map<String, List<Group>> groupTree)
	{
		for (Group child : groupTree.get(parentNode.getPath()))
		{
			TreeNode childNode = new TreeNode(msg, child, Images.folder_close.getHtml(), parentNode);
			treeData.addItem(parentNode, childNode);
			addChilds(childNode, groupTree);
		}
	}

	private TreeNode getSingleSelection()
	{	
		if (getSelectedItems() != null && getSelectedItems().size() == 1)
		{
			return getSelectedItems().iterator().next();
		} else
		{
			return null;
		}
	}

	private SingleActionHandler<TreeNode> getAddAction()
	{
		return SingleActionHandler.builder(TreeNode.class).withCaption(msg.getMessage("GroupsTree.createGroup"))
				.withIcon(Images.add.getResource()).withHandler(this::showAddDialog).build();
	}

	private void showAddDialog(Collection<TreeNode> target)
	{
		final TreeNode node = target.iterator().next();
		new GroupEditDialog(msg, new Group(node.getPath()), false, g -> {
			createGroup(g);
			refreshNode(node);
			expand(node);
		}).show();
	}

	private void createGroup(Group toBeCreated)
	{
		try
		{
			controller.addGroup(toBeCreated);
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
		}
	}

	private SingleActionHandler<TreeNode> getEditACsAction()
	{
		return SingleActionHandler.builder(TreeNode.class)
				.withCaption(msg.getMessage("GroupDetails.editACAction"))
				.withIcon(Images.attributes.getResource()).withHandler(this::showEditACsDialog).build();
	}

	private void showEditACsDialog(Collection<TreeNode> target)
	{
		final TreeNode node = target.iterator().next();
		controller.getGroupAttributesClassesDialog(node.getPath(), bus).show();
	}

	private SingleActionHandler<TreeNode> getEditAction()
	{
		return SingleActionHandler.builder4Edit(msg, TreeNode.class).withHandler(this::showEditDialog).build();
	}

	private void showEditDialog(Collection<TreeNode> target)
	{
		TreeNode node = target.iterator().next();
		Group group = resolveGroup(node);
		if (group == null)
			return;

		new GroupEditDialog(msg, group, true, g -> {
			updateGroup(node.getPath(), g);
			refreshNode(node.getParentNode());
			if (node.equals(getSingleSelection()))
				bus.fireEvent(new GroupChangedEvent(node.getPath()));
		}).show();

	}

	private void updateGroup(String path, Group group)
	{
		try
		{
			controller.updateGroup(path, group);
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
		}
	}

	private Group resolveGroup(TreeNode node)
	{
		Group group = null;
		try
		{
			group = controller.getGroupContent(node.getPath(), GroupContents.METADATA).getGroup();
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
		}
		return group;
	}

	private SingleActionHandler<TreeNode> getEditDelegationConfigAction()
	{
		return SingleActionHandler.builder(TreeNode.class)
				.withCaption(msg.getMessage("GroupsTree.editDelegationConfigAction"))
				.withIcon(Images.forward.getResource())
				.withHandler(this::showEditDelegationCondigDialog).build();
	}

	private void showEditDelegationCondigDialog(Collection<TreeNode> target)
	{
		TreeNode node = target.iterator().next();
		Group group = resolveGroup(node);
		if (group == null)
			return;

		controller.getGroupDelegationEditConfigDialog(bus, group, g -> updateGroup(node.getPath(), g)).show();
	}

	private SingleActionHandler<TreeNode> getDeleteAction()
	{
		return SingleActionHandler.builder4Delete(msg, TreeNode.class).withHandler(this::deleteHandler).build();
	}

	private void deleteHandler(Collection<TreeNode> items)
	{
		Set<TreeNode> realToRemove = getParentOnly(items);
		new ConfirmWithOptionDialog(msg,
				msg.getMessage("GroupRemovalDialog.confirmDelete",
						String.join(", ",
								realToRemove.stream().map(g -> g.toString())
										.collect(Collectors.toList()))),
				msg.getMessage("GroupRemovalDialog.recursive"), r -> removeGroups(realToRemove, r))
						.show();
	}

	private void removeGroups(Set<TreeNode> groups, boolean recursive)
	{
		try
		{
			controller.removeGroups(groups, recursive);
			deselectAll();
			groups.forEach(g -> {
				refreshNode(g.getParentNode());
			});
		} catch (ControllerException e)
		{
			NotificationPopup.showError(msg, e);
		}

	}

	private SingleActionHandler<TreeNode> getExpandAllAction()
	{
		return SingleActionHandler.builder(TreeNode.class)
				.withCaption(msg.getMessage("GroupsTree.expandAllGroupsAction")).dontRequireTarget()
				.withIcon(Images.expand.getResource())
				.withHandler(g -> expandItemsRecursively(getParentOnly(g))).build();
	}
	
	private SingleActionHandler<TreeNode> getExpandAction()
	{
		return SingleActionHandler.builder(TreeNode.class)
				.withCaption(msg.getMessage("GroupsTree.expandGroupAction")).dontRequireTarget()
				.withIcon(Images.expand.getResource())
				.withHandler(g -> expandItemsRecursively(g)).build();
	}

	private void expandItemsRecursively(Collection<TreeNode> items)
	{
		if (items.isEmpty())
		{
			expandItemsRecursively(treeData.getRootItems());
		}
		
		for (TreeNode node : items)
		{
			expand(node);
			for (TreeNode child : treeData.getChildren(node))
				expandItemsRecursively(Arrays.asList(child));
		}
	}

	private SingleActionHandler<TreeNode> getCollapseAllAction()
	{
		return SingleActionHandler.builder(TreeNode.class)
				.withCaption(msg.getMessage("GroupsTree.collapseAllGroupsAction")).dontRequireTarget()
				.withIcon(Images.collapse.getResource())
				.withHandler(g -> collapseItemsRecursively(getParentOnly(g))).build();
	}
	
	private SingleActionHandler<TreeNode> getCollapseAction()
	{
		return SingleActionHandler.builder(TreeNode.class)
				.withCaption(msg.getMessage("GroupsTree.collapseGroupAction")).dontRequireTarget()
				.withIcon(Images.collapse.getResource())
				.withHandler(g -> collapseItemsRecursively(g)).build();
	}

	private void collapseItemsRecursively(Collection<TreeNode> items)
	{
		if (items.isEmpty())
		{
			collapseItemsRecursively(treeData.getRootItems());
			expand(treeData.getRootItems());
		}
		
		for (TreeNode node : items)
		{
			collapse(node);
			for (TreeNode child : treeData.getChildren(node))
				collapseItemsRecursively(Arrays.asList(child));
		}
	}

	public void addFilter(SerializablePredicate<TreeNode> filter)
	{
		dataProvider.addFilter(filter);

	}

	public void clearFilters()
	{
		dataProvider.clearFilters();
	}

	private Set<TreeNode> getParentOnly(Collection<TreeNode> items)
	{
		Set<TreeNode> parents = new HashSet<>();
		for (TreeNode node : items)
		{
			boolean child = false;
			for (TreeNode potentialParent : items)
			{
				if (node.isChild(potentialParent))
				{
					child = true;
					break;
				}
			}

			if (!child)
			{
				parents.add(node);
			}
		}
		return parents;
	}

	private class GroupExpandListener implements ExpandListener<TreeNode>
	{

		@Override
		public void itemExpand(ExpandEvent<TreeNode> event)
		{
			event.getExpandedItem().setIcon(Images.folder_open.getHtml());

		}

	}

	private class GroupCollapseListener implements CollapseListener<TreeNode>
	{

		@Override
		public void itemCollapse(CollapseEvent<TreeNode> event)
		{
			event.getCollapsedItem().setIcon(Images.folder_close.getHtml());

		}

	}
}
