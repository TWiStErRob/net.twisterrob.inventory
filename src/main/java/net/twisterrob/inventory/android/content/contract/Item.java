package net.twisterrob.inventory.android.content.contract;
public interface Item extends CommonColumns {
	String TABLE = "Item";

	String CATEGORY = "category";
	String CATEGORY_NAME = "categoryName";

	String PARENT_ID = "parent";
	String PARENT_NAME = "parentName";

	String ROOM_ROOT = "ROOT";

	String PROPERTY_NAME = "propertyName";

	String ROOM_NAME = "roomName";
}
