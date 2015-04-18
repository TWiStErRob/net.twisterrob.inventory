package net.twisterrob.inventory.android.content.contract;

public interface Item extends CommonColumns {
	String TABLE = "Item";

	String CATEGORY_NAME = "categoryName";

	String PARENT_ID = "parent";
	String PARENT_NAME = "parentName";

	String PROPERTY_ID = "propertyID";
	String PROPERTY_NAME = "propertyName";

	String ROOM_ID = "roomID";
	String ROOM_NAME = "roomName";
	String ROOM_ROOT = "roomItemID";
}
