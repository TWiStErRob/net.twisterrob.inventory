package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.content.contract.ParentColumns;
import net.twisterrob.inventory.android.content.contract.ParentColumns.Type;

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
		Uri baseUri = getBaseUri();
		if (baseUri != null) {
			ContentUris.withAppendedId(baseUri, this.id);
		}
		return null;
	}

	private Uri getBaseUri() {
		switch (parentType) {
			case Category:
				return InventoryContract.Category.ITEM_URI;
			case Property:
				return InventoryContract.Property.ITEM_URI;
			case Room:
				return InventoryContract.Room.ITEM_URI;
			case Item:
				return InventoryContract.Item.ITEM_URI;
		}
		return null;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "Parent %3$s #%1$d: '%2$s'", id, name, parentType);
	}
}

