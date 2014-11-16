package net.twisterrob.inventory.android.view;

import android.support.v7.widget.RecyclerView;

public interface RecyclerViewItemEvents {
	void onItemClick(RecyclerView.ViewHolder holder);
	boolean onItemLongClick(RecyclerView.ViewHolder holder);
}
