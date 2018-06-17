package net.twisterrob.inventory.android.content.contract;

import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.inventory.android.content.InventoryContract;

public enum Type {
	Category("category", false) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Category.imageUri(id);
		}
	},
	Property("property", true) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Property.imageUri(id);
		}
	},
	Room("room", true) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Room.imageUri(id);
		}
	},
	Root("root", false) {
		@Override public Uri getImageUri(long id) {
			return null;
		}
	},
	Item("item", true) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Item.imageUri(id);
		}
	};

	private final String string;
	private final boolean isMain;

	Type(String string, boolean isMain) {
		this.string = string;
		this.isMain = isMain;
	}

	public abstract Uri getImageUri(long id);

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
