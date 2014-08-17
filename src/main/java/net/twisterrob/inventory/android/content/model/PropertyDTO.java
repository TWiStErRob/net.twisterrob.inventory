package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.utils.DatabaseUtils;

public class PropertyDTO extends ImagedDTO {
	public long type = PropertyType.DEFAULT;

	public static PropertyDTO fromCursor(Cursor cursor) {
		PropertyDTO property = new PropertyDTO();
		return property.fromCursorInternal(cursor);
	}

	@Override
	protected PropertyDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		type = DatabaseUtils.getOptionalLong(cursor, Property.TYPE, PropertyType.DEFAULT);

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Property #%1$d: '%2$s' / %3$s", id, name, type);
	}
}
