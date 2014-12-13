package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.zip.*;

import static java.lang.String.*;

import org.apache.commons.csv.*;
import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.StringRes;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.io.ImporterTask.ImportCallbacks.Progress;
import net.twisterrob.inventory.android.content.io.ImporterTask.ImportCallbacks.Progress.Phase;

import static net.twisterrob.java.utils.CollectionTools.*;

public class ImporterTask extends SimpleAsyncTask<File, Progress, Progress> {
	private static final Logger LOG = LoggerFactory.getLogger(ImporterTask.class);

	private ImportCallbacks callbacks = DUMMY_CALLBACK;
	private Progress progress;

	public interface ImportCallbacks {
		void importStarting();
		void importProgress(Progress progress);
		void importFinished(Progress progress);

		public static final class Progress implements Cloneable {
			public File input;
			public Phase phase;
			public int done;
			public int total;
			public Throwable failure;
			public final List<CharSequence> conflicts = new ArrayList<>();

			@Override public Progress clone() {
				try {
					return (Progress)super.clone();
				} catch (CloneNotSupportedException ex) {
					throw new InternalError(ex.toString());
				}
			}
			@Override public String toString() {
				return format(Locale.ROOT, "%2$s: data=%3$d/%4$d: %5$s for %1$s", input, phase, done, total, failure);
			}

			public enum Phase {
				Init,
				Data
			}
		}
	}

	private final Context context;
	private final String invalidZipFormat;

	public ImporterTask(Context context) {
		this.context = context;
		this.invalidZipFormat = "The file %s is not a valid " + context.getString(R.string.app_name) + " backup: %s";
	}

	public void setCallbacks(ImportCallbacks callbacks) {
		this.callbacks = callbacks != null? callbacks : DUMMY_CALLBACK;
	}

	@Override protected void onPreExecute() {
		callbacks.importStarting();
	}
	@Override protected void onProgressUpdate(Progress progress) {
		callbacks.importProgress(progress);
	}
	@Override protected void onPostExecute(Progress progress) {
		if (progress.failure != null) {
			LOG.warn("Export failed", progress.failure);
		}
		callbacks.importFinished(progress);
	}

	private void publishStart() {
		progress.done = 0;
		publishProgress();
	}
	private void publishIncrement() {
		progress.done++;
		publishProgress();
	}
	private void publishProgress() {
		if (isCancelled()) {
			throw new CancellationException();
		}
		publishProgress(progress.clone());
	}

	private void warning(@StringRes int stringID, Object... args) {
		String message = context.getString(stringID, args);
		LOG.warn("Warning: {}", message);
		progress.conflicts.add(message);
	}

	private void error(String message) {
		LOG.warn("Error: {}", message);
		progress.conflicts.add(message);
	}

