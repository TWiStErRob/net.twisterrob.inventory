package net.twisterrob.inventory.android.content.model;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.utils.DatabaseUtils;

public class DTO {
	public long id = CommonColumns.ID_ADD;
	public String name;

	public Integer numDirectItems;
	public Integer numAllItems;

	public Integer numDirectChildren;
	public Integer numAllChildren;

	protected DTO fromCursorInternal(Cursor cursor) {
		id = DatabaseUtils.getOptionalLong(cursor, CommonColumns.ID, CommonColumns.ID_ADD);
		name = DatabaseUtils.getOptionalString(cursor, CommonColumns.NAME);

		numDirectChildren = DatabaseUtils.getOptionalInt(cursor, CommonColumns.COUNT_CHILDREN_DIRECT);
		numAllChildren = DatabaseUtils.getOptionalInt(cursor, CommonColumns.COUNT_CHILDREN_ALL);

		numDirectItems = DatabaseUtils.getOptionalInt(cursor, CommonColumns.COUNT_ITEM_DIRECT);
		numAllItems = DatabaseUtils.getOptionalInt(cursor, CommonColumns.COUNT_ITEM_ALL);

		return this;
	}
}
