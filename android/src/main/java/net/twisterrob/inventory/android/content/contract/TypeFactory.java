package net.twisterrob.inventory.android.content.contract;

import android.database.Cursor;

import net.twisterrob.inventory.android.content.model.*;

public class TypeFactory {
	// FIXME add second argument for "old DTO" and allow reuse
	public static DTO fromCursor(Type type, Cursor cursor) {
		switch (type) {
			case Category:
				return CategoryDTO.fromCursor(cursor);
			case Property:
				return PropertyDTO.fromCursor(cursor);
			case Room:
				return RoomDTO.fromCursor(cursor);
			case Root:
				throw new UnsupportedOperationException("No DTO for root.");
			case Item:
				return ItemDTO.fromCursor(cursor);
			default:
				throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}
}
