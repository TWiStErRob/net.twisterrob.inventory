package net.twisterrob.inventory.android.content.model;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

public class Types {
	private final Map<String, Long> types = new HashMap<>();
	private static final Map<String, String> DEPRECATED = Collections.unmodifiableMap(buildDeprecatedMap());

	static Map<String, String> buildDeprecatedMap() {
		Map<String, String> map = new HashMap<>();
		map.put("category_storage_disccases", "category_storage_cases"); // XXX remove
		map.put("category_storage", "category_storage_group"); // XXX remove
		return map;
	}

	public Types() {
		putTypes(App.db().listPropertyTypes(), PropertyType.ID, PropertyType.NAME);
		putTypes(App.db().listRoomTypes(), RoomType.ID, RoomType.NAME);
		putTypes(App.db().listItemCategories(), Category.ID, Category.NAME);
	}

	public Long getID(String type) {
		Long id = types.get(type);
		if (id == null) {
			id = types.get(DEPRECATED.get(type));
		}
		return id;
	}

	private void putTypes(Cursor cursor, String idColumn, String nameColumn) {
		try {
			while (cursor.moveToNext()) {
				long id = cursor.getLong(cursor.getColumnIndexOrThrow(idColumn));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn));
				Long existing = types.put(name, id);
				if (existing != null) {
					throw new IllegalStateException(
							"Duplicate type: " + name + " existing: " + existing + " new: " + id);
				}
			}
		} finally {
			cursor.close();
		}
	}
}
