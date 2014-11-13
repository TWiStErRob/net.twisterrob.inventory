package net.twisterrob.inventory.android.content.io;

import java.util.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

import static net.twisterrob.inventory.android.content.DatabaseHelper.*;

@SuppressLint("UseSparseArrays")
public class DatabaseImporter {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseImporter.class);
	private static final String SEPARATOR = "\u001F"; // Unit Separator ASCII character

	private final Map<String, Long> properties = new HashMap<>();
	private final Map<String, Long> roots = new HashMap<>();
	private final Map<Long, Long> items = new HashMap<>();
	private final Types types = new Types();

	protected void startProcess() {
		App.db().getWritableDatabase().beginTransaction();
	}
	protected void endProcess(Exception ex) {
		if (ex == null) {
			App.db().getWritableDatabase().setTransactionSuccessful();
		}
		App.db().getWritableDatabase().endTransaction();
	}

	protected long processProperty(String type, String property, String image) {
		LOG.trace("Processing property: {}", property);
		long propertyID = getOrCreateProperty(property, types.getType(type, PropertyType.DEFAULT), image);
		properties.put(property, propertyID);
		return propertyID;
	}

	protected long processRoom(String type, String property, String room, String image) {
		LOG.trace("Processing room: {} in {}", room, property);
		if (property == null) {
			throw new IllegalArgumentException("Cannot process room which is not in a property");
		}
		long propertyID = properties.get(property);
		long roomID = getOrCreateRoom(propertyID, room, types.getType(type, RoomType.DEFAULT), image);
		roots.put(createRoomKey(property, room), getRoot(roomID));
		return roomID;
	}

	protected long processItem(String type, String property, String room, String item, String parent, String id,
			String image) {
		LOG.trace("Processing item: {} in {}/{}", item, property, room);
		if (property == null) {
			throw new IllegalArgumentException("Cannot process item which is not in a property");
		}
		if (room == null) {
			throw new IllegalArgumentException("Cannot process item which is not in a room");
		}
		long csvID = Long.parseLong(id);
		long parentID = getParentID(property, room, parent);
		long itemID = getOrCreateItem(parentID, item, types.getType(type, Category.DEFAULT), image);
		items.put(csvID, itemID);
		return itemID;
	}

	private long getParentID(String property, String room, String parent) {
		if (parent == null || parent.isEmpty()) {
			return roots.get(createRoomKey(property, room));
		} else {
			long parentID = Long.parseLong(parent);
			return items.get(parentID);
		}
	}

	private static long getRoot(long roomID) {
		Cursor room = App.db().getRoom(roomID);
		try {
			room.moveToFirst();
			return room.getLong(room.getColumnIndexOrThrow(Room.ROOT_ITEM));
		} finally {
			room.close();
		}
	}

	private static String createRoomKey(String property, String room) {
		return property + SEPARATOR + room;
	}

	private static class Types {
		private final Map<String, Long> types = new HashMap<>();

		public Types() {
			setupTypes();
		}

		public long getType(String type, long fallback) {
			Long typeID = types.get(type);
			return typeID != null? typeID : fallback;
		}

		private void setupTypes() {
			putTypes(App.db().listPropertyTypes(), PropertyType.ID, PropertyType.NAME);
			putTypes(App.db().listRoomTypes(), RoomType.ID, RoomType.NAME);
			putTypes(App.db().listItemCategories(), Category.ID, Category.NAME);
		}

		private void putTypes(Cursor cursor, String idColumn, String nameColumn) {
			try {
				while (cursor.moveToNext()) {
					putType(cursor, idColumn, nameColumn);
				}
			} finally {
				cursor.close();
			}
		}

		private void putType(Cursor cursor, String idColumn, String nameColumn) {
			long id = cursor.getLong(cursor.getColumnIndexOrThrow(idColumn));
			String name = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn));
			types.put(name, id);
		}
	}
}
