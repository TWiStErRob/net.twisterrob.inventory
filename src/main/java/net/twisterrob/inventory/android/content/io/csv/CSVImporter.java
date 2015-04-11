package net.twisterrob.inventory.android.content.io.csv;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.*;
import org.slf4j.*;

import android.database.Cursor;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.io.*;
import net.twisterrob.inventory.android.content.model.Types;

import static net.twisterrob.java.utils.CollectionTools.*;

public class CSVImporter implements Importer {
	private static final Logger LOG = LoggerFactory.getLogger(CSVImporter.class);
	private final ImportProgressHandler progress;

	public CSVImporter(ImportProgressHandler progress) {
		this.progress = progress;
	}

	public void doImport(InputStream stream) throws IOException {
		ImportProcessor processor = new ImportProcessor();

		Collection<CSVRecord> records = getRecords(stream);
		progress.publishStart(records.size());
		for (CSVRecord record : records) {
			try {
				processor.belongingTypeName = record.get("type");
				processor.propertyName = record.get("property");
				processor.roomName = record.get("room");
				processor.itemName = record.get("item");
				processor.description = record.get("description");
				processor.imageFileName = record.get("image");
				processor.parentName = record.get("parent");
				processor.id = Long.parseLong(record.get("id"));

				long dbID = processor.process();
				if (processor.imageFileName != null) {
					progress.importImage(null, dbID,
							coalesce(processor.itemName, processor.roomName, processor.propertyName),
							processor.imageFileName);
				}
				progress.publishIncrement();
			} catch (Exception ex) {
				LOG.warn("Cannot process {}", record, ex);
				progress.error(ex.getMessage());
			}
		}
	}

	private Collection<CSVRecord> getRecords(InputStream in) throws IOException {
		try {
			@SuppressWarnings("resource")
			CSVParser parser = CSVConstants.FORMAT.parse(new InputStreamReader(in, CSVConstants.ENCODING));
			return parser.getRecords();
		} finally {
			IOTools.ignorantClose(in);
		}
	}

	private class ImportProcessor {
		private static final String SEPARATOR = "\u001F"; // Unit Separator ASCII character

		private final Map<String, Long> properties = new HashMap<>();
		private final Map<String, Long> roots = new HashMap<>();
		private final Map<Long, Long> items = new HashMap<>();
		private final Types types = new Types();

		String belongingTypeName;
		String propertyName;
		String roomName;
		String itemName;
		String description;
		String imageFileName;
		String parentName;
		long id;

		protected long process() throws IOException {
			long dbID = processInternal();

			if (imageFileName != null) {
				progress.importImage(null, dbID, coalesce(itemName, roomName, propertyName), imageFileName);
			}

			return dbID;
		}
		protected long processInternal() {
			if (itemName != null) { // item: property ?= null && room ?= null && item != null
				if (propertyName == null) {
					throw new IllegalArgumentException("Cannot process item which is not in a property");
				}
				if (roomName == null) {
					throw new IllegalArgumentException("Cannot process item which is not in a room");
				}
				return processItem();
			} else if (roomName != null) { // room: property ?= null && room != null && item == null
				if (propertyName == null) {
					throw new IllegalArgumentException("Cannot process room which is not in a property");
				}
				return processRoom();
			} else if (propertyName != null) { // property: property != null && room == null && item == null
				return processProperty();
			} else { // invalid: property: property == null && room == null && item == null
				progress.warning(R.string.backup_import_invalid_belonging, belongingTypeName, imageFileName,
						parentName, id);
				return 0; // TODO
			}
		}

		protected long processProperty() {
			LOG.trace("Processing property: {}", propertyName);
			Long propertyID = getOrCreateProperty();
			properties.put(propertyName, propertyID);
			return propertyID;
		}
		private Long getOrCreateProperty() {
			Long propertyID = App.db().findProperty(propertyName);
			if (propertyID == null) {
				Long typeID = types.getID(belongingTypeName);
				if (typeID == null) {
					progress.warning(R.string.backup_import_invalid_type, belongingTypeName, propertyName);
					typeID = PropertyType.DEFAULT;
				}
				propertyID = App.db().createProperty(typeID, propertyName, description, imageFileName);
			} else {
				progress.warning(R.string.backup_import_conflict_property, propertyName);
			}
			return propertyID;
		}

		protected long processRoom() {
			LOG.trace("Processing room: {} in {}", roomName, propertyName);
			long propertyID = properties.get(propertyName);
			Long roomID = getOrCreateRoom(propertyID);
			roots.put(createRoomKey(propertyName, roomName), getRoot(roomID));
			return roomID;
		}
		private Long getOrCreateRoom(long propertyID) {
			Long roomID = App.db().findRoom(propertyID, roomName);
			if (roomID == null) {
				Long typeID = types.getID(belongingTypeName);
				if (typeID == null) {
					progress.warning(R.string.backup_import_invalid_type, belongingTypeName, roomName);
					typeID = RoomType.DEFAULT;
				}
				roomID = App.db().createRoom(propertyID, typeID, roomName, description, imageFileName);
			} else {
				progress.warning(R.string.backup_import_conflict_room, propertyName, roomName);
			}
			return roomID;
		}

		protected long processItem() {
			LOG.trace("Processing item: {} in {}/{}", itemName, propertyName, roomName);
			long csvID = id;
			long parentID = getParentID(propertyName, roomName, parentName);
			Long itemID = getOrCreateItem(parentID);
			items.put(csvID, itemID);
			return itemID;
		}
		private Long getOrCreateItem(long parentID) {
			Long itemID = App.db().findItem(parentID, itemName);
			if (itemID == null) {
				Long typeID = types.getID(belongingTypeName);
				if (typeID == null) {
					progress.warning(R.string.backup_import_invalid_type, belongingTypeName, itemName);
					typeID = Category.DEFAULT;
				}
				itemID = App.db().createItem(parentID, typeID, itemName, description, imageFileName);
			} else {
				progress.warning(R.string.backup_import_conflict_item, propertyName, roomName, itemName);
			}
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

		private long getRoot(long roomID) {
			Cursor room = App.db().getRoom(roomID);
			try {
				room.moveToFirst();
				return room.getLong(room.getColumnIndexOrThrow(Room.ROOT_ITEM));
			} finally {
				room.close();
			}
		}

		private String createRoomKey(String property, String room) {
			return property + SEPARATOR + room;
		}
	}
}
