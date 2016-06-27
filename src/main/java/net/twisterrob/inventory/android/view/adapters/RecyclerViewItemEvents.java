package net.twisterrob.inventory.android.view.adapters;

import android.support.annotation.UiThread;

@UiThread
public interface RecyclerViewItemEvents {
	void onItemClick(int position, long recyclerViewItemID);
	boolean onItemLongClick(int position, long recyclerViewItemID);
}
