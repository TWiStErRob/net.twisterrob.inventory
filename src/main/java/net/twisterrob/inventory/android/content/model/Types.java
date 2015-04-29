package net.twisterrob.inventory.android.content.model;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

public class Types {
	private final Map<String, Long> types = new HashMap<>();
	public Types() {
		putTypes(App.db().listPropertyTypes(), PropertyType.ID, PropertyType.NAME);
		putTypes(App.db().listRoomTypes(), RoomType.ID, RoomType.NAME);
		putTypes(App.db().listItemCategories(), Category.ID, Category.NAME);

		// XXX remove these for first release
		// types.put("category_old", "category_new"); // transitive must be resolved manually
		types.put("category_storage_disccases", types.get("category_storage_cases"));
		types.put("category_storage", types.get("category_group"));
		types.put("category_cleaning", types.get("category_tools_cleaning"));
		types.put("category_cleaning_chemicals", types.get("category_tools_chemicals"));
		types.put("category_cleaning_equipment", types.get("category_tools_cleaning"));
	}

	public Long getID(String type) {
		return types.get(type);
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
