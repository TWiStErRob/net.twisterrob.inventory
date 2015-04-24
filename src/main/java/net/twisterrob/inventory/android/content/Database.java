package net.twisterrob.inventory.android.content;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.StringRes;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;

public class Database extends VariantDatabase {
	private static final Logger LOG = LoggerFactory.getLogger(Database.class);

	private final DatabaseOpenHelper m_helper;

	public Database(Context context) {
		super(context);
		m_helper = new DatabaseOpenHelper(context, "MagicHomeInventory", 5) { // FIXME reset to 1 before release
			@Override
			public void onConfigure(SQLiteDatabase db) {
				super.onConfigure(db);
				db.execSQL("PRAGMA recursive_triggers = TRUE;");
			}
		};
		// TODO App.getPrefEditor().remove(Prefs.CURRENT_LANGUAGE).apply();
		m_helper.setDevMode(BuildConfig.DEBUG);
	}

	public SQLiteDatabase getReadableDatabase() {
		return m_helper.getReadableDatabase();
	}

	public SQLiteDatabase getWritableDatabase() {
		return m_helper.getWritableDatabase();
	}

	public DatabaseOpenHelper getHelper() {
		return m_helper;
	}

	public Database beginTransaction() {
		getWritableDatabase().beginTransaction();
		return this;
	}
	public void endTransaction() {
		getWritableDatabase().endTransaction();
	}
	public void setTransactionSuccessful() {
		getWritableDatabase().setTransactionSuccessful();
	}

	private void execSQL(@StringRes int queryResource, Object... params) {
		execSQL(getWritableDatabase(), queryResource, params);
	}

	private Cursor rawQuery(@StringRes int queryResource, Object... params) {
		return rawQuery(getReadableDatabase(), queryResource, params);
	}

	private long rawInsert(@StringRes int queryResource, Object... params) {
		return rawInsert(getReadableDatabase(), queryResource, params);
	}

	private Long getID(@StringRes int queryResource, Object... params) {
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

	// Filtered example
	// where pt.name like ? escape '\'
	// return rawQuery(R.string.query_..., "%" + DatabaseTools.escapeLike(nameFilter, '\\') + "%");

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
		return rawQuery(R.string.query_rooms, null, null);
	}
	public Cursor listRooms(long propertyID) {
		return rawQuery(R.string.query_rooms, propertyID, propertyID);
	}
	public Cursor getRoom(long roomID) {
		return rawQuery(R.string.query_room, roomID);
	}
	public Cursor listItemCategories() {
		return rawQuery(R.string.query_item_categories);
	}
	public Cursor listItems(long parentID) {
		return rawQuery(R.string.query_items_by_item, parentID, parentID, parentID);
	}
	public Cursor listItemsInRoom(long roomID) {
		return rawQuery(R.string.query_items_by_room, roomID);
	}
	public Cursor listItemsInList(long listID) {
		return rawQuery(R.string.query_items_by_list, listID);
	}
	public Cursor listItems() {
		return rawQuery(R.string.query_items);
	}
	public Cursor listItemsForCategory(long categoryID, boolean include) {
		if (include) {
			return rawQuery(R.string.query_items_in_category, categoryID);
		} else {
			return rawQuery(R.string.query_items_by_category, categoryID);
		}
	}
	public Cursor listItemParents(long itemID) {
		return rawQuery(R.string.query_item_parents, itemID);
	}

	public Cursor getItem(long itemID, boolean addToRecents) {
		if (addToRecents) {
			execSQL(R.string.query_recent_add, itemID);
		}
		return rawQuery(R.string.query_item, itemID, itemID);
	}
	public Cursor listCategories(Long parentCategoryID) {
		return rawQuery(R.string.query_categories, parentCategoryID, parentCategoryID);
	}
	public Cursor getCategory(long itemID) {
		return rawQuery(R.string.query_category, itemID);
	}

	public long createProperty(long type, String name, String description) {
		return rawInsert(R.string.query_property_create, type, name, description);
	}
	public Long findProperty(String name) {
		return getID(R.string.query_property_find, name);
	}
	public void updateProperty(long id, long type, String name, String description) {
		execSQL(R.string.query_property_update, type, name, description, id);
	}
	public void setPropertyImage(long id, byte[] imageContents, Long time) {
		if (time != null) {
			execSQL(R.string.query_property_image_set_with_time, imageContents, time, id);
		} else {
			execSQL(R.string.query_property_image_set, imageContents, id);
		}
	}
	public Cursor getPropertyImage(long id) {
		return rawQuery(R.string.query_property_image_get, id);
	}
	public void deleteProperty(long id) {
		execSQL(R.string.query_property_delete, id);
	}

