package net.twisterrob.android.adapter;

import java.util.*;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.ExpandableListView.OnGroupClickListener;

public abstract class BaseExpandableList3Adapter<Level1, Level2, Level3, Level1VH, Level2VH, Level3VH>
		extends android.widget.BaseExpandableListAdapter {
	protected final Context m_context;
	protected final LayoutInflater m_inflater;
	private final List<Level1> m_groups = new ArrayList<Level1>();
	private final Map<Level1, List<Level2>> m_children;
	private final Map<Level1, ? extends Map<Level2, ? extends List<Level3>>> m_data;
	private ExpandableListView m_outerList;

	public BaseExpandableList3Adapter(final Context context, ExpandableListView outerList,
			final Map<Level1, ? extends Map<Level2, ? extends List<Level3>>> data) {
		this.m_context = context;
		this.m_outerList = outerList;
		this.m_inflater = LayoutInflater.from(m_context);
		this.m_data = data;
		this.m_children = createChildrenMap();
		registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				refreshData();
			}
		});
		refreshData();
	}

	protected Map<Level1, List<Level2>> createChildrenMap() {
		return new HashMap<Level1, List<Level2>>();
	}

	protected void refreshData() {
		this.m_groups.clear();
		this.m_groups.addAll(m_data.keySet());
		this.m_children.clear();
		for (Entry<Level1, ? extends Map<Level2, ? extends List<Level3>>> entry : m_data.entrySet()) {
			m_children.put(entry.getKey(), new ArrayList<Level2>(entry.getValue().keySet()));
		}
	}

	public List<Level1> getGroups() {
		return m_groups;
	}
	@Override
	public int getGroupCount() {
		return getGroups().size();
	}
	@Override
	public Level1 getGroup(int groupPosition) {
		return getGroups().get(groupPosition);
	}
	public int getGroupIndex(Level1 group) {
		return getGroups().indexOf(group);
	}

	public List<Level2> getChildren(Level1 group) {
		return m_children.get(group);
	}
	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}
	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return null;
	}
	public int getChildIndex(Level1 group, Level2 child) {
		return getChildren(group).indexOf(child);
	}

	public List<Level3> getDetails(Level1 group, Level2 child) {
		return m_data.get(group).get(child);
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

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View groupConvertView, ViewGroup parentGroupView) {
		Level1 currentGroup = getGroup(groupPosition);
		List<Level2> currentChildren = getChildren(currentGroup);
		Level1VH groupHolder;
		if (groupConvertView == null) {
			groupConvertView = m_inflater.inflate(getLevel1LayoutId(), parentGroupView, false);
			groupHolder = createGroupHolder(groupConvertView);
			groupConvertView.setTag(groupHolder);
		} else {
			groupHolder = (Level1VH)groupConvertView.getTag();
		}
		if (currentGroup == null) {
			bindEmptyLevel1View(groupHolder, groupConvertView);
		} else {
			bindLevel1View(groupHolder, currentGroup, currentChildren, groupConvertView);
		}
		return groupConvertView;
	}
	@Override
	public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View childConvertView,
			ViewGroup parentGroupViewGroup) {
		assert childPosition == 0 : "We should only have one child";
		Level1 level1Group = getGroup(groupPosition);
		List<Level2> level2Children = getChildren(level1Group);
		ExpandableListView list;
		if (childConvertView == null) {
			list = new InnerExpandableListView(m_context, m_outerList);
			list.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		} else {
			list = (ExpandableListView)childConvertView;
		}
		InnerExpandableListAdapter adapter = new InnerExpandableListAdapter(m_context, level1Group, level2Children,
				m_data.get(level1Group));
		list.setAdapter(adapter);
		bindLevel2Groups(list, adapter);
		return list;
	}

	// Level 1

	protected abstract int getLevel1LayoutId();

	protected abstract Level1VH createGroupHolder(View level1ConvertView);

	protected abstract void bindLevel1View(final Level1VH level1Holder, final Level1 currentLevel1,
			List<Level2> currentLevel2, final View level1ConvertView);

	/**
	 * @param level1Holder
	 * @param level1ConvertView
	 */
	protected void bindEmptyLevel1View(final Level1VH level1Holder, final View level1ConvertView) {
		// optional @Override
	}

	// Level 2

	/**
	 * @param list
	 * @param adapter
	 */
	protected void bindLevel2Groups(ExpandableListView list, BaseExpandableListAdapter adapter) {
		// optional @Override
	}

	protected abstract int getLevel2LayoutId();

	protected abstract Level2VH createLevel2Holder(View level2ConvertView);

	protected abstract void bindLevel2View(final Level2VH level2Holder, Level1 currentLevel1,
			final Level2 currentLevel2, List<Level3> currentLevel3, final View level2ConvertView);

	/**
	 * @param level2Holder
	 * @param currentLevel1
	 * @param level1ConvertView
	 */
	protected void bindEmptyLevel2View(final Level2VH level2Holder, Level1 currentLevel1,
			final View level1ConvertView) {
		// optional @Override
	}

	// Level 3

	protected abstract int getLevel3LayoutId();

	protected abstract Level3VH createLevel3Holder(View level3ConvertView);

	protected abstract void bindLevel3View(final Level3VH level3Holder, Level1 currentLevel1, Level2 currentLevel2,
			final Level3 currentLevel3, final View level3ConvertView);

	/**
	 * @param level3Holder
	 * @param currentLevel1
	 * @param currentLevel2
	 * @param level3ConvertView
	 */
	protected void bindEmptyLevel3View(final Level3VH level3Holder, Level1 currentLevel1, Level2 currentLevel2,
			final View level3ConvertView) {
		// optional @Override
	}

	private class InnerExpandableListAdapter extends BaseExpandableList2Adapter<Level2, Level3, Level2VH, Level3VH> {
		private final Level1 currentLevel1;
		protected InnerExpandableListAdapter(Context context, Level1 currentLevel1, Collection<Level2> currentLevel2,
				Map<Level2, ? extends List<Level3>> currentLevel3) {
			super(context, currentLevel2, currentLevel3);
			this.currentLevel1 = currentLevel1;
		}
		@Override
		protected int getGroupLayoutId() {
			return getLevel2LayoutId();
		}
		@Override
		protected Level2VH createGroupHolder(View level2ConvertView) {
			return createLevel2Holder(level2ConvertView);
		}
		@Override
		protected void bindGroupView(Level2VH level2Holder, Level2 currentLevel2, List<Level3> currentLevel3,
				View groupConvertView) {
			bindLevel2View(level2Holder, currentLevel1, currentLevel2, currentLevel3, groupConvertView);
		}
		@Override
		protected int getChildLayoutId() {
			return getLevel3LayoutId();
		}
		@Override
		protected Level3VH createChildHolder(View level3ConvertView) {
			return createLevel3Holder(level3ConvertView);
		}
		@Override
		protected void bindChildView(Level3VH level3Holder, Level2 currentLevel2, Level3 currentLevel3,
				View childConvertView) {
			bindLevel3View(level3Holder, currentLevel1, currentLevel2, currentLevel3, childConvertView);
		}
	}

	private static class InnerExpandableListView extends ExpandableListView implements OnGroupClickListener {
		private ExpandableListView outerList;
		public InnerExpandableListView(Context context, ExpandableListView outerList) {
			super(context);
			this.outerList = outerList;
			this.setOnGroupClickListener(this);
		}
		@Override
		@SuppressLint("InlinedApi")
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST));
		}
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			assert parent == this;
			outerList.requestLayout();
			return false;
		}

		@Override
		public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
			super.setOnGroupClickListener(onGroupClickListener);
		}
	}
}
