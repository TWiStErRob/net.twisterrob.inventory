package net.twisterrob.inventory.android.db;
public interface PropertyType {
	String TABLE = "PropertyType";
	String ID = "id";

	String NAME = "name";
	String PRIORITY = "priority";

	String DEFAULT_ORDER = PRIORITY;

	String NAME_LIKE = "(" + NAME + " LIKE ?" + ")";
}
