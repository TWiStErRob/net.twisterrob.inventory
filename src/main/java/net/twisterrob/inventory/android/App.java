package net.twisterrob.inventory.android;

import org.slf4j.impl.AndroidLoggerFactory;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.*;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import net.twisterrob.inventory.BuildConfig;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.utils.PicassoWrapper;

public class App extends Application {
	private static App s_instance;
	private boolean initialized = false;
	private Database database;
	private PicassoWrapper picasso;

	public App() {
		if (s_instance != null) {
			throw new IllegalStateException("Multiple applications running at the same time?!");
		}
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

	private static App getInstance() {
		s_instance.afterPropertiesSet();
		return s_instance;
	}

	public static Context getAppContext() {
		return getInstance();
	}

	/**
	 * Used to prevent escaping an uninitialized instance in the constructor.
	 */
	private synchronized void afterPropertiesSet() {
		if (!initialized) {
			database = new Database(this);
			picasso = new PicassoWrapper(this);
			initialized = true;
		}
	}

	public static SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getAppContext());
	}

	public static SharedPreferences.Editor getPrefEditor() {
		SharedPreferences prefs = getPrefs();
		return prefs != null? prefs.edit() : null;
	}

	public static void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static PicassoWrapper pic() {
		return getInstance().picasso;
	}

	/**
	 * android.database.DatabaseUtils.dumpCursor(net.twisterrob.inventory.android.
	 * App.db().getReadableDatabase().rawQuery("select * from sqlite_sequence;", null));
	 */
	public static Database db() {
		return getInstance().database;
	}
}
