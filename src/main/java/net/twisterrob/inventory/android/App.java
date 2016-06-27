package net.twisterrob.inventory.android;

import java.util.*;

import org.slf4j.*;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.impl.AndroidLoggerFactory;

import android.annotation.*;
import android.app.Application;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.*;
import android.os.Build.*;
import android.os.StrictMode;
import android.os.StrictMode.*;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.preference.PreferenceManager;
import android.support.annotation.*;
import android.util.Log;
import android.widget.Toast;

import net.twisterrob.android.utils.concurrent.BackgroundExecution;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.java.utils.StringTools;

public class App extends Application {
	static {
		// Make sure to set up LoggerFactory before the first logger is created
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.inventory\\.android\\.(.+\\.)?", "");
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.android\\.(.+\\.)?", "");
	}

	// This is the first Logger created which will result in reading the classpath to create the binding.
	// Make sure the strict mode is set up after this!
	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	static {
		setStrictMode();
		if (BuildConfig.DEBUG) {
			//android.app.FragmentManager.enableDebugLogging(true);
			//android.support.v4.app.FragmentManager.enableDebugLogging(true);
			//android.app.LoaderManager.enableDebugLogging(true);
			//android.support.v4.app.LoaderManager.enableDebugLogging(true);
		}
	}

	private static App s_instance;
	private Database database;

	public App() {
		synchronized (App.class) {
			if (s_instance != null) {
				throw new IllegalStateException("Multiple applications running at the same time?!");
			}
			s_instance = this;
		}
	}

	public static @NonNull App getInstance() {
		return s_instance;
	}

