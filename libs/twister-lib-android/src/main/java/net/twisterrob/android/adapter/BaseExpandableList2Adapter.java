package net.twisterrob.android.adapter;

import java.util.*;

import android.content.Context;
import android.view.*;

public abstract class BaseExpandableList2Adapter<Group, Child, GroupVH, ChildVH>
		extends android.widget.BaseExpandableListAdapter {
	protected final Context m_context;
	protected final LayoutInflater m_inflater;
	private List<Group> m_groups;
	private Map<Group, ? extends List<Child>> m_children;

	public BaseExpandableList2Adapter(Context context, Collection<Group> groups,
			Map<Group, ? extends List<Child>> children) {
		this.m_context = context;
		this.m_inflater = LayoutInflater.from(m_context);
		this.m_groups = groups instanceof List? (List<Group>)groups : new ArrayList<>(groups);
		this.m_children = children;
	}

	public void setGroups(List<Group> groups) {
		m_groups = groups != null? groups : new ArrayList<Group>();
	}
	public void setChildren(Map<Group, ? extends List<Child>> children) {
		m_children = children != null? children : new HashMap<Group, List<Child>>();
	}

	public List<Group> getGroups() {
		return m_groups;
	}
	@Override
	public int getGroupCount() {
		return getGroups().size();
	}
	@Override
	public Group getGroup(int groupPosition) {
		return getGroups().get(groupPosition);
	}
	public int getGroupIndex(Group group) {
		return getGroups().indexOf(group);
	}

	public List<Child> getChildren(Group group) {
		return m_children.get(group);
	}
	@Override
	public int getChildrenCount(int groupPosition) {
		return getChildren(getGroup(groupPosition)).size();
	}
	@Override
	public Child getChild(int groupPosition, int childPosititon) {
		return getChildren(getGroup(groupPosition)).get(childPosititon);
	}
	public int getChildIndex(Group group, Child child) {
		return getChildren(group).indexOf(child);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	/**
	 * @param groupPosition
	 * @param childPosition
	 */
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public View getGroupView(int groupPosition, boolean isExpanded, View groupConvertView, ViewGroup parentGroupView) {
		Group currentGroup = getGroup(groupPosition);
		List<Child> currentChildren = getChildren(currentGroup);
		GroupVH groupHolder;
		if (groupConvertView == null) {
			groupConvertView = m_inflater.inflate(getGroupLayoutId(), parentGroupView, false);

			groupHolder = createGroupHolder(groupConvertView);
			bindGroupModel(groupHolder, currentChildren, currentGroup);

			groupConvertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupVH)groupConvertView.getTag();
		}
		if (currentGroup == null) {
			bindEmptyGroupView(groupHolder, currentChildren, groupConvertView);
		} else {
			bindGroupView(groupHolder, currentGroup, currentChildren, groupConvertView);
		}
		return groupConvertView;
	}

	@Override
	@SuppressWarnings("unchecked")
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View childConvertView,
			ViewGroup parentGroupViewGroup) {
		Group currentGroup = getGroup(groupPosition);
		Child currentChild = getChild(groupPosition, childPosition);
		ChildVH childHolder;
		if (childConvertView == null) {
			childConvertView = m_inflater.inflate(getChildLayoutId(), parentGroupViewGroup, false);

			childHolder = createChildHolder(childConvertView);
			bindChildModel(childHolder, currentGroup, currentChild);

			childConvertView.setTag(childHolder);
		} else {
			childHolder = (ChildVH)childConvertView.getTag();
		}
		if (currentChild == null) {
			bindEmptyChildView(childHolder, currentGroup, childConvertView);
		} else {
			bindChildView(childHolder, currentGroup, currentChild, childConvertView);
		}

		return childConvertView;
	}

	protected abstract int getGroupLayoutId();

	protected abstract GroupVH createGroupHolder(View groupConvertView);

	/**
	 * Update the view-model object {@link Group} on the first usage.
	 *
	 * @param groupHolder
	 * @param currentChildren
	 * @param currentGroup
	 */
	protected void bindGroupModel(GroupVH groupHolder, List<Child> currentChildren, Group currentGroup) {
		// optional @Override
	}

	protected abstract void bindGroupView(GroupVH groupHolder, Group currentGroup, List<Child> currentChildren,
			View groupConvertView);

	/**
	 * @param groupHolder
	 * @param currentChildren
	 * @param groupConvertView
	 */
	protected void bindEmptyGroupView(GroupVH groupHolder, List<Child> currentChildren, View groupConvertView) {
		// optional @Override
	}

	protected abstract int getChildLayoutId();

	protected abstract ChildVH createChildHolder(View childConvertView);

	/**
	 * Update the view-model object {@link Child} on the first usage.
	 * @param childHolder
	 * @param currentGroup
	 * @param currentChild
	 */
	protected void bindChildModel(ChildVH childHolder, Group currentGroup, Child currentChild) {
		// optional @Override
	}

	protected abstract void bindChildView(ChildVH childHolder, Group currentGroup, Child currentChild,
			View childConvertView);

	/**
	 * @param childHolder
	 * @param currentGroup
	 * @param childConvertView
	 */
	protected void bindEmptyChildView(ChildVH childHolder, Group currentGroup, View childConvertView) {
		// optional @Override
	}
}
