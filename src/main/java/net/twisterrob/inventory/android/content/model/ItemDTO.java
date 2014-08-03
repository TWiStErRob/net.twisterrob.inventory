package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.Item;

public class ItemDTO extends ImagedDTO {
	public long id = Item.ID_ADD;
	public long parentID = Item.ID_ADD;
	public String name;
	public long category;

	public static ItemDTO fromCursor(Cursor cursor) {
		ItemDTO item = new ItemDTO();
		return item.fromCursorInternal(cursor);
	}

	@Override
	protected ItemDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		int idColumn = cursor.getColumnIndex(Item.ID);
		if (idColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			id = cursor.getLong(idColumn);
		}

		int parentColumn = cursor.getColumnIndex(Item.PARENT);
		if (parentColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			parentID = cursor.getLong(parentColumn);
		}

		int nameColumn = cursor.getColumnIndex(Item.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			name = cursor.getString(nameColumn);
		}

		int categoryColumn = cursor.getColumnIndex(Item.CATEGORY);
		if (categoryColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			category = cursor.getLong(categoryColumn);
		}

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' / %3$s", id, name, category);
	}
}