	private void logStartup() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			Log.e("App", MessageFormatter.arrayFormat("************ Starting up {} {} installed at {}", new Object[] {
					getPackageName(), BuildConfig.VERSION_NAME, new Date(info.lastUpdateTime)}).getMessage());
		} catch (NameNotFoundException ex) {
			LOG.warn("************* Starting up {} {}", getPackageName(), BuildConfig.VERSION_NAME, ex);
		}
	}

	@Override public void onCreate() {
		// StrictModeDiskReadViolation on startup, but there isn't really a good way around these
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			// may cause StrictModeDiskReadViolation if Application.onCreate calls
			// android.graphics.Typeface.SetAppTypeFace (this happened on Galaxy S3 with custom font set up)
			super.onCreate();
			logStartup();

			if (BuildConfig.DEBUG) {
				AndroidTools.setContext(this);
//				CONSIDER com.idescout.sql.SqlScoutServer.create(this, getPackageName());
//				com.facebook.stetho.Stetho.initializeWithDefaults(this); // reads /proc/self/cmdline
			}
			// may cause StrictModeDiskReadViolation, but necessary for startup since anything can read the preferences
			PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
			database = new Database(this);
			// may cause StrictModeDiskReadViolation if prefs are not loaded yet, because it reads prefs 
			updateLanguage(Locale.getDefault());
			new BackgroundExecution(new Runnable() {
				@Override public void run() {
					try {
						// preload on startup
						CategoryDTO.getCache(App.this);
					} catch (Exception ex) {
						// if fails we'll crash later when used, but at least let the app start up
						LOG.error("Failed to initialize Category cache", ex);
					}
				}
			}).execute();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLanguage(AndroidTools.getLocale(newConfig));
	}

	private void updateLanguage(Locale newLocale) {
		final SharedPreferences prefs = getPrefs();
		final String storedLanguage = prefs.getString(getString(R.string.pref_currentLanguage), null);
		final String currentLanguage = newLocale.toString();
		if (!currentLanguage.equals(storedLanguage)) {
			String from = StringTools.toLocale(storedLanguage).getDisplayName();
			String to = StringTools.toLocale(currentLanguage).getDisplayName();
			String message = getAppContext().getString(R.string.message_locale_changed, from, to);
			LOG.debug(message);
			App.toast(message);
			new BackgroundExecution(new Runnable() {
				@WorkerThread
				@Override public void run() {
					try {
						database.updateCategoryCache(App.getAppContext());
						LOG.debug("Locale update successful: {} -> {}", storedLanguage, currentLanguage);
						prefs.edit().putString(getString(R.string.pref_currentLanguage), currentLanguage).apply();
					} catch (Exception ex) {
						LOG.error("Locale update failed: {} -> {}", storedLanguage, currentLanguage, ex);
					}
				}
			}).execute();
		}
	}

	@SuppressWarnings("WrongThread")
	@Override public void onTerminate() {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			database.getWritableDatabase().close();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
		super.onTerminate();
	}

	public static @NonNull Context getAppContext() {
		return getInstance();
	}

	// TODO wrapper for resource based retrieval: getBPref/getSPref/etc...
	public static @NonNull SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getAppContext());
	}

	/** Get boolean Preference */
	public static boolean getBPref(@StringRes int prefName, @BoolRes int defaultRes) {
		String prefKey = App.getAppContext().getString(prefName);
		boolean prefDefault = App.getAppContext().getResources().getBoolean(defaultRes);
		return App.getPrefs().getBoolean(prefKey, prefDefault);
	}
	/** Get String Preference */
	public static String getSPref(@StringRes int prefName, String prefDefaultValue) {
		String prefKey = App.getAppContext().getString(prefName);
		return App.getPrefs().getString(prefKey, prefDefaultValue);
	}
	/** Get String Preference */
	public static String getSPref(@StringRes int prefName, @StringRes int defaultRes) {
		String prefKey = App.getAppContext().getString(prefName);
		String prefDefaultValue = App.getAppContext().getResources().getString(defaultRes);
		return App.getPrefs().getString(prefKey, prefDefaultValue);
	}
	/** Set String Preference */
	public static void setSPref(@StringRes int prefName, String value) {
		String prefKey = App.getAppContext().getString(prefName);
		getPrefEditor().putString(prefKey, value).apply();
	}

	/** @return You must call {@link SharedPreferences.Editor#commit} as per {@link SharedPreferences#edit} contract. */
	@SuppressLint("CommitPrefEdits")
	public static @NonNull SharedPreferences.Editor getPrefEditor() {
		return getPrefs().edit();
	}

	public static void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * android.database.DatabaseTools.dumpCursor(net.twisterrob.inventory.android.
	 * App.db().getReadableDatabase().rawQuery("select * from sqlite_sequence;", null));
	 */
	public static Database db() {
		return getInstance().database;
	}

	@UiThread
	public static void toast(CharSequence message) {
		if (BuildConfig.DEBUG) {
			//LOG.info("Debug Toast: {}", message, new StackTrace());
			Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
		}
	}

	@UiThread
	public static void toastUser(CharSequence message) {
		//LOG.trace("User Toast: {}", message, new StackTrace());
		Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
	}

	public static CharSequence getError(Throwable ex, int errorResource, Object... args) {
		return getError(ex, getAppContext().getString(errorResource, args));
	}
	public static CharSequence getError(Throwable ex, CharSequence message) {
		CharSequence errorMessage = ex.toString();
		Resources res = getAppContext().getResources();
		String msg = ex.getMessage();
		if (msg == null) {
			msg = "";
		}
		if (msg.equals("constraint failed (code 19)")) {
			errorMessage = res.getString(R.string.generic_error_length_name);
		} else if (msg.equals("column name is not unique (code 19)")
				|| msg.equals("columns property, name are not unique (code 19)")
				|| msg.equals("columns parent, name are not unique (code 19)")) {
			errorMessage = res.getString(R.string.generic_error_unique_name);
		}
		return message + " " + errorMessage;
	}

	/**
	 * Set up StrictMode in a way that doesn't interfere much with development,
	 * but tries to tell you any violations available in all possible ways (except death).
	 */
	@TargetApi(VERSION_CODES.M)
	private static void setStrictMode() {
		if (!BuildConfig.DEBUG) {
			return;
		}
		if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
			return; // StrictMode was added in API 9
		}
		Builder threadBuilder = new Builder();
		if (VERSION_CODES.GINGERBREAD <= VERSION.SDK_INT) {
			threadBuilder = threadBuilder
					.detectDiskReads()
					.detectDiskWrites()
					.detectNetwork()
					.penaltyLog()
					.penaltyDialog()
					.penaltyDropBox()
					.penaltyDeath()
			;
		}
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			threadBuilder = threadBuilder
					.detectCustomSlowCalls()
					.penaltyFlashScreen()
					.penaltyDeathOnNetwork()
			;
		}

		if (VERSION_CODES.M <= VERSION.SDK_INT) {
			threadBuilder = threadBuilder
					.detectResourceMismatches()
			;
		}
		StrictMode.setThreadPolicy(threadBuilder.build());

		VmPolicy.Builder vmBuilder = new VmPolicy.Builder();
		if (VERSION_CODES.GINGERBREAD <= VERSION.SDK_INT) {
			vmBuilder = vmBuilder
					.detectLeakedSqlLiteObjects()
					.penaltyLog()
					.penaltyDropBox()
//					.penaltyDeath() // don't die on android.os.StrictMode$InstanceCountViolation: class ...Activity; instances=2; limit=1
			;
		}
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
		if (VERSION_CODES.M <= VERSION.SDK_INT) {
			vmBuilder = vmBuilder
					.detectCleartextNetwork()
					.penaltyDeathOnCleartextNetwork()
			;
		}
		StrictMode.setVmPolicy(vmBuilder.build());
	}
	public static void notImplemented() {
		toastUser("Not implemented yet, sorry. Please send feedback on what you were using so we can implement it.");
	}
}
