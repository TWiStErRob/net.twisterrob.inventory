package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.utils.DatabaseUtils;

public class RoomDTO extends ImagedDTO {
	public long propertyID = Property.ID_ADD;
	public String propertyName;
	public long type = RoomType.DEFAULT;
	public long rootItemID = Item.ID_ADD;

	public static RoomDTO fromCursor(Cursor cursor) {
		RoomDTO room = new RoomDTO();
		return room.fromCursorInternal(cursor);
	}

	@Override
	protected RoomDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		type = DatabaseUtils.getOptionalLong(cursor, Room.TYPE, RoomType.DEFAULT);
		rootItemID = DatabaseUtils.getOptionalLong(cursor, Room.ROOT_ITEM, Item.ID_ADD);
		propertyID = DatabaseUtils.getOptionalLong(cursor, Room.PROPERTY_ID, Property.ID_ADD);
		propertyName = DatabaseUtils.getOptionalString(cursor, Room.PROPERTY_NAME);

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Room #%2$d: '%3$s' / %4$s in property #%1$d", propertyID, id, name, type);
	}
}
