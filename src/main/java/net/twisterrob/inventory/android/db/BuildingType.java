package net.twisterrob.inventory.android.db;
public interface BuildingType {
	String TABLE = "BuildingType";
	String ID = "_id";

	String NAME = "name";
	String PRIORITY = "priority";

	String DEFAULT_ORDER = PRIORITY;

	String NAME_LIKE = "(" + NAME + " LIKE ?" + ")";
}
