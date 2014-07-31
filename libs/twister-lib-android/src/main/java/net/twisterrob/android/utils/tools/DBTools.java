package net.twisterrob.android.utils.tools;

import java.util.Locale;

import android.database.sqlite.SQLiteDatabase;

public final class DBTools {
	private DBTools() {
		// prevent instantiation
	}

	public static String escapeLike(Object string, char escape) {
		return string.toString().replace("%", escape + "%").replace("_", escape + "_");
	}

	public static String toString(final SQLiteDatabase database) {
		int version = database != null? database.getVersion() : 0;
		String path = database != null? database.getPath() : null;
		return String.format(Locale.ROOT, "v%d@%s", version, path);
	}
}
