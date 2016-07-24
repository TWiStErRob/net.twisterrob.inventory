package net.twisterrob.android.app;

import java.lang.reflect.*;
import java.util.Date;

import org.slf4j.*;
import org.slf4j.helpers.*;
import org.slf4j.impl.AndroidLoggerFactory;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.*;
import android.os.StrictMode;
import android.os.StrictMode.*;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.preference.PreferenceManager;
import android.support.annotation.*;
import android.util.Log;
import android.widget.Toast;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.android.utils.tools.AndroidTools;

public class BaseApp extends android.app.Application {
	static {
		// TODO figure out something so IDEA sees these classes, but they're not on lib's test classpath.
		// Make sure to set up LoggerFactory before the first logger is created
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.android\\.(.+\\.)?", "");
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.java\\.(.+\\.)?", "");
	}

	// This is the first Logger created which will result in reading the classpath to create the binding.
	// Make sure the strict mode is set up after this!
	private static final Logger LOG = LoggerFactory.getLogger(BaseApp.class);

	private static BaseApp s_instance;
	private boolean BuildConfigDEBUG;
	/**
	 * android.database.DatabaseTools.dumpCursor(net.twisterrob.inventory.android.
	 * App.db().getReadableDatabase().rawQuery("select * from sqlite_sequence;", null));
	 */
	protected Object database;
	private ResourcePreferences prefs;
	private int preferencesResource;

	public BaseApp(boolean debugMode, @XmlRes int preferences) {
		synchronized (BaseApp.class) {
			if (s_instance != null) {
				throw new IllegalStateException("Multiple applications running at the same time?!");
			}
			this.preferencesResource = preferences;
			this.BuildConfigDEBUG = debugMode;
			if (BuildConfigDEBUG) {
				setStrictMode();
			}
			//android.app.FragmentManager.enableDebugLogging(true);
			//android.support.v4.app.FragmentManager.enableDebugLogging(true);
			//android.app.LoaderManager.enableDebugLogging(true);
			//android.support.v4.app.LoaderManager.enableDebugLogging(true);

			s_instance = this;
		}
	}

	protected void logStartup() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			FormattingTuple message = MessageFormatter.arrayFormat(
					"************ Starting up {} {} ({}) installed at {}", new Object[] {
							getPackageName(), info.versionName, info.versionCode, new Date(info.lastUpdateTime)
					});
			// Could be wtf() except that does other things than just logging.
			Log.e("App", message.getMessage());
		} catch (NameNotFoundException ex) {
			LOG.warn("************* Starting up {}", getPackageName(), ex);
		}
	}

	public void onCreate() {
		// StrictModeDiskReadViolation on startup, but there isn't really a good way around these
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			// may cause StrictModeDiskReadViolation if Application.onCreate calls
			// android.graphics.Typeface.SetAppTypeFace (this happened on Galaxy S3 with custom font set up)
			super.onCreate();
			logStartup();
			safeOnCreate();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	protected void safeOnCreate() {
		if (BuildConfigDEBUG) {
			AndroidTools.setContext(this);
			initStetho();
		}
		if (preferencesResource != AndroidTools.INVALID_RESOURCE_ID) {
			// may cause StrictModeDiskReadViolation, but necessary for startup since anything can read the preferences
			PreferenceManager.setDefaultValues(this, preferencesResource, false);
		}
		prefs = new ResourcePreferences(getResources(), PreferenceManager.getDefaultSharedPreferences(this));
		database = createDatabase();
	}

	/**
	 * Do a reflective initialization if it's on the classpath.
	 */
	@SuppressWarnings("TryWithIdenticalCatches")
	protected void initStetho() {
		// CONSIDER com.idescout.sql.SqlScoutServer.create(this, getPackageName());
		try {
			// com.facebook.stetho.Stetho.initializeWithDefaults(this); // reads /proc/self/cmdline
			Class<?> stetho = Class.forName("com.facebook.stetho.Stetho");
			Method initializeWithDefaults = stetho.getDeclaredMethod("initializeWithDefaults", Context.class);
			initializeWithDefaults.invoke(null, this);
		} catch (ClassNotFoundException ex) {
			LOG.trace("Stetho not available");
		} catch (NoSuchMethodException ex) {
			LOG.warn("Stetho initialization failed", ex);
		} catch (InvocationTargetException ex) {
			LOG.warn("Stetho initialization failed", ex);
		} catch (IllegalAccessException ex) {
			LOG.warn("Stetho initialization failed", ex);
		}
	}

	protected Object createDatabase() {
		return null;
	}

	protected static @NonNull BaseApp getInstance() {
		return s_instance;
	}

	public static @NonNull Context getAppContext() {
		return getInstance();
	}

	public static @NonNull ResourcePreferences prefs() {
		return getInstance().prefs;
	}

	public static void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@UiThread
	public static void toast(CharSequence message) {
		getInstance().doToast(message);
	}
	@UiThread
	protected void doToast(CharSequence message) {
		if (BuildConfigDEBUG) {
			//LOG.info("Debug Toast: {}", message, new StackTrace());
			Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
		}
	}

	@UiThread
	public static void toastUser(CharSequence message) {
		getInstance().doToastUser(message);
	}

	@UiThread
	protected void doToastUser(CharSequence message) {
		//LOG.trace("User Toast: {}", message, new StackTrace());
		Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Set up StrictMode in a way that doesn't interfere much with development,
	 * but tries to tell you any violations available in all possible ways (except death).
	 */
	@TargetApi(VERSION_CODES.M)
	public static void setStrictMode() {
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
