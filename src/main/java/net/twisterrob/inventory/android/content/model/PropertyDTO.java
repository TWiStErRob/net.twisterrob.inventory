package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.Property;

public class PropertyDTO {
	public long id = Property.ID_ADD;
	public String name;
	public long type;

	public static PropertyDTO fromCursor(Cursor cursor) {
		PropertyDTO property = new PropertyDTO();

		int idColumn = cursor.getColumnIndex(Property.ID);
		if (idColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			property.id = cursor.getLong(idColumn);
		}

		int nameColumn = cursor.getColumnIndex(Property.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			property.name = cursor.getString(nameColumn);
		}

		int typeColumn = cursor.getColumnIndex(Property.TYPE);
		if (typeColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			property.type = cursor.getLong(typeColumn);
		}

		return property;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Property #%1$d: '%2$s' / %3$s", id, name, type);
	}
}
