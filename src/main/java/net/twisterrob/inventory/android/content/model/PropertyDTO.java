package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.Property;

public class PropertyDTO extends ImagedDTO {
	public long id = Property.ID_ADD;
	public String name;
	public long type;

	public PropertyDTO() {
		super.setImageDriveIdColumnName(Property.IMAGE);
		super.setImageDrawableColumnName(Property.TYPE_IMAGE);
	}

	public static PropertyDTO fromCursor(Cursor cursor) {
		PropertyDTO property = new PropertyDTO();
		return property.fromCursorInternal(cursor);
	}

	@Override
	protected PropertyDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		int idColumn = cursor.getColumnIndex(Property.ID);
		if (idColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			id = cursor.getLong(idColumn);
		}

		int nameColumn = cursor.getColumnIndex(Property.NAME);
		if (nameColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			name = cursor.getString(nameColumn);
		}

		int typeColumn = cursor.getColumnIndex(Property.TYPE);
		if (typeColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			type = cursor.getLong(typeColumn);
		}

		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Property #%1$d: '%2$s' / %3$s", id, name, type);
	}
}
