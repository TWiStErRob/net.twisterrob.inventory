package net.twisterrob.inventory.android.content.model;

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
}