	public long createRoom(long propertyID, long type, String name, String description) {
		rawInsert(R.string.query_room_create, propertyID, type, name, description);
		return findRoom(propertyID, name); // last_insert_rowid() doesn't work with INSTEAD OF INSERT triggers on VIEWs
	}
	public Long findRoom(long propertyID, String name) {
		return getID(R.string.query_room_find, propertyID, name);
	}
	public void updateRoom(long id, long type, String name, String description) {
		execSQL(R.string.query_room_update, type, name, description, id);
	}
	public void setRoomImage(long id, byte[] imageContents, Long time) {
		if (time != null) {
			execSQL(R.string.query_room_image_set_with_time, imageContents, time, id);
		} else {
			execSQL(R.string.query_room_image_set, imageContents, id);
		}
	}
	public Cursor getRoomImage(long id) {
		return rawQuery(R.string.query_room_image_get, id);
	}
	public void deleteRoom(long id) {
		execSQL(R.string.query_room_delete, id);
	}
	public void moveRoom(long id, long propertyID) {
		execSQL(R.string.query_room_move, propertyID, id);
	}
	public void moveRooms(long propertyID, long[] roomIDs) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.beginTransaction();
			for (long roomID : roomIDs) {
				execSQL(db, R.string.query_room_move, propertyID, roomID);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private long createItem(Long parentID, long category, String name, String description) {
		return rawInsert(R.string.query_item_create, parentID, category, name, description);
	}
	public long createItem(long parentID, long category, String name, String description) {
		return createItem((Long)parentID, category, name, description);
	}
	public Long findItem(long parentID, String name) {
		return getID(R.string.query_item_find, parentID, name);
	}
	public void updateItem(long id, long category, String name, String description) {
		execSQL(R.string.query_item_update, category, name, description, id);
	}
	public void setItemImage(long id, byte[] imageContents, Long time) {
		if (time != null) {
			execSQL(R.string.query_item_image_set_with_time, imageContents, time, id);
		} else {
			execSQL(R.string.query_item_image_set, imageContents, id);
		}
	}
	public Cursor getItemImage(long id) {
		return rawQuery(R.string.query_item_image_get, id);
	}
	public void deleteItem(long id) {
		execSQL(R.string.query_item_delete, id);
	}
	public void moveItem(long id, long parentID) {
		execSQL(R.string.query_item_move, parentID, id);
	}
	public void moveItems(long parentID, long[] itemIDs) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.beginTransaction();
			for (long itemID : itemIDs) {
				execSQL(db, R.string.query_item_move, parentID, itemID);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public Cursor listLists(long itemID) {
		return rawQuery(R.string.query_list_list, itemID, itemID);
	}
	public Cursor getList(long listID) {
		return rawQuery(R.string.query_list, listID);
	}
	public long createList(String name) {
		return rawInsert(R.string.query_list_create, name);
	}
	public void updateList(long id, String name) {
		execSQL(R.string.query_list_update, name, id);
	}
	public void deleteList(long id) {
		execSQL(R.string.query_list_delete, id);
	}
	public Long findList(String name) {
		return getID(R.string.query_list_find, name);
	}
	public void addListEntry(long listID, long itemID) {
		execSQL(R.string.query_list_entry_add, listID, itemID);
	}
	public void deleteListEntry(long listID, long itemID) {
		execSQL(R.string.query_list_entry_remove, listID, itemID);
	}

	public void deleteRecentsOfItem(long itemID) {
		execSQL(R.string.query_recent_delete, itemID);
	}
	public Cursor listRecents() {
		return rawQuery(R.string.query_recents, 1, 0.5);
	}

	public Cursor searchSuggest(String query) {
		query = fixQuery(query);
		return rawQuery(R.string.query_search_suggest, query);
	}

	public Cursor search(String query) {
		query = fixQuery(query);
		return rawQuery(R.string.query_search, query);
	}

	public long getSearchSize() {
		return DatabaseTools.singleLong(rawQuery(R.string.query_search_size), null);
	}

	private static String fixQuery(String query) {
		if (query.contains("*")) {
			return query;
		}
		return query.trim().replaceAll("\\s+", "*") + "*";
	}

	public void updateCategoryCache(Context context) {
		LOG.info("Updating category name cache");
		@SuppressWarnings("resource")
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			Cursor cursor = rawQuery(db, R.string.query_category_cache_names);
			try {
				while (cursor.moveToNext()) {
					String resourceName = cursor.getString(0);
					String displayName = String.valueOf(AndroidTools.getText(context, resourceName));
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

	public void clearImages() {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.beginTransaction();
			db.execSQL("UPDATE Property SET image = NULL");
			db.execSQL("UPDATE Room SET image = NULL");
			db.execSQL("UPDATE Item SET image = NULL");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		//db.execSQL("VACUUM"); // must be outside a transaction
	}

	public void rebuildSearch() {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.beginTransaction();
			db.execSQL("DELETE from Search");
			db.execSQL("insert into Search_Refresher(_id) select _id from Item");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		//db.execSQL("VACUUM"); // must be outside a transaction
	}
}
