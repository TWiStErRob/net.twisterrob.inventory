package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.*;

public class RoomDTO extends ImagedDTO {
	public long propertyID = Property.ID_ADD;
	public long id = Room.ID_ADD;
	public String name;
	public long type;
	public long rootItemID = Item.ID_ADD;

	public RoomDTO() {
		super.setImageDriveIdColumnName(Room.IMAGE);
		super.setImageDrawableColumnName(Room.TYPE_IMAGE);
	}

	public static RoomDTO fromCursor(Cursor cursor) {
		RoomDTO room = new RoomDTO();
		return room.fromCursorInternal(cursor);
	}

	@Override
	protected RoomDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		int idColumn = cursor.getColumnIndex(Room.ID);
		if (idColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			id = cursor.getLong(idColumn);
		}

		int nameColumn = cursor.getColumnIndex(Room.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			name = cursor.getString(nameColumn);
		}

		int typeColumn = cursor.getColumnIndex(Room.TYPE);
		if (typeColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			type = cursor.getLong(typeColumn);
		}

		int rootItemColumn = cursor.getColumnIndex(Room.ROOT_ITEM);
		if (rootItemColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			rootItemID = cursor.getLong(rootItemColumn);
		}

		int propertyColumn = cursor.getColumnIndex(Room.PROPERTY);
		if (propertyColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			propertyID = cursor.getLong(propertyColumn);
		}

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Room #%2$d: '%3$s' / %4$s in property #%1$d", propertyID, id, name, type);
	}
}
