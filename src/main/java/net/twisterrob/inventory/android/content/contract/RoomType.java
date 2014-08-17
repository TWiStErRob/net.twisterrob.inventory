package net.twisterrob.inventory.android.content.contract;
public interface RoomType {
	String TABLE = "RoomType";
	String ID = "_id";

	String NAME = "name";
	String PRIORITY = "priority";

	String DEFAULT_ORDER = PRIORITY;

	String NAME_LIKE = "(" + NAME + " LIKE ?" + ")";

	/** Other */
	long DEFAULT = 0;
}
