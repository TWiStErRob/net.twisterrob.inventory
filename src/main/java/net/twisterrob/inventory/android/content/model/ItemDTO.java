package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.content.contract.*;

public class ItemDTO extends ImagedDTO {
	public long parentID = Item.ID_ADD;
	public String parentName;
	public long category;
	public String categoryName;
	public String propertyName;
	public String roomName;

	public static ItemDTO fromCursor(Cursor cursor) {
		ItemDTO item = new ItemDTO();
		return item.fromCursorInternal(cursor);
	}

	@Override
	protected ItemDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		parentID = DatabaseTools.getOptionalLong(cursor, Item.PARENT_ID, Item.ID_ADD);
		parentName = DatabaseTools.getOptionalString(cursor, Item.PARENT_NAME);
		category = DatabaseTools.getOptionalLong(cursor, Item.CATEGORY, Category.ID_ADD);
		categoryName = DatabaseTools.getOptionalString(cursor, Item.CATEGORY_NAME);
		propertyName = DatabaseTools.getOptionalString(cursor, Item.PROPERTY_NAME);
		roomName = DatabaseTools.getOptionalString(cursor, Item.ROOM_NAME);

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' / %3$s", id, name, category);
	}
}
