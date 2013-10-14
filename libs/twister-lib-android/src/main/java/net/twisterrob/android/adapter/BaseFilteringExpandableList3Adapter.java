package net.twisterrob.android.adapter;

import java.util.*;

import android.content.Context;
import android.database.DataSetObserver;
import android.widget.ExpandableListView;

public abstract class BaseFilteringExpandableList3Adapter<Level1, Level2, Level3, Level1VH, Level2VH, Level3VH>
		extends
			BaseExpandableList3Adapter<Level1, Level2, Level3, Level1VH, Level2VH, Level3VH> {
	List<Level1> m_filteredGroups;
	Map<Level1, List<Level2>> m_filteredChildren;

	public BaseFilteringExpandableList3Adapter(final Context context, ExpandableListView outerList,
			Map<Level1, ? extends Map<Level2, ? extends List<Level3>>> data) {
		super(context, outerList, data);
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
	public List<Level1> getGroups() {
		if (m_filteredGroups == null) {
			m_filteredGroups = filterGroups(super.getGroups());
		}
		return m_filteredGroups;
	}

	@Override
	public List<Level2> getChildren(Level1 group) {
		if (m_filteredChildren == null) {
			m_filteredChildren = new HashMap<Level1, List<Level2>>();
		}
		List<Level2> filteredChildren = m_filteredChildren.get(group);
		if (filteredChildren == null) {
			filteredChildren = filterChildren(super.getChildren(group), group);
			m_filteredChildren.put(group, filteredChildren);
		}
		return filteredChildren;
	}

	protected List<Level1> filterGroups(List<Level1> groups) {
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
	protected List<Level2> filterChildren(List<Level2> children, Level1 group) {
		return children;
	}
}
