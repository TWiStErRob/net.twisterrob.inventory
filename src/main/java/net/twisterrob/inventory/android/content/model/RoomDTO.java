package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.*;

public class RoomDTO {
	public long propertyID = Property.ID_ADD;
	public long id = Room.ID_ADD;
	public String name;
	public long type;
	public long rootItemID = Item.ID_ADD;

	public static RoomDTO fromCursor(Cursor cursor) {
		RoomDTO room = new RoomDTO();

		int idColumn = cursor.getColumnIndex(Room.ID);
		if (idColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.id = cursor.getLong(idColumn);
		}

		int nameColumn = cursor.getColumnIndex(Room.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.name = cursor.getString(nameColumn);
		}

		int typeColumn = cursor.getColumnIndex(Room.TYPE);
		if (typeColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.type = cursor.getLong(typeColumn);
		}

		int rootItemColumn = cursor.getColumnIndex(Room.ROOT_ITEM);
		if (rootItemColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.rootItemID = cursor.getLong(rootItemColumn);
		}

		int propertyColumn = cursor.getColumnIndex(Room.PROPERTY);
		if (propertyColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.propertyID = cursor.getLong(propertyColumn);
		}

		return room;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Room #%2$d: '%3$s' / %4$s in property #%1$d", propertyID, id, name, type);
	}
}
