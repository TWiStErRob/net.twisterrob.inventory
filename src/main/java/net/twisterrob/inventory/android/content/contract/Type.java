package net.twisterrob.inventory.android.content.contract;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.inventory.android.content.InventoryContract;

public enum Type {
	Category("category", false, InventoryContract.Category.ITEM_URI),
	Property("property", true, InventoryContract.Property.ITEM_URI),
	Room("room", true, InventoryContract.Room.ITEM_URI),
	Root("root", false, null),
	Item("item", true, InventoryContract.Item.ITEM_URI);

	private final String string;
	private final boolean isMain;
	private final Uri baseUri;

	Type(String string, boolean isMain, Uri baseUri) {
		this.string = string;
		this.isMain = isMain;
		this.baseUri = baseUri;
	}

	public Uri getImageUri(long id) {
		return baseUri != null? ContentUris.withAppendedId(baseUri, id) : null;
	}

	public boolean isMain() {
		return isMain;
	}

	public static Type from(Cursor cursor, String columnName) {
		return Type.from(cursor.getString(cursor.getColumnIndexOrThrow(columnName)));
	}
	public static Type from(String string) {
		for (Type type : values()) {
			if (type.string.equals(string)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Cannot find type for " + string);
	}
}
