package net.twisterrob.inventory.android.content.contract;
public interface Category extends CommonColumns {
	String TABLE = "Category";

	long INTERNAL = -1;
	long DEFAULT = 0;

	String PARENT_ID = "parent";
	String PARENT_NAME = "parentName";
}
