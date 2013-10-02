package net.twisterrob.android.adapter;

import java.util.*;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.*;

public abstract class BaseExpandableListAdapter<Group, Child, GroupVH, ChildVH>
		extends
			android.widget.BaseExpandableListAdapter {
	protected final Context m_context;
	protected final LayoutInflater m_inflater;
	protected List<Group> m_groups;
	protected Map<Group, ? extends List<Child>> m_children;
	protected List<Group> m_filteredGroups;
	protected Map<Group, List<Child>> m_filteredChildren;

	public BaseExpandableListAdapter(final Context context, final Collection<Group> groups,
			final Map<Group, ? extends List<Child>> children) {
		this.m_context = context;
		this.m_inflater = LayoutInflater.from(m_context);
		this.m_groups = groups instanceof List? (List<Group>)groups : new ArrayList<Group>(groups);
		this.m_children = children;
		super.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				resetFiltered();
			}
			@Override
			public void onInvalidated() {
				resetFiltered();
			}
			protected void resetFiltered() {
				m_filteredGroups = null;
				m_filteredChildren = null;
			}
		});
	}

	public final List<Group> getAllGroups() {
		return m_groups;
	}
	public void setAllGroups(List<Group> groups) {
		m_groups = groups != null? groups : new ArrayList<Group>();
	}
	public final List<Group> getFilteredGroups() {
		if (m_filteredGroups == null) {
			m_filteredGroups = filterGroups(getAllGroups());
			Log.d("filter", "Groups: " + m_filteredGroups);
		}
		return m_filteredGroups;
	}

	public final Map<Group, ? extends List<Child>> getAllChildren() {
		return m_children;
	}
	public void setAllChildren(Map<Group, ? extends List<Child>> children) {
		m_children = children != null? children : new HashMap<Group, List<Child>>();
	}
	public final List<Child> getFilteredChildren(Group group) {
		if (m_filteredChildren == null) {
			m_filteredChildren = new HashMap<Group, List<Child>>();
		}
		List<Child> filteredChildren = m_filteredChildren.get(group);
		if (filteredChildren == null) {
			filteredChildren = filterChildren(getAllChildren().get(group), group);
			m_filteredChildren.put(group, filteredChildren);
			Log.d("filter", group + ": " + filteredChildren);
		}
		return filteredChildren;
	}

	protected List<Group> filterGroups(List<Group> groups) {
		return groups;
	}
	protected List<Child> filterChildren(List<Child> children, Group group) {
		return children;
	}

	@Override
	public int getGroupCount() {
		Log.d("filter", "groupCount: " + getFilteredGroups().size());
		return getFilteredGroups().size();
	}
	@Override
	public Group getGroup(int groupPosition) {
		return getFilteredGroups().get(groupPosition);
	}
	public int getGroupIndex(Group group) {
		return getFilteredGroups().indexOf(group);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		Log.d("filter", getGroup(groupPosition) + " childCount: " + getFilteredChildren(getGroup(groupPosition)).size());
		return getFilteredChildren(getGroup(groupPosition)).size();
	}
	@Override
	public Child getChild(int groupPosition, int childPosititon) {
		return getFilteredChildren(getGroup(groupPosition)).get(childPosititon);
	}
	public int getChildIndex(Group group, Child child) {
		return getFilteredChildren(group).indexOf(child);
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
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View groupConvertView, ViewGroup parentListGroupView) {
		Group currentGroup = getGroup(groupPosition);
		List<Child> currentChildren = getFilteredChildren(currentGroup);
		GroupVH groupHolder;
		if (groupConvertView == null) {
			groupConvertView = m_inflater.inflate(getGroupLayoutId(), null);

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

	@SuppressWarnings("unchecked")
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View childConvertView,
			ViewGroup parentGroupViewGroup) {
		Group currentGroup = getGroup(groupPosition);
		Child currentChild = getChild(groupPosition, childPosition);
		ChildVH childHolder;
		if (childConvertView == null) {
			childConvertView = m_inflater.inflate(getChildLayoutId(), null);

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

	/** @param currentChildren 
	 * @deprecated Until I figure out why I did it. */
	@Deprecated
	protected void bindGroupModel(final GroupVH groupHolder, List<Child> currentChildren, final Group currentGroup) {}

	protected abstract void bindGroupView(final GroupVH groupHolder, final Group currentGroup,
			List<Child> currentChildren, final View groupConvertView);

	protected void bindEmptyGroupView(final GroupVH groupHolder, List<Child> currentChildren,
			final View groupConvertView) {}

	protected abstract int getChildLayoutId();

	protected abstract ChildVH createChildHolder(View childConvertView);

	/** @param currentGroup 
	 * @deprecated Until I figure out why I did it. */
	@Deprecated
	protected void bindChildModel(final ChildVH childHolder, Group currentGroup, final Child currentChild) {}

	protected abstract void bindChildView(final ChildVH childHolder, Group currentGroup, final Child currentChild,
			final View childConvertView);

	protected void bindEmptyChildView(final ChildVH childHolder, Group currentGroup, final View childConvertView) {}
}
