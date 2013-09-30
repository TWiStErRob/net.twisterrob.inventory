package net.twisterrob.android.adapter;

import java.util.*;

import android.content.Context;
import android.view.*;
import android.widget.BaseAdapter;

public abstract class BaseListAdapter<T, VH> extends BaseAdapter {
	protected List<T> m_items;
	protected final Context m_context;
	protected final LayoutInflater m_inflater;
	private boolean m_hasDefaultItem;

	public BaseListAdapter(final Context context, final Collection<T> items) {
		this(context, items, false);
	}

	public BaseListAdapter(final Context context, final Collection<T> items, final boolean hasDefaultItem) {
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
		m_items = newItems;
	}

	@SuppressWarnings("unchecked")
	public View getView(final int position, View convertView, final ViewGroup parent) {
		T currentItem = m_items.get(position);
		VH holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(getItemLayoutId(), null);

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

	@SuppressWarnings("unchecked")
	@Override
	public View getDropDownView(final int position, View convertView, final ViewGroup parent) {
		T currentItem = m_items.get(position);
		VH holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(getDropDownItemLayoutId(), null);

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

	/** @deprecated Until I figure out why I did it. */
	@Deprecated
	protected void bindModel(final VH holder, final T currentItem) {}

	protected abstract void bindView(VH holder, T currentItem, View convertView);

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
}
