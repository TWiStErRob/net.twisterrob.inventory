package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.inventory.android.content.contract.*;

public class ParentDTO extends ImagedDTO {
	public Type parentType;

	public static ParentDTO fromCursor(Cursor cursor) {
		ParentDTO item = new ParentDTO();
		return item.fromCursorInternal(cursor);
	}

	@Override
	protected ParentDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		id = cursor.getLong(cursor.getColumnIndexOrThrow(ParentColumns.ID));
		parentType = Type.from(cursor.getString(cursor.getColumnIndexOrThrow(ParentColumns.PARENT_TYPE)));
		name = cursor.getString(cursor.getColumnIndexOrThrow(ParentColumns.NAME));

		return this;
	}
	@Override protected Uri getImageUri() {
		return parentType.getImageUri(this.id);
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Parent %3$s #%1$d: '%2$s'", id, name, parentType);
	}
}

