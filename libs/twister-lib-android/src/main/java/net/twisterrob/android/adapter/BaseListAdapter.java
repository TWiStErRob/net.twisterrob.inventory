package net.twisterrob.android.adapter;

import java.util.*;

import org.slf4j.*;

import android.content.Context;
import android.view.*;
import android.widget.*;

@SuppressWarnings("unused")
public abstract class BaseListAdapter<T, VH> extends BaseAdapter implements Filterable {
	private List<T> m_allItems;
	protected List<T> m_items;
	protected final Context m_context;
	protected final LayoutInflater m_inflater;
	private boolean m_hasDefaultItem;
	private final SimplifyingFilter filter = new SimplifyingFilter();

	public BaseListAdapter(Context context, Collection<T> items) {
		this(context, items, false);
	}

	public BaseListAdapter(Context context, Collection<T> items, boolean hasDefaultItem) {
		if (context == null) {
			throw new NullPointerException("context cannot be null");
		}
		if (items == null) {
			throw new NullPointerException("items cannot be null");
		}
		this.m_context = context;
		m_hasDefaultItem = hasDefaultItem;
		this.m_inflater = LayoutInflater.from(m_context);
		setItems(items instanceof List? (List<T>)items : new ArrayList<>(items));
	}

	public int getCount() {
		return m_items.size();
	}

	public T getItem(int position) {
		return m_items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	List<T> getAllItems() {
		return m_allItems.subList(m_hasDefaultItem? 1 : 0, m_allItems.size());
	}

	public Collection<T> getItems() {
		return m_items.subList(m_hasDefaultItem? 1 : 0, m_items.size());
	}

	public void setItems(Collection<T> items) {
		int prefixSize = m_hasDefaultItem? 1 : 0;
		int size = items == null? 0 : items.size();
		ArrayList<T> newItems = new ArrayList<>(prefixSize + size);
		if (m_hasDefaultItem) {
			newItems.add(null);
		}
		if (items != null) {
			newItems.addAll(items);
		}
		m_allItems = newItems;
		m_items = newItems;
	}

	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {
		T currentItem = m_items.get(position);
		VH holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(getItemLayoutId(), parent, false);

			holder = createHolder(convertView);
			bindModel(holder, currentItem);

			convertView.setTag(holder);
		} else {
			holder = (VH)convertView.getTag();
		}

		if (currentItem == null) {
			bindEmptyView(holder, convertView);
		} else {
			bindView(holder, currentItem, convertView);
		}

		return convertView;
	}

	@Override @SuppressWarnings("unchecked")
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		T currentItem = m_items.get(position);
		VH holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(getDropDownItemLayoutId(), parent, false);

			holder = createDropDownHolder(convertView);
			bindDropDownModel(holder, currentItem);

			convertView.setTag(holder);
		} else {
			holder = (VH)convertView.getTag();
		}

		if (currentItem == null) {
			bindEmptyDropDownView(holder, convertView);
		} else {
			bindDropDownView(holder, currentItem, convertView);
		}

		return convertView;
	}

	protected abstract int getItemLayoutId();

	protected abstract VH createHolder(View convertView);

	/**
	 * Update the view-model object {@link T} on the first usage.
	 */
	protected void bindModel(VH holder, T currentItem) {
		// optional override
	}

	protected abstract void bindView(VH holder, T currentItem, View convertView);

	protected void bindEmptyView(VH holder, View convertView) {
		if (m_hasDefaultItem) {
			throw new IllegalStateException("You must override at least bindEmptyView if hasDefaultItem is true");
		}
	}

	protected int getDropDownItemLayoutId() {
		return getItemLayoutId();
	}

	protected VH createDropDownHolder(View convertView) {
		return createHolder(convertView);
	}

	protected void bindDropDownView(VH holder, T currentItem, View convertView) {
		bindView(holder, currentItem, convertView);
	}

	protected void bindEmptyDropDownView(VH holder, View convertView) {
		bindEmptyView(holder, convertView);
	}

	/** @see #bindModel */
	protected void bindDropDownModel(VH holder, T currentItem) {
		bindModel(holder, currentItem);
	}

	/**
	 * No need to call super, default implementation returns everything with {@link List#addAll(Collection)}.
	 * @param fullList source list with all items
	 * @param filter filter query, never <code>null</code>
	 * @param resultList target list with filtered items
	 */
	protected List<T> filter(List<? extends T> fullList, String filter, List<T> resultList) {
		resultList.addAll(fullList);
		return resultList;
	}

	/**
	 * No need to call super, default implementation returns everything with {@link List#addAll(Collection)}.
	 * @param fullList source list with all items
	 * @param resultList target list with filtered items
	 */
	protected List<T> filterNoQuery(List<? extends T> fullList, List<T> resultList) {
		resultList.addAll(fullList);
		return resultList;
	}
	public Filter getFilter() {
		return filter;
	}

	public String getLastFilter() {
		return filter.getLastFinished();
	}

	private final class SimplifyingFilter extends Filter {
		private final Logger LOG = LoggerFactory.getLogger(BaseListAdapter.SimplifyingFilter.class);

		private String m_lastFinished;

		public SimplifyingFilter() {
		}

		@Override protected FilterResults performFiltering(CharSequence constraint) {
			LOG.debug("performFiltering: {}", constraint);
			FilterResults results = new FilterResults();
			List<T> resultList = new ArrayList<>();
			if (constraint == null) {
				resultList = BaseListAdapter.this.filterNoQuery(getAllItems(), resultList);
			} else {
				resultList = BaseListAdapter.this.filter(getAllItems(), constraint.toString(), resultList);
			}
			results.count = resultList.size();
			results.values = resultList;
			LOG.debug("finished performFiltering: {}", constraint);
			return results;
		}
		@SuppressWarnings("unchecked")
		@Override protected void publishResults(CharSequence constraint, FilterResults results) {
			LOG.debug("Publishing: {} -> {}", constraint, results.count);
			m_items = (List<T>)results.values;
			m_lastFinished = constraint != null? constraint.toString() : null;
		}

		public String getLastFinished() {
			return m_lastFinished;
		}
	}
}
