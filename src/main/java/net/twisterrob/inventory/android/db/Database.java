package net.twisterrob.inventory.android.db;

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.R;

public class Database {
	private final Context m_context;
	private final DatabaseOpenHelper m_helper;

	public Database(Context context) {
		m_context = context;
		m_helper = new DatabaseOpenHelper(m_context, "MagicHomeInventory", 1);
	}

	public SQLiteDatabase getReadableDatabase() {
		return m_helper.getReadableDatabase();
	}
	public SQLiteDatabase getWritableDatabase() {
		return m_helper.getWritableDatabase();
	}

	SQLiteDatabase database;
	SQLiteStatement listPropertiesTypes;

	@SuppressWarnings("unused")
	private void prepareStatements(SQLiteDatabase database) {
		if (database != this.database) {
			this.database = database;
			if (listPropertiesTypes != null) {
				listPropertiesTypes.close();
			}
			listPropertiesTypes = database
					.compileStatement("INSERT INTO Route(region, name, direction, description) VALUES(?, ?, ?, ?);");
		}
	}

	@SuppressWarnings("resource")
	public Cursor listPropertyTypes(CharSequence nameFilter) {
		if (nameFilter == null || nameFilter.toString().trim().isEmpty()) {
			return listPropertyTypes();
		}
		SQLiteDatabase db = getReadableDatabase();
		return db.query(PropertyType.TABLE, new String[]{PropertyType.ID, PropertyType.NAME}, PropertyType.NAME_LIKE,
				new String[]{"%" + nameFilter + "%"}, null, null, PropertyType.DEFAULT_ORDER);
	}
	@SuppressWarnings("resource")
	public Cursor listPropertyTypes() {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(PropertyType.TABLE, new String[]{PropertyType.ID, PropertyType.NAME}, null, null, null, null,
				PropertyType.DEFAULT_ORDER);
	}
	public Map<Integer, String> getPropertyTypes() {
		Map<Integer, String> types = new LinkedHashMap<Integer, String>();
		SQLiteDatabase db = getReadableDatabase();
		try {
			Cursor cursor = listPropertyTypes();
			try {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(cursor.getColumnIndex(PropertyType.ID));
					String name = cursor.getString(cursor.getColumnIndex(PropertyType.NAME));
					types.put(id, name);
				}
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
		return types;
	}

	@SuppressWarnings("resource")
	public Cursor listProperties() {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_properties), null);
	}
}
