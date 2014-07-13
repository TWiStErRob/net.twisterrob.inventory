package net.twisterrob.inventory.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.twisterrob.inventory.android.db.Database;

public class App extends Application {
	private static App s_instance;
	private Database database = new Database(this);

	public App() {
		s_instance = this;
	}

	public static App getInstance() {
		return s_instance;
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
