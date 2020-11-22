package net.twisterrob.android.content;

import java.util.*;

import android.annotation.TargetApi;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.*;

/**
 * Example provider class for {@code CREATE TABLE MyItem (_id, name, description)}:
 * <pre>
 * package my.name.space.content.MySimpleProvider;
 * public class MySimpleProvider extends SimpleTableContentProvider {
 * 	private static final String AUTHORITY = "my.name.space.provider";
 * 	private static final String BASE_PATH = "items";
 *
 * 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
 *
 * 	public ExampleSimpleProvider() {
 * 		super(AUTHORITY, "vnd.my.name.space.item", BASE_PATH, "MyItem", BaseColumns._ID, //
 * 				new String[]{ BaseColumns._ID, "name", "description" });
 *    }
 *
 *    {@code @}Override
 * 	protected SQLiteOpenHelper createDatabaseOpenHelper() {
 * 		return new net.twisterrob.android.db.DatabaseOpenHelper(getContext(), "MyDatabase", 1);
 *    }
 * }
 * </pre>
 *
 * Register in app manifest:
 * <pre>
 * &lt;manifest ...>
 * ...
 * 	&lt;application package="my.name.space" ...>
 * 		&lt;provider
 * 			android:name=".content.MySimpleProvider"
 * 			android:authorities="my.name.space.provider" >
 * 		&lt;/provider>
 * 	...
 * </pre>
 * @see <a href="http://www.vogella.com/tutorials/AndroidSQLite/article.html">based on</a>
 */
@SuppressWarnings("resource") // getDB is not closed at each point, it's handled in shutdown
public abstract class SimpleTableContentProvider extends ContentProvider {
	private static final int ALL_IN_DIR = 0;
	private static final int SINGLE_BY_ID = 1;

	protected final String mimeType;
	protected final String basePath;
	protected final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	protected final String tableName;
	protected final String idColumn;
	protected final String[] allColumns;

	public SimpleTableContentProvider(String authority, String mimeType, String basePath, String tableName,
			String idColumn, String... allColumns) {
		this.mimeType = mimeType;
		this.basePath = basePath;
		this.tableName = tableName;
		this.idColumn = idColumn;
		this.allColumns = allColumns;

		this.uriMatcher.addURI(authority, basePath, ALL_IN_DIR);
		this.uriMatcher.addURI(authority, basePath + "/#", SINGLE_BY_ID);
	}

	private SQLiteOpenHelper database;

	protected abstract SQLiteOpenHelper createDatabaseOpenHelper();

	@Override public synchronized boolean onCreate() {
		database = createDatabaseOpenHelper();
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override public synchronized void shutdown() {
		super.shutdown();
		database.close();
		database = null;
	}

	protected SQLiteDatabase getDB() {
		return database.getWritableDatabase();
	}

	@Override public synchronized @Nullable String getType(@NonNull Uri uri) {
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + mimeType;
			case SINGLE_BY_ID:
				return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + mimeType;
			default:
				throw unmatched(uri);
		}
	}

	@Override public synchronized @Nullable Cursor query(
			@NonNull Uri uri,
			@Nullable String[] projection,
			@Nullable String selection,
			@Nullable String[] selectionArgs,
			@Nullable String sortOrder
	) {
		SQLiteDatabase db = getDB();
		checkColumns(projection);

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(tableName);
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				// nothing to do
				break;
			case SINGLE_BY_ID:
				queryBuilder.appendWhere(idFilter(uri.getLastPathSegment()));
				break;
			default:
				throw unmatched(uri);
		}

		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		// make sure that potential listeners are getting notified
		//noinspection ConstantConditions we're past onCreate so we have a Context
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override public synchronized @Nullable Uri insert(
			@NonNull Uri uri,
			@Nullable ContentValues values
	) {
		SQLiteDatabase db = getDB();
		long id;
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				id = db.insert(tableName, null, values);
				break;
			case SINGLE_BY_ID: // cannot supply ID in Uri on insert
			default:
				throw unmatched(uri);
		}
		notify(uri);
		return uri.buildUpon().appendPath(String.valueOf(id)).build();
	}

	@Override public synchronized int delete(
			@NonNull Uri uri,
			@Nullable String selection,
			@Nullable String[] selectionArgs
	) {
		SQLiteDatabase db = getDB();
		selection = filterId(uri, selection);
		int rowsDeleted = db.delete(tableName, selection, selectionArgs);
		notify(uri);
		return rowsDeleted;
	}

	@Override public synchronized int update(
			@NonNull Uri uri,
			@Nullable ContentValues values,
			@Nullable String selection,
			@Nullable String[] selectionArgs
	) {
		SQLiteDatabase db = getDB();
		selection = filterId(uri, selection);
		int rowsUpdated = db.update(tableName, values, selection, selectionArgs);
		notify(uri);
		return rowsUpdated;
	}

	private @Nullable String filterId(@NonNull Uri uri, @Nullable String selection) {
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				// nothing to do
				break;
			case SINGLE_BY_ID:
				String id = uri.getLastPathSegment();
				selection = DatabaseUtils.concatenateWhere(idFilter(id), selection);
				break;
			default:
				throw unmatched(uri);
		}
		return selection;
	}

	private void notify(@NonNull Uri uri) {
		//noinspection ConstantConditions we're past onCreate so we have a Context
		getContext().getContentResolver().notifyChange(uri, null);
	}

	protected @NonNull String idFilter(@NonNull String id) {
		return idColumn + "=" + id;
	}

	protected @NonNull RuntimeException unmatched(@NonNull Uri uri) {
		return new UnsupportedOperationException("Unknown URI: " + uri);
	}

	/** Check if the caller has requested a column which does not exists */
	protected void checkColumns(@Nullable String... projection) {
		if (projection == null) {
			return;
		}
		Set<String> invalidColumns = new LinkedHashSet<>(Arrays.asList(projection));
		invalidColumns.removeAll(Arrays.asList(allColumns)); // known columns are not invalid
		if (!invalidColumns.isEmpty()) {
			throw new IllegalArgumentException("Unknown columns in projection: " + invalidColumns);
		}
	}
}
