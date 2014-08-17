package net.twisterrob.inventory.android.content.contract;
public interface Category extends CommonColumns {
	String TABLE = "Category";

	long INTERNAL = -1;

	String PARENT_ID = "parent";
	String PARENT_NAME = "parentName";

	String ITEM_COUNT_DIRECT = "countDirectItems";
	String ITEM_COUNT_ALL = "countAllItems";
}
