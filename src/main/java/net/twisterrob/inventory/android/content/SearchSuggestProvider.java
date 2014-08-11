package net.twisterrob.inventory.android.content;

import java.util.Locale;

import android.app.SearchManager;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.provider.BaseColumns;
import static android.app.SearchManager.*;

import net.twisterrob.inventory.android.App;

public class SearchSuggestProvider extends ContentProvider {
	//private static final Logger LOG = LoggerFactory.getLogger(SearchSuggestProvider.class);

	public static final String AUTHORITY = "net.twisterrob.inventory.suggest";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search");

	// UriMatcher constant for search suggestions
	private static final int SEARCH_SUGGEST = 1;

	private UriMatcher m_uriMatcher;

	@Override
	public String getType(Uri uri) {
		switch (m_uriMatcher.match(uri)) {
			case SEARCH_SUGGEST:
				return SearchManager.SUGGEST_MIME_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		m_uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		m_uriMatcher.addURI(AUTHORITY, SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		m_uriMatcher.addURI(AUTHORITY, SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		//LOG.debug("query: {}", uri);

		// Use the UriMatcher to see what kind of query we have
		switch (m_uriMatcher.match(uri)) {
			case SEARCH_SUGGEST: {
				String query = uri.getLastPathSegment();
				if (SUGGEST_URI_PATH_QUERY.equals(query)) {
					MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, SUGGEST_COLUMN_TEXT_1,
							SUGGEST_COLUMN_TEXT_2}, 1);
					cursor.addRow(new String[]{null, "Search Inventory Items", "Search for item name."});
					return cursor;
				}
				query = query.toLowerCase(Locale.ROOT);
				//LOG.debug("Search suggestions requested: {}", query);
				return App.db().search(query);
			}
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
}