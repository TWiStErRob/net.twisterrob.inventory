package net.twisterrob.android.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class WrappingAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	protected RecyclerView.Adapter<VH> mWrappedAdapter;
	protected RecyclerView.AdapterDataObserver mDataObserver = new NotifyingObserver();

	protected WrappingAdapter() {
		// child must call setWrappedAdapter after it's initialization
	}
	public WrappingAdapter(@NonNull RecyclerView.Adapter<VH> wrapped) {
		setWrappedAdapter(wrapped);
	}

	public RecyclerView.Adapter<VH> getWrappedAdapter() {
		return mWrappedAdapter;
	}

	protected void setWrappedAdapter(@NonNull RecyclerView.Adapter<VH> wrapped) {
		if (mWrappedAdapter != null) {
			mWrappedAdapter.unregisterAdapterDataObserver(mDataObserver);
		}
		this.mWrappedAdapter = wrapped;
		setHasStableIds(wrapped.hasStableIds());
		mWrappedAdapter.registerAdapterDataObserver(mDataObserver);
	}

	@Override public VH onCreateViewHolder(ViewGroup group, int viewType) {
		return mWrappedAdapter.onCreateViewHolder(group, viewType);
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
			// TOFIX No notifyItemRangeMoved method? http://b.android.com/125984
			notifyItemRangeChanged(fromPosition, toPosition + itemCount);
		}
	}
}
