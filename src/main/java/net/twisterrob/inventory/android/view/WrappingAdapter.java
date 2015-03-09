package net.twisterrob.inventory.android.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.ViewGroup;

public abstract class WrappingAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	protected RecyclerView.Adapter<VH> mWrappedAdapter;
	protected RecyclerView.AdapterDataObserver mDataObserver = new NotifyingObserver();

	protected WrappingAdapter() {
		// child must call setWrappedAdapter after it's initialization
	}
	public WrappingAdapter(@NonNull RecyclerView.Adapter<VH> wrapped) {
		setWrappedAdapter(wrapped);
	}

	protected void setWrappedAdapter(@NonNull Adapter<VH> wrapped) {
		if (mWrappedAdapter != null) {
			mWrappedAdapter.unregisterAdapterDataObserver(mDataObserver);
		}
		this.mWrappedAdapter = wrapped;
		setHasStableIds(wrapped.hasStableIds());
		mWrappedAdapter.registerAdapterDataObserver(mDataObserver);
	}

	@Override public VH onCreateViewHolder(ViewGroup group, int position) {
		return mWrappedAdapter.onCreateViewHolder(group, position);
	}
	@Override public void onBindViewHolder(VH holder, int position) {
		mWrappedAdapter.onBindViewHolder(holder, position);
	}
	@Override public int getItemCount() {
		return mWrappedAdapter.getItemCount();
	}
	@Override public long getItemId(int position) {
		return mWrappedAdapter.getItemId(position);
	}
	@Override public int getItemViewType(int position) {
		return mWrappedAdapter.getItemViewType(position);
	}
	@Override public void onViewAttachedToWindow(VH holder) {
		mWrappedAdapter.onViewAttachedToWindow(holder);
	}
	@Override public void onViewDetachedFromWindow(VH holder) {
		mWrappedAdapter.onViewDetachedFromWindow(holder);
	}
	@Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		mWrappedAdapter.onDetachedFromRecyclerView(recyclerView);
	}
	@Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		mWrappedAdapter.onAttachedToRecyclerView(recyclerView);
	}
	@Override public void onViewRecycled(VH holder) {
		mWrappedAdapter.onViewRecycled(holder);
	}

	protected class NotifyingObserver extends RecyclerView.AdapterDataObserver {
		@Override public void onChanged() {
			notifyDataSetChanged();
		}

		@Override public void onItemRangeChanged(int positionStart, int itemCount) {
			notifyItemRangeChanged(positionStart, itemCount);
		}

		@Override public void onItemRangeInserted(int positionStart, int itemCount) {
			notifyItemRangeInserted(positionStart, itemCount);
		}

		@Override public void onItemRangeRemoved(int positionStart, int itemCount) {
			notifyItemRangeRemoved(positionStart, itemCount);
		}

		@Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			// TODO: No notifyItemRangeMoved method?
			notifyItemRangeChanged(fromPosition, toPosition + itemCount);
		}
	}
}
