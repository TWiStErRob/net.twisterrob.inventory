package net.twisterrob.inventory.android.content.contract;

import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.content.model.*;

public enum Type {
	Category("category", false) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Category.imageUri(id);
		}
		@Override public DTO fromCursor(Cursor cursor) {
			return CategoryDTO.fromCursor(cursor);
		}
	},
	Property("property", true) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Property.imageUri(id);
		}
		@Override public DTO fromCursor(Cursor cursor) {
			return PropertyDTO.fromCursor(cursor);
		}
	},
	Room("room", true) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Room.imageUri(id);
		}
		@Override public DTO fromCursor(Cursor cursor) {
			return RoomDTO.fromCursor(cursor);
		}
	},
	Root("root", false) {
		@Override public Uri getImageUri(long id) {
			return null;
		}
		@Override public DTO fromCursor(Cursor cursor) {
			throw new UnsupportedOperationException("No DTO for root.");
		}
	},
	Item("item", true) {
		@Override public Uri getImageUri(long id) {
			return InventoryContract.Item.imageUri(id);
		}
		@Override public DTO fromCursor(Cursor cursor) {
			return ItemDTO.fromCursor(cursor);
		}
	};

	private final String string;
	private final boolean isMain;

	Type(String string, boolean isMain) {
		this.string = string;
		this.isMain = isMain;
	}

	public abstract Uri getImageUri(long id);
	public abstract DTO fromCursor(Cursor cursor); // FIXME add second argument for "old DTO" and allow reuse

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
