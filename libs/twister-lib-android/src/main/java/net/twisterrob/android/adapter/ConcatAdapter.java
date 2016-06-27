package net.twisterrob.android.adapter;

import java.util.*;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.view.ViewGroup;

@SuppressWarnings("rawtypes") // hope for the implementation to handle the position mapping correctly
public class ConcatAdapter extends Adapter<ViewHolder> {
	private final Adapter<? extends ViewHolder>[] mWrappedAdapters;
	private final Map<Integer, Adapter<? extends ViewHolder>> mViewTypes = new HashMap<>();

	@SafeVarargs
	public ConcatAdapter(Adapter<? extends ViewHolder>... mWrappedAdapters) {
		this.mWrappedAdapters = mWrappedAdapters;

		boolean hasStableIDs = true;
		for (Adapter<? extends RecyclerView.ViewHolder> wrapped : mWrappedAdapters) {
			wrapped.registerAdapterDataObserver(new NotifyingObserver(wrapped));
			hasStableIDs &= wrapped.hasStableIds();
		}
		setHasStableIds(hasStableIDs);
	}

	@Override public ViewHolder onCreateViewHolder(ViewGroup group, int viewType) {
		return mViewTypes.get(viewType).onCreateViewHolder(group, viewType);
	}
	@Override public void onBindViewHolder(ViewHolder holder, int position) {
		Adapter<? super ViewHolder> adapter = getAdapter(position);
		adapter.onBindViewHolder(holder, position - getOffsetOf(adapter));
	}
	@Override public int getItemCount() {
		int count = 0;
		for (Adapter<?> adapter : mWrappedAdapters) {
			count += adapter.getItemCount();
		}
		return count;
	}
	/**
	 * For a given merged position, find the corresponding Adapter and local position within that Adapter by iterating through Adapters and
	 * summing their counts until the merged position is found.
	 *
	 * @param position a merged (global) position
	 * @return the matching Adapter and local position, or null if not found
	 */
	public List<Adapter> getAdapters(int position) {
		int count = 0;
		List<Adapter> results = new LinkedList<>();
		for (Adapter adapter : mWrappedAdapters) {
			if (count <= position && position < count + adapter.getItemCount()) {
				results.add(adapter);
			}
			count += adapter.getItemCount();
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	public Adapter<? super ViewHolder> getAdapter(int position) {
		// if there are multiple adapters at this position, then the first n-1 must be 0 length
		List<Adapter> adapters = getAdapters(position);
		if (adapters.isEmpty()) {
			throw new IllegalStateException(String.format(Locale.ROOT,
					"Cannot find position: %d in %s", position, getCounts()));
		}
		return adapters.listIterator(adapters.size()).previous();
	}
	private List<Integer> getCounts() {
		List<Integer> counts = new ArrayList<>(mWrappedAdapters.length);
		for (Adapter<?> adapter : mWrappedAdapters) {
			counts.add(adapter.getItemCount());
		}
		return counts;
	}

	@Override public long getItemId(int position) {
		Adapter<? super ViewHolder> adapter = getAdapter(position);
		return adapter.getItemId(position - getOffsetOf(adapter));
	}
	@Override public int getItemViewType(int position) {
		Adapter<? super ViewHolder> adapter = getAdapter(position);
		int type = adapter.getItemViewType(position - getOffsetOf(adapter));
		Adapter<? extends ViewHolder> overwritten = mViewTypes.put(type, adapter);
		if (adapter != overwritten && overwritten != null) {
			throw new IllegalStateException(String.format(Locale.ROOT,
					"Two colliding adapters (%s, %s) have the same itemViewType #%d", adapter, overwritten, type));
		}
		return type;
	}
	@Override public void onViewAttachedToWindow(ViewHolder holder) {
		getAdapter(holder.getAdapterPosition()).onViewAttachedToWindow(holder);
	}
	@Override public void onViewDetachedFromWindow(ViewHolder holder) {
		getAdapter(holder.getAdapterPosition()).onViewDetachedFromWindow(holder);
	}
	@Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		for (Adapter<?> adapter : mWrappedAdapters) {
			adapter.onDetachedFromRecyclerView(recyclerView);
		}
	}
	@Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		for (Adapter<?> adapter : mWrappedAdapters) {
			adapter.onAttachedToRecyclerView(recyclerView);
		}
	}
	@Override public void onViewRecycled(ViewHolder holder) {
		// FIXME getAdapter(holder.getPosition()).onViewRecycled(holder);
	}

	private int getOffsetOf(Adapter<? extends ViewHolder> wrapped) {
		int count = 0;
		for (Adapter<?> adapter : mWrappedAdapters) {
			if (adapter == wrapped) {
				break;
			}
			count += adapter.getItemCount();
		}
		return count;
	}

	protected class NotifyingObserver extends RecyclerView.AdapterDataObserver {
		private final Adapter<? extends ViewHolder> wrapped;

		public NotifyingObserver(Adapter<? extends ViewHolder> wrapped) {
			this.wrapped = wrapped;
		}

		@Override public void onChanged() {
			notifyDataSetChanged();
		}

		@Override public void onItemRangeChanged(int positionStart, int itemCount) {
			notifyItemRangeChanged(getOffsetOf(wrapped) + positionStart, itemCount);
		}

		@Override public void onItemRangeInserted(int positionStart, int itemCount) {
			notifyItemRangeInserted(getOffsetOf(wrapped) + positionStart, itemCount);
		}

		@Override public void onItemRangeRemoved(int positionStart, int itemCount) {
			notifyItemRangeRemoved(getOffsetOf(wrapped) + positionStart, itemCount);
		}

		@Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			// TOFIX No notifyItemRangeMoved method?
			int offset = getOffsetOf(wrapped);
			notifyItemRangeChanged(offset + fromPosition, offset + toPosition + itemCount);
		}
	}
}
