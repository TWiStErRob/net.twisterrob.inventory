package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.Property;

public class PropertyDTO {
	public long id = Property.ID_ADD;
	public String name;
	public long type;

	public static PropertyDTO fromCursor(Cursor item) {
		PropertyDTO property = new PropertyDTO();
		property.name = item.getString(item.getColumnIndexOrThrow(Property.NAME));
		property.type = item.getLong(item.getColumnIndexOrThrow(Property.TYPE));
		return property;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Property #%1$d: '%2$s' / %3$s", id, name, type);
	}
}
