package net.twisterrob.android.inventory;
public interface BuildingType {
	String TABLE = "BuildingType";
	String ID = "_id";

	String NAME = "name";
	String PRIORITY = "priority";

	String DEFAULT_ORDER = PRIORITY;

	String NAME_LIKE = "(" + NAME + " LIKE ?" + ")";
}
