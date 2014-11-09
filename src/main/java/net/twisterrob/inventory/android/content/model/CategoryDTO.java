package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.content.contract.Category;

public class CategoryDTO extends ImagedDTO {
	public long parentID = Category.ID_ADD;
	public String parentName;

	public static CategoryDTO fromCursor(Cursor cursor) {
		CategoryDTO category = new CategoryDTO();
		return category.fromCursorInternal(cursor);
	}

	@Override
	protected CategoryDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		parentID = DatabaseTools.getOptionalLong(cursor, Category.PARENT_ID, Category.ID_ADD);
		parentName = DatabaseTools.getOptionalString(cursor, Category.PARENT_NAME);

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' in %3$d", id, name, parentID);
	}
}
