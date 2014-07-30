package net.twisterrob.inventory.android.content.model;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.*;

public class RoomDTO {
	public long propertyID = Property.ID_ADD;
	public long id = Room.ID_ADD;
	public String name;
	public long type;

	public static RoomDTO fromCursor(Cursor item) {
		RoomDTO room = new RoomDTO();
		room.name = item.getString(item.getColumnIndexOrThrow(Room.NAME));
		room.type = item.getLong(item.getColumnIndexOrThrow(Room.TYPE));
		return room;
	}
}
