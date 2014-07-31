package net.twisterrob.inventory.android.content.contract;
public interface Item {
	String TABLE = "Item";
	String ID = "_id";
	long ID_ADD = -1;

	String NAME = "name";
	String CATEGORY = "category";
	String PARENT = "parent";

	String DEFAULT_ORDER = NAME;

	String ROOM_ROOT = "ROOT";
}
