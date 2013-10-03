package net.twisterrob.android.adapter;

import java.util.*;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;

public abstract class BaseFilteringExpandableListAdapter<Group, Child, GroupVH, ChildVH>
		extends
			BaseExpandableListAdapter<Group, Child, GroupVH, ChildVH> {
	private List<Group> m_filteredGroups;
	private Map<Group, List<Child>> m_filteredChildren;

	public BaseFilteringExpandableListAdapter(final Context context, final Collection<Group> groups,
			final Map<Group, ? extends List<Child>> children) {
		super(context, groups, children);
		this.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				resetFiltered();
			}
			@Override
			public void onInvalidated() {
				resetFiltered();
			}
			void resetFiltered() {
				m_filteredGroups = null;
				m_filteredChildren = null;
			}
		});
	}

	@Override
	public List<Group> getGroups() {
		if (m_filteredGroups == null) {
			m_filteredGroups = filterGroups(super.getGroups());
			Log.d("filter", "Groups: " + m_filteredGroups);
		}
		return m_filteredGroups;
	}

	@Override
	public List<Child> getChildren(Group group) {
		if (m_filteredChildren == null) {
			m_filteredChildren = new HashMap<Group, List<Child>>();
		}
		List<Child> filteredChildren = m_filteredChildren.get(group);
		if (filteredChildren == null) {
			filteredChildren = filterChildren(super.getChildren(group), group);
			m_filteredChildren.put(group, filteredChildren);
		}
		return filteredChildren;
	}

	protected List<Group> filterGroups(List<Group> groups) {
		return groups;
	}

	protected List<Child> filterChildren(List<Child> children, Group group) {
		return children;
	}
}
