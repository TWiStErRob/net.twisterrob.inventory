package net.twisterrob.inventory.android.content;

import net.twisterrob.inventory.android.App;

public class DatabaseHelper {
	public static long getOrCreateProperty(String name, long type, String image) {
		Long propertyID = App.db().findProperty(name);
		if (propertyID == null) {
			propertyID = App.db().createProperty(name, type, image);
		}
		return propertyID;
	}
	public static long getOrCreateRoom(long propertyID, String name, long type, String image) {
		Long roomID = App.db().findRoom(propertyID, name);
		if (roomID == null) {
			roomID = App.db().createRoom(propertyID, name, type, image);
		}
		return roomID;
	}
	public static long getOrCreateItem(long parentID, String name, long type, String image) {
		Long itemID = App.db().findItem(parentID, name);
		if (itemID == null) {
			itemID = App.db().createItem(parentID, name, type, image);
		}
		return itemID;
	}
}
