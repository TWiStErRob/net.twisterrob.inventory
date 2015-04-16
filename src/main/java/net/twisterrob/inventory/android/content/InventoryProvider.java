package net.twisterrob.inventory.android.content;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.Locale;

import org.slf4j.*;

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;

import static android.app.SearchManager.*;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.java.utils.StringTools;

import static net.twisterrob.inventory.android.content.InventoryContract.*;

// TODO http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/
// TODO http://www.vogella.com/tutorials/AndroidSQLite/article.html
// TODO https://code.google.com/p/iosched/source/browse/#git%2Fandroid%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgoogle%2Fandroid%2Fapps%2Fiosched%2Fprovider
// TODO https://raw.githubusercontent.com/android/platform_packages_providers_contactsprovider/master/src/com/android/providers/contacts/ContactsProvider2.java
public class InventoryProvider extends ContentProvider {
	private static final Logger LOG = LoggerFactory.getLogger(InventoryProvider.class);

	private static final int FIRST_DIR = 100000;
	private static final int FIRST_ITEM = 10000;
	private static final int PROPRETIES = FIRST_DIR + 1;
	private static final int PROPERTY = FIRST_ITEM + 1;
	private static final int ROOMS = FIRST_DIR + 2;
	private static final int ROOM = FIRST_ITEM + 2;
	private static final int ITEMS = FIRST_DIR + 3;
	private static final int ITEM = FIRST_ITEM + 3;
	private static final int CATEGORIES = FIRST_DIR + 4;
	private static final int CATEGORY = FIRST_ITEM + 4;
	private static final int SEARCH_ITEMS = FIRST_DIR + 5;
	private static final int SEARCH_ITEMS_SUGGEST = FIRST_DIR + 6;

	protected static final String URI_PATH_ID = "/#";
	protected static final String URI_PATH_ANY = "/*";

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "properties", PROPRETIES);
		URI_MATCHER.addURI(AUTHORITY, "property" + URI_PATH_ID, PROPERTY);
		URI_MATCHER.addURI(AUTHORITY, "rooms", ROOMS);
		URI_MATCHER.addURI(AUTHORITY, "room" + URI_PATH_ID, ROOM);
		URI_MATCHER.addURI(AUTHORITY, Item.DIR_URI_PATH, ITEMS);
		URI_MATCHER.addURI(AUTHORITY, Item.ITEM_URI_PATH + URI_PATH_ID, ITEM);
		URI_MATCHER.addURI(AUTHORITY, "categories", CATEGORIES);
		URI_MATCHER.addURI(AUTHORITY, "category" + URI_PATH_ID, CATEGORY);
		URI_MATCHER.addURI(AUTHORITY, Search.URI_PATH, SEARCH_ITEMS);
		URI_MATCHER.addURI(AUTHORITY, Search.URI_PATH_SUGGEST, SEARCH_ITEMS_SUGGEST);
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case ITEMS:
				return InventoryContract.Item.DIR_TYPE;
			case ITEM:
				return InventoryContract.Item.ITEM_TYPE;
			case SEARCH_ITEMS:
				return InventoryContract.Search.TYPE;
			case SEARCH_ITEMS_SUGGEST:
				return InventoryContract.Search.TYPE_SUGGEST;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		LOG.debug("query({}, {}, {}, {}, {}): {}",
				uri, projection, selection, selectionArgs, sortOrder, resolveMatch(URI_MATCHER.match(uri)));
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
				if (selectionArgs == null || StringTools.isNullOrEmpty(selectionArgs[0])) {
					return App.db().listItemsForCategory(Category.INTERNAL, true);
				}
				String query = selectionArgs[0].toLowerCase(Locale.getDefault());
				return App.db().search(query);
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
		MatrixCursor cursor = new MatrixCursor(new String[] {BaseColumns._ID
				, SUGGEST_COLUMN_INTENT_ACTION
				, SUGGEST_COLUMN_INTENT_DATA
				, SUGGEST_COLUMN_ICON_1
				, SUGGEST_COLUMN_TEXT_1
				, SUGGEST_COLUMN_TEXT_2
		}, 1);
		cursor.addRow(new String[] {null
				, Intent.ACTION_SEARCH // Opens search activity
				, ""
				, "android.resource://android/drawable/ic_menu_search"
				, "Search Inventory Items" // Opens search activity
				, "Search for item name above." // Opens search activity
		});
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException("Unknown URI: " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("Unknown URI: " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("Unknown URI: " + uri);
	}

	@Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		return super.openFile(uri, mode);
	}
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
		return "NOT_FOUND";
	}
}
