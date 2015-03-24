package net.twisterrob.inventory.android.view.adapters;

public interface RecyclerViewItemEvents {
	void onItemClick(int position, long recyclerViewItemID);
	boolean onItemLongClick(int position, long recyclerViewItemID);
}
