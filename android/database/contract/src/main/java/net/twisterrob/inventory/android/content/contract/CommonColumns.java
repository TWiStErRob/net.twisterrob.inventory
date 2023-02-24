package net.twisterrob.inventory.android.content.contract;

public interface CommonColumns {
	String ID = "_id";
	long ID_ADD = Long.MIN_VALUE;

	String NAME = "name"; // never NULL
	String DESCRIPTION = "description";
	String HAS_IMAGE = "hasImage";
	String IMAGE_TIME = "imageTime";
	String TYPE_IMAGE = "typeImage"; // never NULL
	String TYPE_ID = "typeID";
	String TYPE = "type";
	String COUNT_CHILDREN_DIRECT = "countChildren";
	String COUNT_CHILDREN_ALL = "countAllChildren";
	String COUNT_ITEM_DIRECT = "countDirectItems";
	String COUNT_ITEM_ALL = "countAllItems";
}
