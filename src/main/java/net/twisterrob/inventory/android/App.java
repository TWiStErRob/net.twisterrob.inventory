package net.twisterrob.inventory.android;

import java.util.Locale;

import org.slf4j.*;
import org.slf4j.impl.AndroidLoggerFactory;

import android.annotation.*;
import android.app.Application;
import android.content.*;
import android.content.res.Configuration;
import android.os.Build.*;
import android.os.StrictMode;
import android.os.StrictMode.*;
import android.preference.PreferenceManager;
import android.support.annotation.*;
import android.widget.Toast;

import net.twisterrob.android.utils.concurrent.BackgroundExecution;
import net.twisterrob.inventory.android.Constants.Prefs;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.java.utils.StringTools;

public class App extends Application {
	// This is the first Logger created which will result in reading the classpath to create the binding.
	// Make sure the strict mode is set up after this!
	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	static {
		if (BuildConfig.DEBUG) {
			setStrictMode();
			//android.app.FragmentManager.enableDebugLogging(true);
			//android.support.v4.app.FragmentManager.enableDebugLogging(true);
			//android.app.LoaderManager.enableDebugLogging(true);
			//android.support.v4.app.LoaderManager.enableDebugLogging(true);
		}
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.inventory\\.android\\.(.+\\.)?", "");
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.android\\.(.+\\.)?", "");
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

	public static App getInstance() {
		return s_instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LOG.info("************* Starting up {} {} built at {}",
				getPackageName(), BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME);

		// StrictModeDiskReadViolation on startup, but there isn't really a good way around it,
		// since it needs to be loaded for the following code to work, make an exception:
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		database = new Database(this);
		updateLanguage(Locale.getDefault()); // reads prefs, so may cause StrictModeDiskReadViolation if not loaded yet
		StrictMode.setThreadPolicy(originalPolicy);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLanguage(newConfig.locale);
	}

	private void updateLanguage(Locale newLocale) {
		final SharedPreferences prefs = getPrefs();
		final String storedLanguage = prefs.getString(Prefs.CURRENT_LANGUAGE, null);
		final String currentLanguage = newLocale.toString();
		if (!currentLanguage.equals(storedLanguage)) {
			String from = StringTools.toLocale(storedLanguage).getDisplayName();
			String to = StringTools.toLocale(currentLanguage).getDisplayName();
			String message = getAppContext().getString(R.string.message_locale_changed, from, to);
			LOG.debug(message);
			App.toast(message);
			new BackgroundExecution(new Runnable() {
				@Override public void run() {
					database.updateCategoryCache(App.getAppContext());
					LOG.debug("Locale update successful: {} -> {}", storedLanguage, currentLanguage);
					prefs.edit().putString(Prefs.CURRENT_LANGUAGE, currentLanguage).apply();
				}
			}).execute();
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

	// TODO wrapper for resource based retrieval
	public static SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getAppContext());
	}

	public static boolean getBPref(@StringRes int prefName, @BoolRes int defaultRes) {
		String prefKey = App.getAppContext().getString(prefName);
		boolean prefDefault = App.getAppContext().getResources().getBoolean(defaultRes);
		return App.getPrefs().getBoolean(prefKey, prefDefault);
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

	/**
	 * android.database.DatabaseTools.dumpCursor(net.twisterrob.inventory.android.
	 * App.db().getReadableDatabase().rawQuery("select * from sqlite_sequence;", null));
	 */
	public static Database db() {
		return getInstance().database;
	}

	public static void toast(CharSequence message) {
		if (BuildConfig.DEBUG) {
			//LOG.info("Debug Toast: {}", message, new StackTrace());
			Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
		}
	}

	public static void toastUser(CharSequence message) {
		//LOG.trace("User Toast: {}", message, new StackTrace());
		Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
	}

	public static CharSequence getError(Throwable ex, int errorResource, Object... args) {
		return getError(ex, getAppContext().getString(errorResource, args));
	}
	public static CharSequence getError(Throwable ex, CharSequence message) {
		CharSequence errorMessage = ex.toString();
		if ("column name is not unique (code 19)".equals(ex.getMessage())) {
			errorMessage = getAppContext().getString(R.string.generic_error_unique_name);
		} else if ("constraint failed (code 19)".equals(ex.getMessage())) {
			errorMessage = getAppContext().getString(R.string.generic_error_length_name);
		}
		return message + " " + errorMessage;
	}

	@TargetApi(VERSION_CODES.KITKAT)
	private static void setStrictMode() {
		if (VERSION_CODES.GINGERBREAD <= VERSION.SDK_INT) {
			ThreadPolicy.Builder threadBuilder = new ThreadPolicy.Builder();
			threadBuilder = threadBuilder
					.detectDiskReads()
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