	@Override protected Progress doInBackground(File file) {
		progress = new Progress();
		progress.input = file;
		ZipFile zip = null;
		try {
			progress.phase = Phase.Init;
			// TODO wakelock?
			App.db().getWritableDatabase().beginTransaction();

			zip = new ZipFile(file);
			ZipEntry csv = zip.getEntry("data.csv");
			if (csv == null) {
				throw new IllegalArgumentException(format(invalidZipFormat, file, "missing data.csv"));
			}
			Collection<CSVRecord> records = getRecords(zip.getInputStream(csv));
			putTypes(App.db().listPropertyTypes(), PropertyType.ID, PropertyType.NAME);
			putTypes(App.db().listRoomTypes(), RoomType.ID, RoomType.NAME);
			putTypes(App.db().listItemCategories(), Category.ID, Category.NAME);

			progress.phase = Phase.Data;
			importAll(records, zip);
			App.db().getWritableDatabase().setTransactionSuccessful();
		} catch (ZipException ex) {
			progress.failure = new IllegalArgumentException(format(invalidZipFormat, file, "not a zip file"), ex);
		} catch (Throwable ex) {
			progress.failure = ex;
		} finally {
			IOTools.ignorantClose(zip);
			App.db().getWritableDatabase().endTransaction();
		}
		return progress;
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

	private void importAll(Collection<CSVRecord> records, ZipFile zip) throws IOException {
		progress.total = records.size();
		publishStart();
		for (CSVRecord record : records) {
			try {
				String type = record.get("type");
				String property = record.get("property");
				String room = record.get("room");
				String item = record.get("item");
				String image = record.get("image");
				String parent = record.get("parent");
				String id = record.get("id");

				process(type, property, room, item, image, parent, id);
				if (image != null) {
					ZipEntry imageEntry = zip.getEntry(image);
					if (imageEntry != null) {
						InputStream imageStream = zip.getInputStream(imageEntry);
						File imageFile = Constants.Paths.getImageFile(context, image);
						IOTools.copyStream(imageStream, new FileOutputStream(imageFile));
					} else {
						warning(R.string.backup_import_invalid_image, coalesce(item, room, property), image);
					}
				}
				publishIncrement();
			} catch (Exception ex) {
				LOG.warn("Cannot process {}", record, ex);
				error(ex.getMessage());
				publishProgress();
			}
		}
	}

	protected void process(String type, String property, String room, String item, String image, String parent,
			String id) {
		if (item != null) { // item: property ?= null && room ?= null && item != null
			processItem(type, property, room, item, parent, id, image);
		} else if (room != null) { // room: property ?= null && room != null && item == null
			processRoom(type, property, room, image);
		} else if (property != null) { // property: property != null && room == null && item == null
			processProperty(type, property, image);
		} else { // invalid: property: property == null && room == null && item == null
			String message = context.getString(R.string.backup_import_invalid_belonging, type, image, parent, id);
			throw new IllegalArgumentException(message);
		}
	}

	private static final String SEPARATOR = "\u001F"; // Unit Separator ASCII character

	private final Map<String, Long> types = new HashMap<>();
	private final Map<String, Long> properties = new HashMap<>();
	private final Map<String, Long> roots = new HashMap<>();
	private final Map<Long, Long> items = new HashMap<>();

	protected long processProperty(String type, String property, String image) {
		LOG.trace("Processing property: {}", property);
		Long propertyID = App.db().findProperty(property);
		if (propertyID == null) {
			Long typeID = types.get(type);
			if (typeID == null) {
				warning(R.string.backup_import_invalid_type, type, property);
				typeID = PropertyType.DEFAULT;
			}
			propertyID = App.db().createProperty(property, typeID, image);
		} else {
			warning(R.string.backup_import_conflict_property, property);
		}
		properties.put(property, propertyID);
		return propertyID;
	}

	protected long processRoom(String type, String property, String room, String image) {
		LOG.trace("Processing room: {} in {}", room, property);
		if (property == null) {
			throw new IllegalArgumentException("Cannot process room which is not in a property");
		}
		long propertyID = properties.get(property);
		Long roomID = App.db().findRoom(propertyID, room);
		if (roomID == null) {
			Long typeID = types.get(type);
			if (typeID == null) {
				warning(R.string.backup_import_invalid_type, type, room);
				typeID = RoomType.DEFAULT;
			}
			roomID = App.db().createRoom(propertyID, room, typeID, image);
		} else {
			warning(R.string.backup_import_conflict_room, property, room);
		}
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
		Long itemID = App.db().findItem(parentID, item);
		if (itemID == null) {
			Long typeID = types.get(type);
			if (typeID == null) {
				warning(R.string.backup_import_invalid_type, type, item);
				typeID = Category.DEFAULT;
			}
			itemID = App.db().createItem(parentID, item, typeID, image);
		} else {
			warning(R.string.backup_import_conflict_item, property, room, item);
		}
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

	private void putTypes(Cursor cursor, String idColumn, String nameColumn) {
		try {
			while (cursor.moveToNext()) {
				long id = cursor.getLong(cursor.getColumnIndexOrThrow(idColumn));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn));
				Long existing = types.put(name, id);
				if (existing != null) {
					throw new IllegalStateException(
							"Duplicate type: " + name + " existing: " + existing + " new: " + id);
				}
			}
		} finally {
			cursor.close();
		}
	}

	/** To prevent NullPointerException and null-checks in code */
	private static final ImportCallbacks DUMMY_CALLBACK = new ImportCallbacks() {
		@Override public void importStarting() {
		}
		@Override public void importProgress(Progress progress) {
		}
		@Override public void importFinished(Progress progress) {
		}
	};
}
