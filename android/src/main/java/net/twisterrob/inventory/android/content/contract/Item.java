package net.twisterrob.inventory.android.content.contract;

public interface Item extends ParentColumns {
	String TABLE = "Item";

	String CATEGORY_NAME = "categoryName";

	String PROPERTY_ID = "propertyID";
	String PROPERTY_NAME = "propertyName";

	String ROOM_ID = "roomID";
	String ROOM_NAME = "roomName";
	String ROOM_ROOT = "roomItemID";
}
