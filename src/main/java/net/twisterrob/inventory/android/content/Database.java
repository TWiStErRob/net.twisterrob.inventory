package net.twisterrob.inventory.android.content;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.contract.*;

@SuppressWarnings("resource")
public class Database {
	private final Context m_context;
	private final DatabaseOpenHelper m_helper;

	public Database(Context context) {
		m_context = context;
		m_helper = new DatabaseOpenHelper(m_context, "MagicHomeInventory", 1);
		m_helper.setDevMode(false);
	}

	public SQLiteDatabase getReadableDatabase() {
		return m_helper.getReadableDatabase();
	}

	public SQLiteDatabase getWritableDatabase() {
		return m_helper.getWritableDatabase();
	}

	private void execSQL(int queryResource, Object... params) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(m_context.getString(queryResource), params);
	}

	private Cursor rawQuery(int queryResource, Object... params) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(queryResource), StringTools.toStringArray(params));
	}

	private static void bindDriveID(SQLiteStatement insert, int arg, DriveId driveID) {
		if (driveID == null) {
			insert.bindNull(arg);
		} else {
			insert.bindString(arg, driveID.encodeToString());
		}
	}

	public Cursor listPropertyTypes(CharSequence nameFilter) {
		if (nameFilter == null || nameFilter.toString().trim().isEmpty()) {
			return listPropertyTypes();
		}
		return rawQuery(R.string.query_property_types_filtered, "%" + DBTools.escapeLike(nameFilter, '\\') + "%");
	}
	public Cursor listPropertyTypes() {
		return rawQuery(R.string.query_property_types);
	}
	public Cursor listRoomTypes() {
		return rawQuery(R.string.query_room_types);
	}
	public Cursor listProperties() {
		return rawQuery(R.string.query_properties);
	}
	public Cursor getProperty(long propertyID) {
		return rawQuery(R.string.query_property, String.valueOf(propertyID));
	}
	public Cursor listRooms(long propertyID) {
		return rawQuery(R.string.query_rooms, String.valueOf(propertyID));
	}
	public Cursor getRoom(long roomID) {
		return rawQuery(R.string.query_room, String.valueOf(roomID));
	}
	public Cursor listItems(long parentID) {
		return rawQuery(R.string.query_items, String.valueOf(parentID));
	}
	public Cursor getItem(long itemID) {
		return rawQuery(R.string.query_item, String.valueOf(itemID));
	}
	public Cursor listCategories(long parentID) {
		return rawQuery(R.string.query_categories, String.valueOf(parentID));
	}
	public Cursor getCategory(long itemID) {
		return rawQuery(R.string.query_category, String.valueOf(itemID));
	}
	public long newProperty(String name, long type, DriveId imageDriveID) {
		SQLiteDatabase db = getWritableDatabase();

		SQLiteStatement insert = db.compileStatement(m_context.getString(R.string.query_property_new));
		try {
			int arg = 0;
			insert.bindString(++arg, name);
			insert.bindLong(++arg, type);
			bindDriveID(insert, ++arg, imageDriveID);

			return insert.executeInsert();
		} finally {
			insert.close();
		}
	}

	public void updateProperty(long id, String name, long type, DriveId imageDriveID) {
		execSQL(R.string.query_property_update, name, type, imageDriveID.encodeToString(), id);
	}

	public void deleteProperty(long id) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			// TODO delete all items recursively from all rooms?
			execSQL(R.string.query_property_delete_rooms, id);
			execSQL(R.string.query_property_delete, id);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public long newRoom(long propertyID, String name, long type, DriveId imageDriveID) {
		SQLiteDatabase db = getWritableDatabase();
		SQLiteStatement insert = db.compileStatement(m_context.getString(R.string.query_room_new));
		db.beginTransaction();
		try {
			long rootID = newItem(null, Item.ROOM_ROOT, Category.INTERNAL, null);

			int arg = 0;
			insert.bindLong(++arg, propertyID);
			insert.bindLong(++arg, rootID);
			insert.bindString(++arg, name);
			insert.bindLong(++arg, type);
			bindDriveID(insert, ++arg, imageDriveID);
			long roomID = insert.executeInsert();
			db.setTransactionSuccessful();
			return roomID;
		} finally {
			insert.close();
			db.endTransaction();
		}
	}

	public void updateRoom(long id, String name, long type, DriveId imageDriveID) {
		execSQL(R.string.query_room_update, name, type, imageDriveID.encodeToString(), id);
	}

	public void deleteRoom(long id) {
		execSQL(R.string.query_room_delete, id);
		// TODO delete all items
	}

	public long newItem(long parentID, String name, long category, DriveId imageDriveID) {
		return newItem((Long)parentID, name, category, imageDriveID);
	}

	private long newItem(Long parentID, String name, long category, DriveId imageDriveID) {
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
			bindDriveID(insert, ++arg, imageDriveID);

			return insert.executeInsert();
		} finally {
			insert.close();
		}
	}

	public void updateItem(long id, String name, long category, DriveId imageDriveID) {
		execSQL(R.string.query_item_update, name, category, imageDriveID, id);
	}

	public void deleteItem(long id) {
		execSQL(R.string.query_item_delete, id);
		// TODO delete all items
	}
}
