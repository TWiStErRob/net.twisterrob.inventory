package net.twisterrob.android.inventory;

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

public class Dao {
	private final Context m_context;
	private final DatabaseOpenHelper m_helper;

	public Dao(Context context) {
		m_context = context;
		m_helper = new DatabaseOpenHelper(m_context);
	}

	public SQLiteDatabase getReadableDatabase() {
		return m_helper.getReadableDatabase();
	}
	public SQLiteDatabase getWritableDatabase() {
		return m_helper.getWritableDatabase();
	}

	SQLiteDatabase database;
	SQLiteStatement listBuildingTypes;

	@SuppressWarnings("unused")
	private void prepareStatements(SQLiteDatabase database) {
		if (database != this.database) {
			this.database = database;
			if (listBuildingTypes != null) {
				listBuildingTypes.close();
			}
			listBuildingTypes = database
					.compileStatement("INSERT INTO Route(region, name, direction, description) VALUES(?, ?, ?, ?);");
		}
	}

	@SuppressWarnings("resource")
	public Cursor listBuildingTypes(CharSequence nameFilter) {
		if (nameFilter == null || nameFilter.toString().trim().isEmpty()) {
			return listBuildingTypes();
		}
		SQLiteDatabase db = getReadableDatabase();
		return db.query(BuildingType.TABLE, new String[]{BuildingType.ID, BuildingType.NAME}, BuildingType.NAME_LIKE,
				new String[]{"%" + nameFilter + "%"}, null, null, BuildingType.DEFAULT_ORDER);
	}
	@SuppressWarnings("resource")
	public Cursor listBuildingTypes() {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(BuildingType.TABLE, new String[]{BuildingType.ID, BuildingType.NAME}, null, null, null, null,
				BuildingType.DEFAULT_ORDER);
	}
	public Map<Integer, String> getBuildingTypes() {
		Map<Integer, String> types = new LinkedHashMap<Integer, String>();
		SQLiteDatabase db = getReadableDatabase();
		try {
			Cursor cursor = listBuildingTypes();
			try {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(cursor.getColumnIndex(BuildingType.ID));
					String name = cursor.getString(cursor.getColumnIndex(BuildingType.NAME));
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
	public Cursor listBuildings() {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_buildings), null);
	}
}
