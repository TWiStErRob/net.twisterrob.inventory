package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.*;

public class CategoryDTO extends ImagedDTO {
	public long parentID = Category.ID_ADD;

	public static CategoryDTO fromCursor(Cursor cursor) {
		CategoryDTO category = new CategoryDTO();
		return category.fromCursorInternal(cursor);
	}

	@Override
	protected CategoryDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		int parentColumn = cursor.getColumnIndex(Item.PARENT);
		if (parentColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			parentID = cursor.getLong(parentColumn);
		}

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' in %3$d", id, name, parentID);
	}
}
