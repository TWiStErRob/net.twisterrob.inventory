package net.twisterrob.inventory.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.twisterrob.inventory.android.db.Database;

public class App extends Application {
	private static App s_instance;
	private boolean initialized = false;
	private Database database;

	public App() {
		s_instance = this;
	}

	public static App getInstance() {
		s_instance.afterPropertiesSet();
		return s_instance;
	}

	private void afterPropertiesSet() {
		if (!initialized) {
			database = new Database(this);
			initialized = true;
		}
	}

	public Database getDataBase() {
		return database;
	}

	public static SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(App.getInstance());
	}

	public static SharedPreferences.Editor getPrefEditor() {
		SharedPreferences prefs = getPrefs();
		return prefs != null? prefs.edit() : null;
	}

	public static void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
