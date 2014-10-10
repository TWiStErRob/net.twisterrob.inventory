package net.twisterrob.android.adapter;

import java.util.*;

import org.slf4j.*;

import android.content.Context;
import android.view.*;
import android.widget.*;

public abstract class BaseListAdapter<T, VH> extends BaseAdapter implements Filterable {
	private List<T> m_allItems;
	protected List<T> m_items;
	protected final Context m_context;
	protected final LayoutInflater m_inflater;
	private boolean m_hasDefaultItem;
	private SimplifyingFilter filter = new SimplifyingFilter();

	public BaseListAdapter(final Context context, final Collection<T> items) {
		this(context, items, false);
	}

	public BaseListAdapter(final Context context, final Collection<T> items, final boolean hasDefaultItem) {
		if (context == null) {
			throw new NullPointerException("context cannot be null");
		}
		if (items == null) {
			throw new NullPointerException("items cannot be null");
		}
		this.m_context = context;
		m_hasDefaultItem = hasDefaultItem;
		this.m_inflater = LayoutInflater.from(m_context);
		setItems(items instanceof List? (List<T>)items : new ArrayList<T>(items));
	}

	public int getCount() {
		return m_items.size();
	}

	public T getItem(final int position) {
		return m_items.get(position);
	}

	public long getItemId(final int position) {
		return position;
	}

	List<T> getAllItems() {
		return m_allItems.subList(m_hasDefaultItem? 1 : 0, m_allItems.size());
	}

	public List<T> getItems() {
		return m_items.subList(m_hasDefaultItem? 1 : 0, m_items.size());
	}

	public void setItems(final Collection<T> items) {
		int prefixSize = m_hasDefaultItem? 1 : 0;
		int size = items == null? 0 : items.size();
		ArrayList<T> newItems = new ArrayList<T>(prefixSize + size);
		if (m_hasDefaultItem) {
			newItems.add(null);
		}
		if (items != null) {
			newItems.addAll(items);
		}
		m_allItems = newItems;
		m_items = newItems;
	}

	public View getView(final int position, View convertView, final ViewGroup parent) {
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

	@Override
	public View getDropDownView(final int position, View convertView, final ViewGroup parent) {
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
	 * @param holder
	 * @param currentItem
	 * @deprecated Until I figure out why I did it.
	 */
	@Deprecated
	protected void bindModel(final VH holder, final T currentItem) {
		// optional @Override
	}

	protected abstract void bindView(VH holder, T currentItem, View convertView);

	/**
	 * @param holder
	 * @param convertView
	 */
	protected void bindEmptyView(final VH holder, final View convertView) {
		if (m_hasDefaultItem) {
			throw new IllegalStateException("You must override at least bindEmptyView if hasDefaultItem is true");
		}
	}

	protected int getDropDownItemLayoutId() {
		return getItemLayoutId();
	}

	protected VH createDropDownHolder(final View convertView) {
		return createHolder(convertView);
	}

	protected void bindDropDownView(final VH holder, final T currentItem, final View convertView) {
		bindView(holder, currentItem, convertView);
	}

	protected void bindEmptyDropDownView(final VH holder, final View convertView) {
		bindEmptyView(holder, convertView);
	}

	/** @deprecated Until I figure out why I did it. */
	@Deprecated
	protected void bindDropDownModel(final VH holder, final T currentItem) {
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

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			LOG.debug("performFiltering: {}", constraint);
			FilterResults results = new FilterResults();
			List<T> resultList = new ArrayList<T>();
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
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			LOG.debug("Publishing: {} -> {}", constraint, results.count);
			m_items = (List<T>)results.values;
			m_lastFinished = constraint != null? constraint.toString() : null;
		}

		public String getLastFinished() {
			return m_lastFinished;
		}
	}
}
