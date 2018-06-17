package net.twisterrob.inventory.android.backup;

import android.content.Context;

import net.twisterrob.android.app.BaseApp;
import net.twisterrob.inventory.android.content.Database;

public class DBProvider {
	public static Database db(Context context) {
		return (Database)((BaseApp)context.getApplicationContext()).getDatabase();
	}
}
