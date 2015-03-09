package net.twisterrob.inventory.android.view;

public interface RecyclerViewItemEvents {
	void onItemClick(int position, long recyclerViewItemID);
	boolean onItemLongClick(int position, long recyclerViewItemID);
}
