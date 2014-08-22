package net.twisterrob.inventory.android.content;

import java.util.Arrays;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources;
import android.database.*;
import android.database.sqlite.*;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Prefs;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.java.utils.StringTools;

public class Database {
	private static final Logger LOG = LoggerFactory.getLogger(Database.class);

	private final Context m_context;
	private final Resources m_resources;
	private final DatabaseOpenHelper m_helper;

	public Database(Context context) {
		m_context = context;
		m_resources = context.getResources();
		m_helper = new DatabaseOpenHelper(context, "MagicHomeInventory", 1) {
			@Override
			public void onConfigure(SQLiteDatabase db) {
				super.onConfigure(db);
				db.execSQL("PRAGMA recursive_triggers = TRUE;");
			}
		};
		m_helper.setDevMode(false);
		App.getPrefEditor().remove(Prefs.CURRENT_LANGUAGE).apply();
		m_helper.setDumpOnOpen(true);
	}

	public SQLiteDatabase getReadableDatabase() {
		return m_helper.getReadableDatabase();
	}

	public SQLiteDatabase getWritableDatabase() {
		return m_helper.getWritableDatabase();
	}

	private void execSQL(int queryResource, Object... params) {
		execSQL(getWritableDatabase(), queryResource, params);
	}
	private void execSQL(SQLiteDatabase db, int queryResource, Object... params) {
		LOG.trace("execSQL({}, {})", m_resources.getResourceEntryName(queryResource), Arrays.toString(params));
		db.execSQL(m_resources.getString(queryResource), params);
	}

	private Cursor rawQuery(int queryResource, Object... params) {
		return rawQuery(getReadableDatabase(), queryResource, params);
	}
	private Cursor rawQuery(SQLiteDatabase db, int queryResource, Object... params) {
		LOG.trace("rawQuery({}, {})", m_resources.getResourceEntryName(queryResource), Arrays.toString(params));
		return db.rawQuery(m_resources.getString(queryResource), StringTools.toStringArray(params));
	}
	private Long getID(int queryResource, Object... params) {
		Cursor cursor = rawQuery(queryResource, params);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getLong(0);
			} else {
				return null;
			}
		} finally {
			cursor.close();
		}
	}

	@SuppressWarnings("resource")
	private long rawInsert(int insertResource, Object... params) {
		LOG.trace("rawInsert({}, {})", m_resources.getResourceEntryName(insertResource), Arrays.toString(params));
		SQLiteDatabase db = getWritableDatabase();

		SQLiteStatement insert = db.compileStatement(m_resources.getString(insertResource));
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
	public Cursor listRooms() {
		return rawQuery(R.string.query_rooms);
	}
	public Cursor listRooms(long propertyID) {
		return rawQuery(R.string.query_rooms_by_property, propertyID);
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

	public long createProperty(String name, long type, DriveId imageDriveID) {
		return rawInsert(R.string.query_property_create, name, type, imageDriveID);
	}
	public Long findProperty(String name) {
		return getID(R.string.query_property_find, name);
	}
	public void updateProperty(long id, String name, long type, DriveId imageDriveID) {
		execSQL(R.string.query_property_update, name, type, imageDriveID, id);
	}
	public void deleteProperty(long id) {
		execSQL(R.string.query_property_delete, id);
	}

	public long createRoom(long propertyID, String name, long type, DriveId imageDriveID) {
		rawInsert(R.string.query_room_create, propertyID, name, type, imageDriveID);
		return findRoom(propertyID, name); // last_insert_rowid() doesn't work with INSTEAD OF INSERT triggers on VIEWs 
	}
	public Long findRoom(long propertyID, String name) {
		return getID(R.string.query_room_find, propertyID, name);
	}
	public void updateRoom(long id, String name, long type, DriveId imageDriveID) {
		execSQL(R.string.query_room_update, name, type, imageDriveID, id);
	}
	public void deleteRoom(long id) {
		execSQL(R.string.query_room_delete, id);
	}

	private long createItem(Long parentID, String name, long category, DriveId imageDriveID) {
		return rawInsert(R.string.query_item_create, parentID, name, category, imageDriveID);
	}
	public long createItem(long parentID, String name, long category, DriveId imageDriveID) {
		return createItem((Long)parentID, name, category, imageDriveID);
	}
	public Long findItem(long parentID, String name) {
		return getID(R.string.query_item_find, parentID, name);
	}
	public void updateItem(long id, String name, long category, DriveId imageDriveID) {
		execSQL(R.string.query_item_update, name, category, imageDriveID, id);
	}
	public void deleteItem(long id) {
		execSQL(R.string.query_item_delete, id);
	}

	public Cursor searchSuggest(String query) {
		query = fixQuery(query);
		return rawQuery(R.string.query_search_suggest, query);
	}

	public Cursor search(String query) {
		query = fixQuery(query);
		return rawQuery(R.string.query_search, query);
	}

	private String fixQuery(String query) {
		if (!query.matches(".*[\\s\\*].*")) {
			query += "*";
		}
		return query;
	}

	public void updateCategoryCache() {
		LOG.info("Updating category name cache");
		@SuppressWarnings("resource")
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			Cursor cursor = rawQuery(db, R.string.query_category_cache_names);
			try {
				while (cursor.moveToNext()) {
					String resourceName = cursor.getString(0);
					String displayName = String.valueOf(AndroidTools.getText(m_context, resourceName));
					execSQL(db, R.string.query_category_cache_update, displayName, resourceName);
				}
				db.setTransactionSuccessful();
			} finally {
				cursor.close();
			}
		} finally {
			db.endTransaction();
		}
	}

	public Cursor export() {
		return rawQuery(R.string.query_export);
	}
}
