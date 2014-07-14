package net.twisterrob.android.db;

import java.util.*;

import android.annotation.TargetApi;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;
import android.os.Build;
import android.support.v4.database.DatabaseUtilsCompat;

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
 * 	}
 * 
 * 	{@code @}Override
 * 	protected SQLiteOpenHelper createDatabaseOpenHelper() {
 * 		return new net.twisterrob.android.db.DatabaseOpenHelper(getContext(), "MyDatabase", 1);
 * 	}
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
 * @author TWiStEr
 * @see <a href="http://www.vogella.com/tutorials/AndroidSQLite/article.html">based on</a>
 */
@SuppressWarnings("resource")
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

	@Override
	public synchronized boolean onCreate() {
		database = createDatabaseOpenHelper();
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public synchronized void shutdown() {
		super.shutdown();
		database.close();
		database = null;
	}

	protected SQLiteDatabase getDB() {
		return database.getWritableDatabase();
	}

	@Override
	public synchronized String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + mimeType;
			case SINGLE_BY_ID:
				return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + mimeType;
			default:
				throw unmatched(uri);
		}
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
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
		cursor.setNotificationUri(getContext().getContentResolver(), uri); // make sure that potential listeners are getting notified

		return cursor;
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = getDB();
		long id = 0;
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				id = db.insert(tableName, null, values);
				break;
			case SINGLE_BY_ID: // cannot supply ID in Uri on insert
			default:
				throw unmatched(uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return uri.buildUpon().appendPath(String.valueOf(id)).build();
	}

	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = getDB();
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				// nothing to do
				break;
			case SINGLE_BY_ID:
				String id = uri.getLastPathSegment();
				selection = DatabaseUtilsCompat.concatenateWhere(idFilter(id), selection);
				break;
			default:
				throw unmatched(uri);
		}
		int rowsDeleted = db.delete(tableName, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = getDB();
		switch (uriMatcher.match(uri)) {
			case ALL_IN_DIR:
				// nothing to do
				break;
			case SINGLE_BY_ID:
				String id = uri.getLastPathSegment();
				selection = DatabaseUtilsCompat.concatenateWhere(idFilter(id), selection);
				break;
			default:
				throw unmatched(uri);
		}
		int rowsUpdated = db.update(tableName, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	protected String idFilter(String id) {
		return idColumn + "=" + id;
	}

	protected RuntimeException unmatched(Uri uri) {
		return new UnsupportedOperationException("Unknown URI: " + uri);
	}

	/** Check if the caller has requested a column which does not exists */
	protected void checkColumns(String[] projection) {
		if (projection == null) {
			return;
		}
		Set<String> invalidColumns = new LinkedHashSet<String>(Arrays.asList(projection));
		invalidColumns.removeAll(Arrays.asList(allColumns)); // known columns are not invalid
		if (!invalidColumns.isEmpty()) {
			throw new IllegalArgumentException("Unknown columns in projection: " + invalidColumns);
		}
	}
}
