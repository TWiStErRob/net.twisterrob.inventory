package net.twisterrob.inventory.android.content.model;

import android.database.Cursor;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public class DTO {
	public long id = CommonColumns.ID_ADD;
	public String name;
	public String description;

	public Integer numDirectItems;
	public Integer numAllItems;

	public Integer numDirectChildren;
	public Integer numAllChildren;

	protected DTO fromCursorInternal(@NonNull Cursor cursor) {
		if (cursor.isBeforeFirst() || cursor.isAfterLast()) {
			throw new IllegalArgumentException("Did you forget to advance the cursor? See DatabaseDTOTools.");
		}

		id = DatabaseTools.getOptionalLong(cursor, CommonColumns.ID, CommonColumns.ID_ADD);
		name = DatabaseTools.getOptionalString(cursor, CommonColumns.NAME);
		description = DatabaseTools.getOptionalString(cursor, CommonColumns.DESCRIPTION);

		numDirectChildren = DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_CHILDREN_DIRECT);
		numAllChildren = DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_CHILDREN_ALL);

		numDirectItems = DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_ITEM_DIRECT);
		numAllItems = DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_ITEM_ALL);

		return this;
	}
}
