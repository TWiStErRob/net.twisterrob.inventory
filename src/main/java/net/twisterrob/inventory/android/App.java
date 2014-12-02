package net.twisterrob.inventory.android;

import java.io.File;
import java.util.Locale;

import org.slf4j.*;
import org.slf4j.impl.AndroidLoggerFactory;

import android.annotation.*;
import android.app.Application;
import android.content.*;
import android.content.res.Configuration;
import android.os.*;
import android.os.Build.*;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.twisterrob.inventory.android.Constants.Prefs;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.utils.ImageLoaderFacade;
import net.twisterrob.java.exceptions.StackTrace;
import net.twisterrob.java.utils.StringTools;

public class App extends Application {
	static {
		if (BuildConfig.DEBUG) {
			setStrictMode();
			//android.support.v4.app.LoaderManager.enableDebugLogging(true);
		}
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.inventory\\.android\\.(.+\\.)?", "");
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.android\\.(.+\\.)?", "");
	}

	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	private static App s_instance;
	private Database database;
	private File phoneHome;

	public App() {
		synchronized (App.class) {
			if (s_instance != null) {
				throw new IllegalStateException("Multiple applications running at the same time?!");
			}
			s_instance = this;
		}
	}

	public static App getInstance() {
		return s_instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		phoneHome = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		database = new Database(this);
		updateLanguage(Locale.getDefault());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLanguage(newConfig.locale);
	}

	private void updateLanguage(Locale newLocale) {
		final String currentLanguage = newLocale.toString();
		final SharedPreferences prefs = getPrefs();
		String storedLanguage = prefs.getString(Prefs.CURRENT_LANGUAGE, null);
		if (!currentLanguage.equals(storedLanguage)) {
			String from = StringTools.toLocale(storedLanguage).getDisplayName();
			String to = StringTools.toLocale(currentLanguage).getDisplayName();
			String message = getAppContext().getString(R.string.message_locale_changed, from, to);
			App.toast(message);
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					database.updateCategoryCache();
					prefs.edit().putString(Prefs.CURRENT_LANGUAGE, currentLanguage).apply();
					return null;
				}
			}.execute();
		}
	}

	@Override
	public void onTerminate() {
		database.getWritableDatabase().close();
		super.onTerminate();
	}

	public static Context getAppContext() {
		return getInstance();
	}

	public File getPhoneHome() {
		return phoneHome;
	}

	public static SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getAppContext());
	}

	/** @return You must call {@link SharedPreferences.Editor#commit} as per {@link SharedPreferences#edit} contract. */
	@SuppressLint("CommitPrefEdits")
	public static SharedPreferences.Editor getPrefEditor() {
		SharedPreferences prefs = getPrefs();
		return prefs != null? prefs.edit() : null;
	}

	public static void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static ImageLoaderFacade pic() {
		return ImageLoaderFacade.getInstance();
	}

	/**
	 * android.database.DatabaseTools.dumpCursor(net.twisterrob.inventory.android.
	 * App.db().getReadableDatabase().rawQuery("select * from sqlite_sequence;", null));
	 */
	public static Database db() {
		return getInstance().database;
	}

	public static void toast(String message) {
		LOG.info("Long Toast: {}", message, new StackTrace());
		Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
	}

	@TargetApi(VERSION_CODES.KITKAT)
	private static void setStrictMode() {
		if (VERSION_CODES.GINGERBREAD <= VERSION.SDK_INT) {
			Builder threadBuilder = new Builder();
			threadBuilder = threadBuilder
					//.detectDiskReads()
					.detectDiskWrites()
					.detectNetwork()
					.penaltyLog()
					.penaltyDialog()
					.penaltyDropBox()
			;
			if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
				threadBuilder = threadBuilder
						.penaltyDeathOnNetwork()
						.detectCustomSlowCalls()
						.penaltyFlashScreen()
				;
			}
			StrictMode.setThreadPolicy(threadBuilder.build());

			VmPolicy.Builder vmBuilder = new VmPolicy.Builder();
			vmBuilder = vmBuilder
					.detectLeakedSqlLiteObjects()
							//.penaltyDeath()
					.penaltyLog()
			;
			if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
				vmBuilder = vmBuilder
						.detectLeakedClosableObjects()
						.detectActivityLeaks()
				;
			}
			if (VERSION_CODES.JELLY_BEAN_MR2 <= VERSION.SDK_INT) {
				vmBuilder = vmBuilder
						.detectFileUriExposure()
				;
			}
			if (VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
				vmBuilder = vmBuilder
						.detectLeakedRegistrationObjects()
				;
			}

			StrictMode.setVmPolicy(vmBuilder.build());
		}
	}
}
