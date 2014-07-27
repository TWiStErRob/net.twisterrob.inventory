package net.twisterrob.inventory.android;

import org.slf4j.impl.AndroidLoggerFactory;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import net.twisterrob.inventory.BuildConfig;
import net.twisterrob.inventory.android.content.Database;

public class App extends Application {
	private static App s_instance;
	private boolean initialized = false;
	private Database database;

	public App() {
		s_instance = this;

		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.inventory\\.android\\.(.+\\.)?", "");
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.android\\.(.+\\.)?", "");

		if (BuildConfig.DEBUG) {
			setStrictMode();
		}
	}

	@SuppressLint("NewApi")
	private static void setStrictMode() {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
				.detectAll() //
				.penaltyLog() //
				.penaltyDialog() //
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder() //
				//.detectActivityLeaks() //
				.detectFileUriExposure() //
				.detectLeakedClosableObjects() //
				.detectLeakedRegistrationObjects() //
				.detectLeakedSqlLiteObjects() //
				.penaltyLog() //
				.penaltyDeath() //
				.build());
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

	/**
	 * net.twisterrob.inventory.android.App.getInstance().getDataBase().getReadableDatabase()
	   .rawQuery("select * from sqlite_sequence;", null);
	 */
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
