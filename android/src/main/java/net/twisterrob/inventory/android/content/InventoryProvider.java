package net.twisterrob.inventory.android.content;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import org.slf4j.*;

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import static android.app.SearchManager.*;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.concurrent.*;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.StringTools;

import static net.twisterrob.inventory.android.content.InventoryContract.*;
import static net.twisterrob.inventory.android.content.contract.ImageDataColumns.*;

// CONSIDER http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/
// CONSIDER http://www.vogella.com/tutorials/AndroidSQLite/article.html
// CONSIDER https://code.google.com/p/iosched/source/browse/#git%2Fandroid%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgoogle%2Fandroid%2Fapps%2Fiosched%2Fprovider
// CONSIDER https://raw.githubusercontent.com/android/platform_packages_providers_contactsprovider/master/src/com/android/providers/contacts/ContactsProvider2.java
public class InventoryProvider extends VariantContentProvider {
	private static final Logger LOG = LoggerFactory.getLogger(InventoryProvider.class);

	private static final int FIRST_DIR = 100000;
	private static final int FIRST_ITEM = 10000;
	private static final int PROPERTIES = FIRST_DIR + 10;
	private static final int PROPERTY = FIRST_ITEM + 10;
	private static final int PROPERTY_IMAGE = FIRST_ITEM + 11;
	private static final int ROOMS = FIRST_DIR + 20;
	private static final int ROOM = FIRST_ITEM + 20;
	private static final int ROOM_IMAGE = FIRST_ITEM + 21;
	private static final int ITEMS = FIRST_DIR + 30;
	private static final int ITEM = FIRST_ITEM + 30;
	private static final int ITEM_IMAGE = FIRST_ITEM + 31;
	private static final int CATEGORIES = FIRST_DIR + 40;
	private static final int CATEGORY = FIRST_ITEM + 40;
	private static final int CATEGORY_IMAGE = FIRST_ITEM + 41;
	private static final int SEARCH_ITEMS = FIRST_DIR + 50;
	private static final int SEARCH_ITEMS_SUGGEST = FIRST_DIR + 51;
	private static final int FULL_BACKUP = FIRST_DIR + 61;

	protected static final String URI_PATH_ID = "/#";
	protected static final String URI_PATH_IMAGE = "/" + Helpers.IMAGE_URI_SEGMENT;

