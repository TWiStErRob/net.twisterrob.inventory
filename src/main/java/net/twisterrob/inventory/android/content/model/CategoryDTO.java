package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.utils.DatabaseUtils;

public class CategoryDTO extends ImagedDTO {
	public long parentID = Category.ID_ADD;
	public String parentName;
	public Integer numDirectSubcategories;
	public Integer numAllSubcategories;
	public Integer numDirectItems;
	public Integer numAllItems;

	public static CategoryDTO fromCursor(Cursor cursor) {
		CategoryDTO category = new CategoryDTO();
		return category.fromCursorInternal(cursor);
	}

	@Override
	protected CategoryDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		parentID = DatabaseUtils.getOptionalLong(cursor, Category.PARENT_ID, Category.ID_ADD);
		parentName = DatabaseUtils.getOptionalString(cursor, Category.PARENT_NAME);
		numDirectSubcategories = DatabaseUtils.getOptionalInt(cursor, Category.COUNT_CHILDREN_DIRECT);
		numAllSubcategories = DatabaseUtils.getOptionalInt(cursor, Category.COUNT_CHILDREN_ALL);
		numDirectItems = DatabaseUtils.getOptionalInt(cursor, Category.ITEM_COUNT_DIRECT);
		numAllItems = DatabaseUtils.getOptionalInt(cursor, Category.ITEM_COUNT_ALL);

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' in %3$d", id, name, parentID);
	}
}
