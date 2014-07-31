package net.twisterrob.inventory.android.content;

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.contract.*;

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

		SQLiteStatement insert = db.compileStatement(m_context.getString(R.string.query_property_new));
		try {
			int arg = 0;
			insert.bindString(++arg, name);
			insert.bindLong(++arg, type);

			return insert.executeInsert();
		} finally {
			insert.close();
		}
	}

	public void updateProperty(long id, String name, long type) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{name, String.valueOf(type), String.valueOf(id)};
		db.execSQL(m_context.getString(R.string.query_property_update), params);
	}

	public void deleteProperty(long id) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			String[] params = new String[]{String.valueOf(id)};
			// TODO delete all items recursively from all rooms?
			db.execSQL(m_context.getString(R.string.query_property_delete_rooms), params);
			db.execSQL(m_context.getString(R.string.query_property_delete), params);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public long newRoom(long propertyID, String name, long type) {
		SQLiteDatabase db = getWritableDatabase();
		SQLiteStatement insert = db.compileStatement(m_context.getString(R.string.query_room_new));
		db.beginTransaction();
		try {
			long rootID = newItem(null, Item.ROOM_ROOT, Category.INTERNAL);

			int arg = 0;
			insert.bindLong(++arg, propertyID);
			insert.bindLong(++arg, rootID);
			insert.bindString(++arg, name);
			insert.bindLong(++arg, type);
			long roomID = insert.executeInsert();
			db.setTransactionSuccessful();
			return roomID;
		} finally {
			insert.close();
			db.endTransaction();
		}
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
		// TODO delete all items
	}

	public long newItem(long parentID, String name, long category) {
		return newItem((Long)parentID, name, category);
	}

	private long newItem(Long parentID, String name, long category) {
		SQLiteDatabase db = getWritableDatabase();
		SQLiteStatement insert = db.compileStatement(m_context.getString(R.string.query_item_new));
		try {
			int arg = 0;
			if (parentID != null) {
				insert.bindLong(++arg, parentID);
			} else {
				insert.bindNull(++arg);
			}
			insert.bindString(++arg, name);
			insert.bindLong(++arg, category);

			return insert.executeInsert();
		} finally {
			insert.close();
		}
	}

	public void updateItem(long id, String name, long category) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{name, String.valueOf(category), String.valueOf(id)};
		db.execSQL(m_context.getString(R.string.query_item_update), params);
	}

	public void deleteItem(long id) {
		SQLiteDatabase db = getWritableDatabase();
		String[] params = new String[]{String.valueOf(id)};
		db.execSQL(m_context.getString(R.string.query_item_delete), params);
		// TODO delete all items
	}
}