	//	protected static final String URI_PATH_ANY = "/*";
	private static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, Property.DIR_URI_SEGMENT, PROPERTIES);
		URI_MATCHER.addURI(AUTHORITY, Property.ITEM_URI_SEGMENT + URI_PATH_ID, PROPERTY);
		URI_MATCHER.addURI(AUTHORITY, Property.ITEM_URI_SEGMENT + URI_PATH_ID + URI_PATH_IMAGE, PROPERTY_IMAGE);
		URI_MATCHER.addURI(AUTHORITY, Room.DIR_URI_SEGMENT, ROOMS);
		URI_MATCHER.addURI(AUTHORITY, Room.ITEM_URI_SEGMENT + URI_PATH_ID, ROOM);
		URI_MATCHER.addURI(AUTHORITY, Room.ITEM_URI_SEGMENT + URI_PATH_ID + URI_PATH_IMAGE, ROOM_IMAGE);
		URI_MATCHER.addURI(AUTHORITY, Item.DIR_URI_SEGMENT, ITEMS);
		URI_MATCHER.addURI(AUTHORITY, Item.ITEM_URI_SEGMENT + URI_PATH_ID, ITEM);
		URI_MATCHER.addURI(AUTHORITY, Item.ITEM_URI_SEGMENT + URI_PATH_ID + URI_PATH_IMAGE, ITEM_IMAGE);
		URI_MATCHER.addURI(AUTHORITY, Category.DIR_URI_SEGMENT, CATEGORIES);
		URI_MATCHER.addURI(AUTHORITY, Category.ITEM_URI_SEGMENT + URI_PATH_ID, CATEGORY);
		URI_MATCHER.addURI(AUTHORITY, Category.ITEM_URI_SEGMENT + URI_PATH_ID + URI_PATH_IMAGE, CATEGORY_IMAGE);
		URI_MATCHER.addURI(AUTHORITY, Search.URI_PATH, SEARCH_ITEMS);
		URI_MATCHER.addURI(AUTHORITY, Search.URI_PATH_SUGGEST, SEARCH_ITEMS_SUGGEST);
		URI_MATCHER.addURI(AUTHORITY, Export.BACKUP_URI_SEGMENT, FULL_BACKUP);
	}

	public @NonNull Context getSafeContext() {
		Context context = getContext();
		if (context == null) {
			throw new IllegalStateException("Content provider not created yet.");
		}
		return context;
	}

	@Override public String getType(@NonNull Uri uri) {
		String type = getTypeInternal(uri);
		LOG.trace("getType/{}({}): {}", resolveMatch(URI_MATCHER.match(uri)), uri, type);
		return type;
	}
	private String getTypeInternal(@NonNull Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case ITEM_IMAGE:
			case ROOM_IMAGE:
			case PROPERTY_IMAGE:
				return "image/jpeg";
			case CATEGORY_IMAGE:
				return "image/svg+xml";
			case PROPERTIES:
				return Property.DIR_TYPE;
			case PROPERTY:
				return Property.ITEM_TYPE;
			case ROOMS:
				return Room.DIR_TYPE;
			case ROOM:
				return Room.ITEM_TYPE;
			case ITEMS:
				return Item.DIR_TYPE;
			case ITEM:
				return Item.ITEM_TYPE;
			case CATEGORIES:
				return Category.DIR_TYPE;
			case CATEGORY:
				return Category.ITEM_TYPE;
			case SEARCH_ITEMS:
				return Search.TYPE;
			case SEARCH_ITEMS_SUGGEST:
				return Search.TYPE_SUGGEST;
			case FULL_BACKUP:
				return Export.TYPE_BACKUP;
			default:
				return null;
		}
	}

	@Override public Cursor query(@NonNull Uri uri,
			String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		LOG.trace("query/{}({}, {}, {}, {}, {})",
				resolveMatch(URI_MATCHER.match(uri)), uri, projection, selection, selectionArgs, sortOrder);
		long start = System.nanoTime();
		try {
			return queryInternal(uri, projection, selection, selectionArgs, sortOrder);
		} catch (RuntimeException ex) {
			LOG.error("query/{}({}, {}, {}, {}, {}) thrown {}",
					resolveMatch(URI_MATCHER.match(uri)),
					uri, projection, selection, selectionArgs, sortOrder, ex.getClass(), ex);
			throw ex;
		} finally {
			long end = System.nanoTime();
			LOG.debug("query/{}({}, {}, {}, {}, {}): {}ms",
					resolveMatch(URI_MATCHER.match(uri)),
					uri, projection, selection, selectionArgs, sortOrder, (end - start) / 1e6);
		}
	}
	private Cursor queryInternal(@NonNull Uri uri,
			@SuppressWarnings("unused") String[] projection,
			@SuppressWarnings("unused") String selection, String[] selectionArgs,
			@SuppressWarnings("unused") String sortOrder) {
		switch (URI_MATCHER.match(uri)) {
			case SEARCH_ITEMS_SUGGEST: {
				// uri.getLastPathSegment().toLowerCase(Locale.getDefault());
				String query = selectionArgs[0].toLowerCase(Locale.getDefault());
				if (StringTools.isNullOrEmpty(query)) {
					return createItemSearchHelp();
				}
				return App.db().searchSuggest(query);
			}
			case SEARCH_ITEMS: {
				if (BuildConfig.DEBUG && (selectionArgs == null || StringTools.isNullOrEmpty(selectionArgs[0]))) {
					return App.db().listItemsForCategory(
							net.twisterrob.inventory.android.content.contract.Category.INTERNAL, true);
				}
				String query = selectionArgs[0].toLowerCase(Locale.getDefault());
				return App.db().search(query);
			}

			case PROPERTY_IMAGE: {
				return App.db().getPropertyImage(Property.getID(uri));
			}
			case ROOM_IMAGE: {
				return App.db().getRoomImage(Room.getID(uri));
			}
			case ITEM_IMAGE: {
				return App.db().getItemImage(Item.getID(uri));
			}
			case FULL_BACKUP: {
				Calendar now = Export.getNow(uri);
				MatrixCursor details = new MatrixCursor(new String[] {COLUMN_SIZE, COLUMN_DISPLAY_NAME});
				long estimatedSize = App.db().getFile().length();
				details.addRow(new Object[] {estimatedSize, Paths.getExportFileName(now)});
				return details;
			}
			default:
				throw new UnsupportedOperationException("Unknown URI: " + uri);
		}
	}

	/**
	 * Return a singular suggestion to display more text about what can be searched
	 * Tapping it would open the search activity.
	 */
	private Cursor createItemSearchHelp() {
		MatrixCursor cursor = new MatrixCursor(new String[] {COLUMN_ID
				, SUGGEST_COLUMN_INTENT_ACTION
				, SUGGEST_COLUMN_INTENT_DATA
				, SUGGEST_COLUMN_ICON_1
				//, SUGGEST_COLUMN_ICON_2
				, SUGGEST_COLUMN_TEXT_1
				, SUGGEST_COLUMN_TEXT_2
		}, 1);
		cursor.addRow(new String[] {null
				, Intent.ACTION_SEARCH // Opens search activity
				, ""
				, "android.resource://android/drawable/ic_menu_search"
				//, "android.resource://net.twisterrob.inventory.debug/drawable/category_unknown"
				, "Search Inventory Items" // Opens search activity
				, "Search for item name above." // Opens search activity
		});
		return cursor;
	}

	@Override public Uri insert(@NonNull Uri uri, ContentValues values) {
		LOG.trace("insert/{}({}, {})",
				resolveMatch(URI_MATCHER.match(uri)), uri, values);
		throw new UnsupportedOperationException("Unknown URI: " + uri);
	}

	@Override public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		LOG.trace("update/{}({}, {}, {}, {})",
				resolveMatch(URI_MATCHER.match(uri)), uri, values, selection, selectionArgs);
		throw new UnsupportedOperationException("Unknown URI: " + uri);
	}

	@Override public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		LOG.trace("delete/{}({}, {}, {})",
				resolveMatch(URI_MATCHER.match(uri)), uri, selection, selectionArgs);
		throw new UnsupportedOperationException("Unknown URI: " + uri);
	}

	@Override public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
			throws FileNotFoundException {
		LOG.trace("openFile/{}({}, {})",
				resolveMatch(URI_MATCHER.match(uri)), uri, mode);
		switch (URI_MATCHER.match(uri)) {
			case PROPERTY_IMAGE:
			case ROOM_IMAGE:
			case ITEM_IMAGE:
				return openBlobHelper(uri, mode);
			case CATEGORY_IMAGE:
				Cursor category = App.db().getCategory(Category.getID(uri));
				String name = DatabaseTools.singleString(category,
						net.twisterrob.inventory.android.content.contract.Category.TYPE_IMAGE);
				int svgID = ResourceTools.getRawResourceID(getSafeContext(), name);
				// The following only works if the resource is uncompressed: android.aaptOptions.noCompress 'svg'
				//noinspection resource AssetFileDescriptor is closed by caller
				return getSafeContext().getResources().openRawResourceFd(svgID).getParcelFileDescriptor();
			case FULL_BACKUP:
				return generateBackupWithService();
		}
		return super.openFile(uri, mode);
	}

	/** @see ContentProvider#openFileHelper(android.net.Uri, java.lang.String) */
	protected final ParcelFileDescriptor openBlobHelper(@NonNull Uri uri,
			@SuppressWarnings("unused") @NonNull String mode) throws FileNotFoundException {
		//noinspection resource
		Cursor c = query(uri, new String[] {COLUMN_BLOB}, null, null, null);
		if (c == null) {
			throw new NullPointerException("No cursor returned for " + uri);
		}

		try {
			if (1 < c.getCount()) {
				throw new FileNotFoundException("Multiple items at " + uri);
			}
			if (!c.moveToFirst()) {
				throw new FileNotFoundException("No entry for " + uri);
			}
			int i = c.getColumnIndex(COLUMN_BLOB);
			if (i < 0) {
				throw new FileNotFoundException("Column " + COLUMN_BLOB + " not found for " + uri);
			}
			byte[] contents = c.getBlob(i);
			if (contents == null) {
				throw new FileNotFoundException("No blob for " + uri);
			}
			return AndroidTools.stream(contents);
		} finally {
			c.close();
		}
	}

	private ParcelFileDescriptor generateBackupWithService() throws FileNotFoundException {
		try {
			final ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
			final Context context = getSafeContext();
			new BackupServiceConnection() {
				@Override protected void serviceBound(ComponentName name, BackupService.LocalBinder service) {
					service.export(pipe[1]);
					unbind();
				}
			}.bind(context);
			return pipe[0];
		} catch (IOException ex) {
			throw IOTools.FileNotFoundException("Cannot create piped stream", ex);
		}
	}

	@DebugHelper
	private static String resolveMatch(int result) {
		if (result == UriMatcher.NO_MATCH) {
			return "NO_MATCH";
		}
		for (Field f : InventoryProvider.class.getDeclaredFields()) {
			try {
				if (int.class.equals(f.getType()) && f.getInt(null) == result) {
					return f.getName();
				}
			} catch (Exception ex) {
				LOG.warn("Can't resolve field for {} from {}", result, f, ex);
			}
		}
		return "NOT_FOUND::" + result;
	}
}
