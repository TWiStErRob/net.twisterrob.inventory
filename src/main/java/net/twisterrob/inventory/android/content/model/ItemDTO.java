package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.utils.DatabaseUtils;

public class ItemDTO extends ImagedDTO {
	public long parentID = Item.ID_ADD;
	public String parentName;
	public long category;
	public String categoryName;

	public static ItemDTO fromCursor(Cursor cursor) {
		ItemDTO item = new ItemDTO();
		return item.fromCursorInternal(cursor);
	}

	@Override
	protected ItemDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		parentID = DatabaseUtils.getOptionalLong(cursor, Item.PARENT_ID, Item.ID_ADD);
		parentName = DatabaseUtils.getOptionalString(cursor, Item.PARENT_NAME);
		category = DatabaseUtils.getOptionalLong(cursor, Item.CATEGORY, Category.ID_ADD);
		categoryName = DatabaseUtils.getOptionalString(cursor, Item.CATEGORY_NAME);

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' / %3$s", id, name, category);
	}
}
