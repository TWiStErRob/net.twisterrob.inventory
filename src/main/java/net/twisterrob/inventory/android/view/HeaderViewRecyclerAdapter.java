/*
 * Copyright (C) 2014 darnmason
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.twisterrob.inventory.android.view;

import java.lang.reflect.Field;
import java.util.*;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.*;

/**
 * <p>
 * RecyclerView adapter designed to wrap an existing adapter allowing the addition of
 * header views and footer views.
 * </p>
 * <p>
 * I implemented it to aid with the transition from ListView to RecyclerView where the ListView's
 * addHeaderView and addFooterView methods were used. Using this class you may initialize your
 * header views in the Fragment/Activity and add them to the adapter in the same way you used to
 * add them to a ListView.
 * </p>
 * <p>
 * I also required to be able to swap out multiple adapters with different content, therefore
 * setAdapter may be called multiple times.
 * </p>
 * Created by darnmason on 07/11/2014.
 *
 * @see <a href="https://gist.github.com/darnmason/7bbf8beae24fe7296c8a">GitHub > darnmason > HeaderViewRecyclerAdapter.java</a>
 */
public class HeaderViewRecyclerAdapter extends WrappingAdapter<RecyclerView.ViewHolder> {
	private static final int HEADERS_START = Integer.MIN_VALUE;
	private static final int FOOTERS_START = Integer.MIN_VALUE + 10;
	private static final int ITEMS_START = Integer.MIN_VALUE + 20;
	private static final int ADAPTER_MAX_TYPES = 100;

	private final List<View> mHeaderViews = new ArrayList<>();
	private final List<View> mFooterViews = new ArrayList<>();
	private final Map<Class, Integer> mItemTypesOffset = new HashMap<>();

	/**
	 * Construct a new header view recycler adapter
	 * @param adapter The underlying adapter to wrap
	 */
	public HeaderViewRecyclerAdapter(RecyclerView.Adapter adapter) {
		mDataObserver = new HeaderFooterAwareNotifyingObserver();
		setWrappedAdapter(adapter);
	}

	/**
	 * Replaces the underlying adapter, notifying RecyclerView of changes
	 * @param adapter The new adapter to wrap
	 */
	public void setAdapter(RecyclerView.Adapter adapter) {
		if (mWrappedAdapter != null && 0 < mWrappedAdapter.getItemCount()) {
			notifyItemRangeRemoved(getHeaderCount(), mWrappedAdapter.getItemCount());
		}
		setWrappedAdapter(adapter);
		notifyItemRangeInserted(getHeaderCount(), mWrappedAdapter.getItemCount());
	}

	@Override public long getItemId(int position) {
		int headerCount = getHeaderCount();
		int itemCount = mWrappedAdapter.getItemCount();

		if (position < headerCount) {
			return HEADERS_START + position;
		} else if (headerCount + itemCount < position) {
			return FOOTERS_START + position - headerCount - itemCount;
		} else { // headerCount <= position && position < headerCount + itemCount
			return mWrappedAdapter.getItemId(position - headerCount);
		}
	}

	@Override
	public int getItemViewType(int position) {
		int hCount = getHeaderCount();
		if (position < hCount) {
			return HEADERS_START + position;
		} else {
			int itemCount = mWrappedAdapter.getItemCount();
			if (position < hCount + itemCount) {
				return getAdapterTypeOffset() + mWrappedAdapter.getItemViewType(position - hCount);
			} else {
				return FOOTERS_START + position - hCount - itemCount;
			}
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		if (viewType < HEADERS_START + getHeaderCount()) {
			return new StaticViewHolder(mHeaderViews.get(viewType - HEADERS_START));
		} else if (viewType < FOOTERS_START + getFooterCount()) {
			return new StaticViewHolder(mFooterViews.get(viewType - FOOTERS_START));
		} else {
			return mWrappedAdapter.onCreateViewHolder(viewGroup, viewType - getAdapterTypeOffset());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		int headerCount = getHeaderCount();
		int itemCount = mWrappedAdapter.getItemCount();

		if (position < headerCount) {
			onBindHeader(viewHolder, position);
		} else if (headerCount + itemCount < position) {
			onBindFooter(viewHolder, position - headerCount - itemCount);
		} else { // headerCount <= position && position < headerCount + itemCount
			try {
				int myType = viewHolder.getItemViewType();
				int viewType = myType - getAdapterTypeOffset(); // reverse of getItemViewType
				Field f = ViewHolder.class.getDeclaredField("mItemViewType");
				f.setAccessible(true);
				f.set(viewHolder, viewType);
				mWrappedAdapter.onBindViewHolder(viewHolder, position - headerCount);
				f.set(viewHolder, myType);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	protected void onBindHeader(RecyclerView.ViewHolder viewHolder, int position) {
	}
	protected void onBindFooter(RecyclerView.ViewHolder viewHolder, int position) {
	}

	/**
	 * Add a static view to appear at the start of the RecyclerView. Headers are displayed in the
	 * order they were added.
	 * @param view The header view to add
	 */
	public void addHeaderView(View view) {
		mHeaderViews.add(view);
	}

	/**
	 * Add a static view to appear at the end of the RecyclerView. Footers are displayed in the
	 * order they were added.
	 * @param view The footer view to add
	 */
	public void addFooterView(View view) {
		mFooterViews.add(view);
	}

	@Override
	public int getItemCount() {
		return getHeaderCount() + getWrappedItemCount() + getFooterCount();
	}

	/**
	 * @return The item count in the underlying adapter
	 */
	public int getWrappedItemCount() {
		return mWrappedAdapter.getItemCount();
	}

	/**
	 * @return The number of header views added
	 */
	public int getHeaderCount() {
		return mHeaderViews.size();
	}

	/**
	 * @return The number of footer views added
	 */
	public int getFooterCount() {
		return mFooterViews.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setWrappedAdapter(RecyclerView.Adapter adapter) {
		super.setWrappedAdapter(adapter);
		Class adapterClass = mWrappedAdapter.getClass();
		if (!mItemTypesOffset.containsKey(adapterClass)) {
			putAdapterTypeOffset(adapterClass);
		}
	}

	private void putAdapterTypeOffset(Class adapterClass) {
		mItemTypesOffset.put(adapterClass, ITEMS_START + mItemTypesOffset.size() * ADAPTER_MAX_TYPES);
	}

	private int getAdapterTypeOffset() {
		return mItemTypesOffset.get(mWrappedAdapter.getClass());
	}

	private static class StaticViewHolder extends RecyclerView.ViewHolder {
		public StaticViewHolder(View itemView) {
			super(itemView);
		}
	}

	private class HeaderFooterAwareNotifyingObserver extends NotifyingObserver {
		@Override
		public void onItemRangeChanged(int positionStart, int itemCount) {
			super.onItemRangeChanged(getHeaderCount() + positionStart, itemCount);
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			super.onItemRangeInserted(getHeaderCount() + positionStart, itemCount);
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			super.onItemRangeRemoved(getHeaderCount() + positionStart, itemCount);
		}

		@Override
		public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			super.onItemRangeMoved(getHeaderCount() + fromPosition, getHeaderCount() + toPosition, itemCount);
		}
	}
}
