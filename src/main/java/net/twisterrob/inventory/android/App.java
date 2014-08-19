package net.twisterrob.inventory.android;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

import org.slf4j.*;
import org.slf4j.impl.AndroidLoggerFactory;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.*;
import android.content.res.Configuration;
import android.os.*;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.PicassoWrapper;

import net.twisterrob.android.utils.tools.StringTools;
import net.twisterrob.inventory.*;
import net.twisterrob.inventory.android.Constants.Prefs;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.utils.DriveIdDownloader.ApiClientProvider;

public class App extends Application {
	static {
		if (BuildConfig.DEBUG) {
			setStrictMode();
		}
		AndroidLoggerFactory.addReplacement("^com\\.squareup\\.picasso\\.", "net.twisterrob.inventory.android.util.");
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.inventory\\.android\\.(.+\\.)?", "");
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.android\\.(.+\\.)?", "");
	}

	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	private static App s_instance;
	private Database database;
	private PicassoWrapper picasso;
	private File phoneHome;

	public App() {
		synchronized (App.class) {
			if (s_instance != null) {
				throw new IllegalStateException("Multiple applications running at the same time?!");
			}
			s_instance = this;
		}
	}

	@SuppressLint("NewApi")
	private static void setStrictMode() {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
				//.detectDiskReads() //
				.detectDiskWrites() //
				.detectNetwork() //
				.penaltyDeathOnNetwork() //
				.detectCustomSlowCalls() //
				.penaltyLog() //
				.penaltyDialog() //
				.penaltyDropBox() //
				.penaltyFlashScreen() //
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder() //
				//.detectActivityLeaks() //
				.detectFileUriExposure() //
				.detectLeakedClosableObjects() //
				.detectLeakedRegistrationObjects() //
				.detectLeakedSqlLiteObjects() //
				.penaltyLog() //
				//.penaltyDeath() //
				.build());
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
		picasso = new PicassoWrapper(this);
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

	private WeakReference<ApiClientProvider> provider = new WeakReference<ApiClientProvider>(null);
	public static void setApiClientProvider(ApiClientProvider current) {
		getInstance().provider = new WeakReference<ApiClientProvider>(current);
	}
	public static GoogleApiClient getConnectedClient() {
		ApiClientProvider current = getInstance().provider.get();
		if (current != null) {
			return current.getConnectedClient();
		}
		return null;
	}

	public File getPhoneHome() {
		return phoneHome;
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

	public static void toast(String message) {
		LOG.info("Long Toast: {}", message);
		Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
	}
}
