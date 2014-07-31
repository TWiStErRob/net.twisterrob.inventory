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

	public static RoomDTO fromCursor(Cursor item) {
		RoomDTO room = new RoomDTO();

		int idColumn = item.getColumnIndex(Room.ID);
		if (idColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.id = item.getLong(idColumn);
		}

		int nameColumn = item.getColumnIndex(Room.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.name = item.getString(nameColumn);
		}

		int typeColumn = item.getColumnIndex(Room.TYPE);
		if (typeColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.type = item.getLong(typeColumn);
		}

		int rootItemColumn = item.getColumnIndex(Room.ROOT_ITEM);
		if (rootItemColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.rootItemID = item.getLong(rootItemColumn);
		}

		int propertyColumn = item.getColumnIndex(Room.PROPERTY);
		if (propertyColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			room.propertyID = item.getLong(propertyColumn);
		}

		return room;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Room #%2$d: '%3$s' / %4$s in property #%1$d", propertyID, id, name, type);
	}
}
