package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.*;

public class CategoryDTO extends ImagedDTO {
	public long id = Category.ID_ADD;
	public long parentID = Category.ID_ADD;
	public String name;

	public static CategoryDTO fromCursor(Cursor cursor) {
		CategoryDTO category = new CategoryDTO();
		return category.fromCursorInternal(cursor);
	}

	@Override
	protected CategoryDTO fromCursorInternal(Cursor cursor) {
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

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' in %3$d", id, name, parentID);
	}
}
