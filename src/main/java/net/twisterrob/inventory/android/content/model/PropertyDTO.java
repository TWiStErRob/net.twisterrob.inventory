package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.Property;

public class PropertyDTO {
	public long id = Property.ID_ADD;
	public String name;
	public long type;

	public static PropertyDTO fromCursor(Cursor item) {
		PropertyDTO property = new PropertyDTO();

		int nameColumn = item.getColumnIndex(Property.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			property.name = item.getString(nameColumn);
		}

		int typeColumn = item.getColumnIndex(Property.TYPE);
		if (typeColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			property.type = item.getLong(typeColumn);
		}

		return property;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Property #%1$d: '%2$s' / %3$s", id, name, type);
	}
}
