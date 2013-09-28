package net.twisterrob.android.utils.tools;

import android.database.sqlite.SQLiteDatabase;

public final class DBTools {
	private DBTools() {
		// prevent instantiation
	}

	public static String toString(final SQLiteDatabase database) {
		int version = database != null? database.getVersion() : 0;
		String path = database != null? database.getPath() : null;
		return String.format("v%d@%s", version, path);
	}
}
