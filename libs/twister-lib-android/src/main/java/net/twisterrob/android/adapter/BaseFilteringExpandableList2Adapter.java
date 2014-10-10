package net.twisterrob.android.adapter;

import java.util.*;

import android.content.Context;
import android.database.DataSetObserver;

public abstract class BaseFilteringExpandableList2Adapter<Group, Child, GroupVH, ChildVH>
		extends BaseExpandableList2Adapter<Group, Child, GroupVH, ChildVH> {
	List<Group> m_filteredGroups;
	Map<Group, List<Child>> m_filteredChildren;

	public BaseFilteringExpandableList2Adapter(final Context context, final Collection<Group> groups,
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

	/**
	 * Filter children in this method.
	 *
	 * Does no filtering by default, just return children.
	 * @param children to filter
	 * @param group the children belong to
	 * @return the filtered children
	 */
	protected List<Child> filterChildren(List<Child> children, Group group) {
		return children;
	}
}
