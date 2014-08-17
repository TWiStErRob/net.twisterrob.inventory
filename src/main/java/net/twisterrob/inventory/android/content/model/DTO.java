package net.twisterrob.inventory.android.content.model;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public class DTO {
	public long id = CommonColumns.ID_ADD;
	public String name;

	protected DTO fromCursorInternal(Cursor cursor) {
		int idColumn = cursor.getColumnIndex(CommonColumns.ID);
		if (idColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			id = cursor.getLong(idColumn);
		}

		int nameColumn = cursor.getColumnIndex(CommonColumns.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			name = cursor.getString(nameColumn);
		}

		return this;
	}
}
