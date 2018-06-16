package net.twisterrob.android.app;

import java.lang.reflect.*;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.slf4j.*;
import org.slf4j.helpers.*;

import android.annotation.*;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.*;
import android.os.*;
import android.os.StrictMode.*;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.preference.PreferenceManager;
import android.support.annotation.*;
import android.util.Log;
import android.widget.Toast;

import net.twisterrob.android.AndroidConstants;
import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.android.utils.tostring.stringers.AndroidStringerRepo;
import net.twisterrob.java.exceptions.StackTrace;
import net.twisterrob.java.utils.tostring.StringerRepo;

public abstract class BaseApp extends android.app.Application {
	// This is the first Logger created which will result in reading the classpath to create the binding.
	// Make sure the strict mode is set up after this!
	private static final Logger LOG = LoggerFactory.getLogger(BaseApp.class);

	private static BaseApp s_instance;
	private boolean BuildConfigDEBUG;
	/**
	 * android.database.DatabaseTools.dumpCursor(net.twisterrob.inventory.android.
	 * App.db().getReadableDatabase().rawQuery("select * from sqlite_sequence;", null));
	 */
	private Object database;
	private final CountDownLatch databaseWaiter = new CountDownLatch(1);
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
		// StrictModeDiskReadViolation and StrictModeDiskWriteViolation on startup,
		// but there isn't really a good way around these
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			// may cause StrictModeDiskReadViolation if Application.onCreate calls
			// android.graphics.Typeface.SetAppTypeFace (this happened on Galaxy S3 with custom font set up)
			super.onCreate();
			logStartup();
			safeOnCreate();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
		onStart();
	}

	@CallSuper
	public void onStart() {
		// optional override
	}

	@Override public void onTerminate() {
		super.onTerminate();
		s_instance = null;
	}

	@CallSuper
	protected void safeOnCreate() {
		if (BuildConfigDEBUG) {
			AndroidStringerRepo.init(StringerRepo.getInstance(), this);
			initStetho();
		}
		initPreferences();
		database = createDatabase();
		databaseWaiter.countDown();
	}

	protected void initPreferences() {
		if (preferencesResource != AndroidConstants.INVALID_RESOURCE_ID) {
			// may cause StrictModeDiskReadViolation on Android 21-23
			// may cause StrictModeDiskWriteViolation on Android 24-25
			// but necessary for startup since anything can read the preferences
			PreferenceManager.setDefaultValues(this, preferencesResource, false);
		}
		prefs = new ResourcePreferences(getResources(), PreferenceManager.getDefaultSharedPreferences(this));
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

	// TODEL either remove it or use it, depends on if the AndroidJUnitRunner::notPackage solves this issue
	private void delayIfTested() {
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			// a lot of the times gradlew connectedCheck simply crashes with a SIGSEGV on 2.3.7 emulator
			// to make this less frequent a little sleep may help before continuing
			// the crash only happens if startService is called from onStart, otherwise all is ok
			// of course since this is only for tests we need to check for that
			try {
				Class.forName("android.support.test.BuildConfig");
				LOG.warn("android.support.test is on classpath, assuming test mode!");
				Thread.sleep(0);
			} catch (ClassNotFoundException ignore) {
				// we're not testing
			} catch (InterruptedException ignore) {
				Thread.interrupted();
			}
		}
	}

	protected Object createDatabase() {
		return null;
	}

	/**
	 * This a temporary workaround for initializing the Database from a {@link android.content.ContentProvider}
	 * before the {@link android.app.Application} has finished initializing.
	 * The proper solution would be to use a Database class that can exist without actually having connected
	 * to the backing database yet, so it can be created on the UI thread and as early as possible.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDatabase() {
		try {
			if (database == null) {
				LOG.warn("Waiting for database to initialize"
						+ " (likely a Content Provider query before App is initialized)", new StackTrace());
			}
			// null database may be correct behavior, so always wait
			databaseWaiter.await();
		} catch (InterruptedException ex) {
			Thread.interrupted();
			throw new IllegalStateException("Database creation interrupted", ex);
		}
		return (T)database;
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

	@AnyThread
	public static void toast(CharSequence message) {
		getInstance().doToast(message);
	}
	@AnyThread
	protected void doToast(final CharSequence message) {
		if (BuildConfigDEBUG) {
			//LOG.info("Debug Toast: {}", message, new StackTrace());
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG).show();
				}
			});
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
	@SuppressLint("ObsoleteSdkInt")
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
