package net.twisterrob.android.utils.tools;

import java.util.*;

import javax.annotation.Nullable;

import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import net.twisterrob.android.db.DatabaseOpenHelper;

@SuppressWarnings("unused")
public /*static*/ abstract class DatabaseTools {
	public static String escapeLike(Object string, char escape) {
		return string.toString().replace("%", escape + "%").replace("_", escape + "_");
	}
	public static String dbToString(final SQLiteDatabase database) {
		int version = database != null? database.getVersion() : 0;
		String path = database != null? database.getPath() : null;
		return String.format(Locale.ROOT, "v%d@%s", version, path);
	}
	public static boolean getBoolean(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndex(columnName);
		return cursor.getInt(col) != 0;
	}
	public static boolean getOptionalBoolean(Cursor cursor, String columnName, boolean defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			return cursor.getInt(col) != 0;
		}
		return defaultValue;
	}

	public static int getOptionalInt(Cursor cursor, String columnName, int defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			return cursor.getInt(col);
		}
		return defaultValue;
	}
	public static Integer getOptionalInt(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndex(columnName);
		if (col != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			return cursor.isNull(col)? null : cursor.getInt(col);
		}
		return null;
	}
	public static long getOptionalLong(Cursor cursor, String columnName, long defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			return cursor.getLong(col);
		}
		return defaultValue;
	}
	public static Long getOptionalLong(Cursor cursor, String columnName) {
		int col = cursor.getColumnIndex(columnName);
		if (col != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			return cursor.isNull(col)? null : cursor.getLong(col);
		}
		return null;
	}
	public static String getOptionalString(Cursor cursor, String columnName) {
		return getOptionalString(cursor, columnName, null);
	}
	public static String getOptionalString(Cursor cursor, String columnName, String defaultValue) {
		int col = cursor.getColumnIndex(columnName);
		if (col != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			return cursor.isNull(col)? null : cursor.getString(col);
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

	protected DatabaseTools() {
		// static utility class
	}
}
