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
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.twisterrob.android.utils.concurrent.BackgroundExecution;
import net.twisterrob.inventory.android.Constants.Prefs;
import net.twisterrob.inventory.android.content.Database;
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
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		database = new Database(this);
		updateLanguage(Locale.getDefault());
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
					database.updateCategoryCache();
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

	/**
	 * android.database.DatabaseTools.dumpCursor(net.twisterrob.inventory.android.
	 * App.db().getReadableDatabase().rawQuery("select * from sqlite_sequence;", null));
	 */
	public static Database db() {
		return getInstance().database;
	}

	public static void toast(CharSequence message) {
		LOG.info("Debug Toast: {}", message, new StackTrace());
		Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
	}

	public static void toastUser(CharSequence message) {
		//LOG.trace("User Toast: {}", message, new StackTrace());
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
