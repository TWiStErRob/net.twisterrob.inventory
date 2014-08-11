package net.twisterrob.inventory.android.content;

import android.content.Context;
import android.database.*;
import android.database.sqlite.*;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.*;
import net.twisterrob.android.db.DatabaseOpenHelper.HelperHooks;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.contract.*;

public class Database {
	private final Context m_context;
	private final DatabaseOpenHelper m_helper;

	public Database(Context context) {
		m_context = context;
		m_helper = new DatabaseOpenHelper(m_context, "MagicHomeInventory", 1, new HelperHooks() {
			public void onConfigure(SQLiteDatabase db) {
				db.execSQL("PRAGMA recursive_triggers = TRUE;");
			}
		});
		m_helper.setDevMode(true);
		m_helper.setDumpOnOpen(true);
	}

	public SQLiteDatabase getReadableDatabase() {
		return m_helper.getReadableDatabase();
	}

	public SQLiteDatabase getWritableDatabase() {
		return m_helper.getWritableDatabase();
	}

	private void execSQL(int queryResource, Object... params) {
		@SuppressWarnings("resource")
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(m_context.getString(queryResource), params);
	}

	@SuppressWarnings("resource")
	private Cursor rawQuery(int queryResource, Object... params) {
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(m_context.getString(queryResource), StringTools.toStringArray(params));
	}

	@SuppressWarnings("resource")
	private long rawInsert(int insertResource, Object... params) {
		SQLiteDatabase db = getWritableDatabase();

		SQLiteStatement insert = db.compileStatement(m_context.getString(insertResource));
		try {
			for (int i = 0; i < params.length; ++i) {
				DatabaseUtils.bindObjectToProgram(insert, i + 1, params[i]);
			}
			return insert.executeInsert();
		} finally {
			insert.close();
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
		return rawQuery(R.string.query_property, propertyID);
	}
	public Cursor listRooms(long propertyID) {
		return rawQuery(R.string.query_rooms, propertyID);
	}
	public Cursor getRoom(long roomID) {
		return rawQuery(R.string.query_room, roomID);
	}
	public Cursor listItemCategories() {
		return rawQuery(R.string.query_item_categories, Category.INTERNAL);
	}
	public Cursor listItems(long parentID) {
		return rawQuery(R.string.query_items, parentID);
	}
	public Cursor listItemsInRoom(long roomID) {
		return rawQuery(R.string.query_items_in_room, roomID);
	}
	public Cursor listItemsForCategory(long categoryID, boolean include) {
		if (include) {
			return rawQuery(R.string.query_items_in_category, categoryID);
		} else {
			return rawQuery(R.string.query_items_by_category, categoryID);
		}
	}
	public Cursor getItem(long itemID) {
		return rawQuery(R.string.query_item, itemID);
	}
	public Cursor listCategories(long parentID) {
		return rawQuery(R.string.query_categories, parentID);
	}
	public Cursor getCategory(long itemID) {
		return rawQuery(R.string.query_category, itemID);
	}
	public long newProperty(String name, long type, DriveId imageDriveID) {
		return rawInsert(R.string.query_property_new, name, type, imageDriveID);
	}

	public void updateProperty(long id, String name, long type, DriveId imageDriveID) {
		execSQL(R.string.query_property_update, name, type, imageDriveID, id);
	}

	public void deleteProperty(long id) {
		execSQL(R.string.query_property_delete, id);
	}

	@SuppressWarnings("resource")
	public long newRoom(long propertyID, String name, long type, DriveId imageDriveID) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			long rootID = newItem(null, Item.ROOM_ROOT, Category.INTERNAL, null);
			long roomID = rawInsert(R.string.query_room_new, propertyID, rootID, name, type, imageDriveID);
			db.setTransactionSuccessful();
			return roomID;
		} finally {
			db.endTransaction();
		}

	}

	public void updateRoom(long id, String name, long type, DriveId imageDriveID) {
		execSQL(R.string.query_room_update, name, type, imageDriveID, id);
	}

	public void deleteRoom(long id) {
		execSQL(R.string.query_room_delete, id);
	}

	public long newItem(long parentID, String name, long category, DriveId imageDriveID) {
		return newItem((Long)parentID, name, category, imageDriveID);
	}

	private long newItem(Long parentID, String name, long category, DriveId imageDriveID) {
		return rawInsert(R.string.query_item_new, parentID, name, category, imageDriveID);
	}

	public void updateItem(long id, String name, long category, DriveId imageDriveID) {
		execSQL(R.string.query_item_update, name, category, imageDriveID, id);
	}

	public void deleteItem(long id) {
		execSQL(R.string.query_item_delete, id);
	}

	public Cursor search(String query) {
		if (!query.matches(".*[\\s\\*].*")) {
			query += "*";
		}
		return rawQuery(R.string.query_search, query);
	}
}
