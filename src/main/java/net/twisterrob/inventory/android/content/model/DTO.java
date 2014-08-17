package net.twisterrob.inventory.android.content.model;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.utils.DatabaseUtils;

public class DTO {
	public long id = CommonColumns.ID_ADD;
	public String name;

	protected DTO fromCursorInternal(Cursor cursor) {
		id = DatabaseUtils.getOptionalLong(cursor, CommonColumns.ID, CommonColumns.ID_ADD);
		name = DatabaseUtils.getOptionalString(cursor, CommonColumns.NAME);

		return this;
	}
}
