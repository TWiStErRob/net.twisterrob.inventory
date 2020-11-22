package net.twisterrob.android.utils.tools;

import java.lang.reflect.Field;
import java.util.*;

import android.annotation.TargetApi;
import android.database.*;
import android.database.MatrixCursor.RowBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build.*;

import androidx.annotation.*;

import net.twisterrob.android.annotation.CursorFieldType;

@SuppressWarnings("unused")
public /*static*/ abstract class DatabaseTools {
	public static final int INVALID_COLUMN = -1;
	public static final String[] NO_ARGS = new String[0];
	public static String escapeLike(Object string, char escape) {
		return string.toString().replace("%", escape + "%").replace("_", escape + "_");
	}

	private static final Map<SQLiteDatabase, String> DATABASE_TO_SQLITE_VERSION = new WeakHashMap<>();
	public static String dbToString(final @Nullable SQLiteDatabase database) {
		int userVersion = database == null? 0
				: database.getVersion();
		int schemaVersion = database == null? 0
				: (int)DatabaseUtils.longForQuery(database, "PRAGMA schema_version;", null);
		String path = database == null? null
				: database.getPath();

		String sqliteVersion = null;
		if (database != null) {
			String cachedVersion = DATABASE_TO_SQLITE_VERSION.get(database);
			if (cachedVersion != null) {
				sqliteVersion = cachedVersion;
			} else {
				sqliteVersion = getSQLiteVersion(database);
				DATABASE_TO_SQLITE_VERSION.put(database, sqliteVersion);
			}
		}
		return String.format(Locale.ROOT, "v%d(%d)::%s@%s", userVersion, schemaVersion, sqliteVersion, path);
	}

	public static @NonNull String getSQLiteVersion(@NonNull SQLiteDatabase database) {
		Cursor sqlite_version = database.rawQuery("select sqlite_version();", NO_ARGS);
		return DatabaseTools.singleString(sqlite_version, null);
	}

	public static boolean getBoolean(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndex(columnName);
		return cursor.getInt(col) != 0;
	}

	public static int getInt(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndexOrThrow(columnName);
		return cursor.getInt(col);
	}
	public static long getLong(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndexOrThrow(columnName);
		return cursor.getLong(col);
	}
	public static String getString(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndexOrThrow(columnName);
		return cursor.getString(col);
	}
	public static byte[] getBlob(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndexOrThrow(columnName);
		return cursor.getBlob(col);
	}

	public static boolean getOptionalBoolean(Cursor cursor, String columnName, boolean defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.getInt(col) != 0;
		}
		return defaultValue;
	}
	public static Boolean getOptionalBoolean(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.getInt(col) != 0;
		}
		return null;
	}

	public static int getOptionalInt(Cursor cursor, String columnName, int defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.getInt(col);
		}
		return defaultValue;
	}
	public static Integer getOptionalInt(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.isNull(col)? null : cursor.getInt(col);
		}
		return null;
	}

	public static long getOptionalLong(Cursor cursor, String columnName, long defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.getLong(col);
		}
		return defaultValue;
	}
	public static Long getOptionalLong(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.isNull(col)? null : cursor.getLong(col);
		}
		return null;
	}

	public static String getOptionalString(Cursor cursor, String columnName) {
		return getOptionalString(cursor, columnName, null);
	}
	public static String getOptionalString(Cursor cursor, String columnName, String defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.isNull(col)? null : cursor.getString(col);
		}
		return defaultValue;
	}

	public static byte[] getOptionalBlob(Cursor cursor, String columnName) {
		return getOptionalBlob(cursor, columnName, (byte[])null);
	}
	public static byte[] getOptionalBlob(Cursor cursor, String columnName, byte... defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != INVALID_COLUMN) {
			return cursor.isNull(col)? null : cursor.getBlob(col);
		}
		return defaultValue;
	}

	public static Long singleLong(@NonNull Cursor cursor, @Nullable String columnName) {
		try {
			checkSingleRow(cursor);
			int columnIndex;
			if (columnName == null) {
				checkSingleColumn(cursor);
				columnIndex = 0;
			} else {
				columnIndex = cursor.getColumnIndexOrThrow(columnName);
			}
			return cursor.isNull(columnIndex)? null : cursor.getLong(columnIndex);
		} finally {
			cursor.close();
		}
	}

	public static String singleString(@NonNull Cursor cursor, @Nullable String columnName) {
		try {
			checkSingleRow(cursor);
			if (columnName == null) {
				checkSingleColumn(cursor);
				return cursor.getString(0);
			} else {
				return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
			}
		} finally {
			cursor.close();
		}
	}

	public static byte[] singleBlob(@NonNull Cursor cursor, @Nullable String columnName) {
		try {
			checkSingleRow(cursor);
			if (columnName == null) {
				checkSingleColumn(cursor);
				return cursor.getBlob(0);
			} else {
				return cursor.getBlob(cursor.getColumnIndexOrThrow(columnName));
			}
		} finally {
			cursor.close();
		}
	}

	/** Convenience for reading {@code select 1 from ...;} style query results. */
	public static boolean singleBoolean(@NonNull Cursor cursor) {
		try {
			int onlyColumn = 0;
			return cursor.moveToFirst() // no row -> false
					&& !cursor.isNull(onlyColumn) // non-null column value
					&& cursor.getLong(onlyColumn) != 0 // coerce integer to boolean C-style
					;
		} finally {
			cursor.close();
		}
	}

	private static void checkSingleRow(@NonNull Cursor cursor) {
		if (cursor.getCount() == 0 || cursor.getColumnCount() == 0) {
			throw new IllegalArgumentException("Empty cursor");
		}
		if (1 < cursor.getCount()) {
			throw new IllegalArgumentException("Multiple rows returned");
		}
		if (!cursor.moveToFirst()) {
			throw new IllegalArgumentException("Cannot move to first item");
		}
	}
	private static void checkSingleColumn(@NonNull Cursor cursor) {
		if (1 < cursor.getColumnCount()) {
			throw new IllegalArgumentException("Multiple columns returned");
		}
	}

	/** @return the number of items consumed until the end of the cursor reached (may be different than getCount. */
	public static int consume(Cursor cursor) {
		try {
			int count = 0;
			while (cursor.moveToNext()) {
				// read to the end
				count++;
			}
			return count;
		} finally {
			cursor.close();
		}
	}

	public static boolean isPragma(SQLiteDatabase db, String pragmaName) {
		Cursor cursor = db.rawQuery("PRAGMA " + pragmaName + ";", NO_ARGS);
		return singleBoolean(cursor);
	}
	// long for now, explode to more return types if needed
	public static long getPragma(SQLiteDatabase db, String pragmaName) {
		Cursor cursor = db.rawQuery("PRAGMA " + pragmaName + ";", NO_ARGS);
		Long value = singleLong(cursor, null);
		if (value == null) {
			throw new IllegalArgumentException("Pragma " + pragmaName + " doesn't have a value");
		}
		return value;
	}
	public static void setPragma(SQLiteDatabase db, String pragmaName, boolean isEnabled) {
		setPragma(db, pragmaName, String.valueOf(isEnabled));
	}
	public static void setPragma(SQLiteDatabase db, String pragmaName, String value) {
		Cursor cursor = db.rawQuery("PRAGMA " + pragmaName + " = " + value + ";", NO_ARGS);
		consume(cursor);
	}
	public static int callPragma(SQLiteDatabase db, String pragmaName, Object... args) {
		StringBuilder query = new StringBuilder("PRAGMA");
		query.append(' ').append(pragmaName);
		query.append('(');
		boolean first = true;
		for (Object arg : args) { // for some reason using ? doesn't work with pragmas
			if (!first) {
				query.append(", ");
			} else {
				first = false;
			}
			query.append(arg);
		}
		query.append(')');
		Cursor cursor = db.rawQuery(query.append(';').toString(), NO_ARGS);
		return consume(cursor);
	}

	public static String dumpCursorToString(Cursor cursor) {
		StringBuilder cursorDump = new StringBuilder();
		DatabaseUtils.dumpCursor(cursor, cursorDump);
		return cursorDump.toString();
	}

	public static Iterable<Cursor> iterate(final Cursor cursor) {
		return new Iterable<Cursor>() {
			@Override public Iterator<Cursor> iterator() {
				return new Iterator<Cursor>() {
					@Override public boolean hasNext() {
						return cursor.getPosition() + 1 < cursor.getCount();
					}
					@Override public Cursor next() {
						if (!cursor.moveToNext()) {
							throw new IllegalStateException("Cannot move to next row in Cursor.");
						}
						return cursor;
					}
					@Override public void remove() {
						throw new UnsupportedOperationException("Cannot remove a row from a Cursor");
					}
				};
			}
		};
	}

	public static Cursor ensureFirst(Cursor cursor) {
		if (!cursor.isFirst()) {
			if (!cursor.moveToFirst()) {
				throw new IllegalStateException("Cannot move to first row in Cursor.");
			}
		}
		return cursor;
	}

	public static MatrixCursor clone(Cursor cursor) {
		MatrixCursor clone = new MatrixCursor(cursor.getColumnNames(), cursor.getCount());
		while (cursor.moveToNext()) {
			RowBuilder row = clone.newRow();
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				CursorColumnType type = CursorColumnType.fromFieldType(getType(cursor, i));
				row.add(type.getValue(cursor, i));
			}
		}
		return clone;
	}

	public static @CursorFieldType int getType(Cursor cursor, String columnName) {
		return getType(cursor, cursor.getColumnIndexOrThrow(columnName));
	}

	@SuppressWarnings("deprecation")
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static @CursorFieldType int getType(Cursor cursor, int columnIndex) {
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			//noinspection WrongConstant returns exactly what's needed as per docs
			return cursor.getType(columnIndex);
		}
		cursor = unwrap(cursor);
		if (!(cursor instanceof CrossProcessCursor)) {
			throw new IllegalArgumentException("Cannot get type if it's not a " + CrossProcessCursor.class);
		}

		CursorWindow window = ((CrossProcessCursor)cursor).getWindow();
		int pos = cursor.getPosition();
		int type;
		if (window.isNull(pos, columnIndex)) {
			type = Cursor.FIELD_TYPE_NULL;
		} else if (window.isLong(pos, columnIndex)) {
			type = Cursor.FIELD_TYPE_INTEGER;
		} else if (window.isFloat(pos, columnIndex)) {
			type = Cursor.FIELD_TYPE_FLOAT;
		} else if (window.isString(pos, columnIndex)) {
			type = Cursor.FIELD_TYPE_STRING;
		} else if (window.isBlob(pos, columnIndex)) {
			type = Cursor.FIELD_TYPE_BLOB;
		} else {
			throw new IllegalArgumentException("Column has no type.");
		}
		return type;
	}

	private static Cursor unwrap(Cursor cursor) {
		while (cursor instanceof CursorWrapper) {
			if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
				cursor = ((CursorWrapper)cursor).getWrappedCursor();
			} else {
				try {
					@SuppressWarnings("JavaReflectionMemberAccess") 
					Field mCursor = CursorWrapper.class.getDeclaredField("mCursor");
					mCursor.setAccessible(true);
					cursor = (Cursor)mCursor.get(cursor);
				} catch (Exception ex) {
					throw new IllegalStateException("Cannot access CursorWrapper.mCursor", ex);
				}
			}
		}
		return cursor;
	}

	protected DatabaseTools() {
		// static utility class
	}

	public interface Pragma {
		String RECURSIVE_TRIGGERS = "recursive_triggers";
		String FOREIGN_KEYS = "foreign_keys";
		String AUTO_VACUUM = "auto_vacuum";
		String AUTO_VACUUM_FULL = "FULL";
		String AUTO_VACUUM_INCREMENTAL = "INCREMENTAL";
		String INCREMENTAL_VACUUM = "incremental_vacuum";
		String PAGE_SIZE = "page_size";
		String FREELIST_COUNT = "freelist_count";
	}
}
