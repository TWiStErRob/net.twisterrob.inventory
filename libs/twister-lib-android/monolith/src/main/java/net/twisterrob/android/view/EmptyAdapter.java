package net.twisterrob.android.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public class EmptyAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	@Override public final int getItemCount() {
		return 0;
	}
	@Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		throw new UnsupportedOperationException("Should never be called, this adapter is permanently empty.");
	}
	@Override public void onBindViewHolder(VH holder, int position) {
		throw new UnsupportedOperationException("Should never be called, this adapter is permanently empty.");
	}
}
