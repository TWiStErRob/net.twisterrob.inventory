package net.twisterrob.inventory.android.content;

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.contract.PropertyType;

@SuppressWarnings("resource")
public class Database {
	private final Context m_context;
	private final DatabaseOpenHelper m_helper;

	public Database(Context context) {
		m_context = context;
		m_helper = new DatabaseOpenHelper(m_context, "MagicHomeInventory", 1);
		m_helper.setDevMode(true);
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

	public Cursor listPropertyTypes(CharSequence nameFilter) {
		if (nameFilter == null || nameFilter.toString().trim().isEmpty()) {
			return listPropertyTypes();
		}
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_property_types_filtered), new String[]{"%" + nameFilter
				+ "%"});
	}
	public Cursor listPropertyTypes() {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_property_types), null);
	}
	public Cursor listRoomTypes() {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_room_types), null);
	}
	public Map<Integer, String> getPropertyTypes() {
		Map<Integer, String> types = new LinkedHashMap<Integer, String>();
		SQLiteDatabase db = getReadableDatabase();
		try {
			Cursor cursor = listPropertyTypes();
			try {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(PropertyType.ID));
					String name = cursor.getString(cursor.getColumnIndexOrThrow(PropertyType.NAME));
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

	public Cursor listProperties() {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_properties), null);
	}

	public Cursor getProperty(long propertyID) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_property), new String[]{String.valueOf(propertyID)});
	}

	public Cursor listRooms(long propertyID) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_rooms), new String[]{String.valueOf(propertyID)});
	}
	public Cursor getRoom(long roomID) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_room), new String[]{String.valueOf(roomID)});
	}

	public Cursor listItems(long parentID) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_items), new String[]{String.valueOf(parentID)});
	}
	public Cursor getItem(long itemID) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(R.string.query_item), new String[]{String.valueOf(itemID)});
	}

	public long newProperty(String name, long type) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{name, String.valueOf(type)};
		db.execSQL(m_context.getString(R.string.query_property_new), params);
		return 0;
	}

	public void updateProperty(long id, String name, long type) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{name, String.valueOf(type), String.valueOf(id)};
		db.execSQL(m_context.getString(R.string.query_property_update), params);
	}

	public void deleteProperty(long id) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{String.valueOf(id)};
		db.execSQL(m_context.getString(R.string.query_property_delete_rooms), params);
		db.execSQL(m_context.getString(R.string.query_property_delete), params);
	}

	public long newRoom(long propertyID, String name, long type) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{String.valueOf(propertyID), name, String.valueOf(type)};
		db.execSQL(m_context.getString(R.string.query_room_new), params);
		return 0;
	}

	public void updateRoom(long id, String name, long type) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{name, String.valueOf(type), String.valueOf(id)};
		db.execSQL(m_context.getString(R.string.query_room_update), params);
	}

	public void deleteRoom(long id) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{String.valueOf(id)};
		db.execSQL(m_context.getString(R.string.query_room_delete), params);
	}
}
