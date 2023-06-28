package net.twisterrob.inventory.android;

import java.util.*;

import javax.inject.Inject;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.*;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.os.Build.*;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.text.TextUtils;

import androidx.annotation.*;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import dagger.hilt.android.HiltAndroidApp;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.adapter.ResourceCursorAdapterWithHolder;
import net.twisterrob.android.app.BaseApp;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.backup.concurrent.BackupNotifications;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.db.DatabaseService;

@HiltAndroidApp
@SuppressLint("Registered") // REPORT False positive, it is there with explicit FQCN.
public class App extends BaseApp implements BaseComponent.Provider {
	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	// TODEL https://github.com/google/dagger/issues/3601
	@Inject @ApplicationContext Context context;

	@SuppressWarnings("deprecation")
	public App() {
		init(BuildConfig.DEBUG, R.xml.preferences);
		ResourceCursorAdapterWithHolder.devMode = BuildConfig.DEBUG;
		LoaderManager.enableDebugLogging(BuildConfig.DEBUG);
		android.app.LoaderManager.enableDebugLogging(BuildConfig.DEBUG);
		FragmentManager.enableDebugLogging(BuildConfig.DEBUG);
		android.app.FragmentManager.enableDebugLogging(BuildConfig.DEBUG);
	}

	public static @NonNull Database db() {
		return BaseApp.getInstance().getDatabase();
	}

	@Override protected Database createDatabase() {
		return new Database(this);
	}

	@Override protected void safeOnCreate() {
		super.safeOnCreate();
		Pic.init(this, BuildConfig.VERSION_NAME);
	}

	@Override public void onStart() {
		super.onStart();
		try {
			// Quick workaround to prevent crashes on startup.
			// Only foreground apps can startService(), at this point it's not foreground yet.
			// https://github.com/TWiStErRob/net.twisterrob.inventory/issues/173
			startServices();
		} catch (RuntimeException ex) {
			LOG.warn("Cannot start services", ex);
		}
	}

	private void startServices() {
		// open a database first, this should lock any other accesses, so it's clearer when the DB open fails
		startService(new Intent(DatabaseService.ACTION_OPEN_DATABASE).setPackage(getPackageName()));
		// run vacuum next, it's quick and most of the time does nothing anyway
		startService(new Intent(DatabaseService.ACTION_VACUUM_INCREMENTAL).setPackage(getPackageName()));
		// this may take a while, but it's necessary for correct display of search results
		updateLocaleDependencies(Locale.getDefault());
		// last, preload categories, this would happen when editing and suggestions kick in
		// so, prevent a delay on first suggestion, load it in the background
		startService(new Intent(DatabaseService.ACTION_PRELOAD_CATEGORIES).setPackage(getPackageName()));
	}

	@Override protected void initPreferences() {
		super.initPreferences();
		new PreferencesMigrator(this, prefs()).migrate();
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLocaleDependencies(AndroidTools.getLocale(newConfig));
		// FIXME private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();
	}

	private void updateLocaleDependencies(@NonNull Locale newLocale) {
		updateLanguage(newLocale);
		updateNotificationChannels();
	}
	private void updateLanguage(@NonNull Locale newLocale) {
		try {
			startService(new Intent(DatabaseService.ACTION_UPDATE_LANGUAGE)
					.setPackage(getPackageName())
					.putExtra(DatabaseService.EXTRA_LOCALE, newLocale));
		} catch (IllegalStateException ex) {
			// Ignore java.lang.IllegalStateException:
			// Not allowed to start service Intent {...}:
			// app is in background uid
			// UidRecord{af72e61 u0a229 CAC  bg:+3m52s273ms idle procs:1 seq(0,0,0)}
			// at com.android.server.am.ActiveServices.startServiceLocked(ContextImpl.java:520)
			// https://android.googlesource.com/platform/frameworks/base/+/android10-release/services/core/java/com/android/server/am/ActiveServices.java#520
			// at android.app.ContextImpl.startServiceCommon(ContextImpl.java:1616)

			// TODO https://github.com/TWiStErRob/net.twisterrob.inventory/issues/166
			// Need to use new JobIntentService to handle this,
			// but that's not available until higher support library or AndroidX.
			// So for now, just ignore the exception. Next startup will do this properly anyway.
		}
	}
	private void updateNotificationChannels() {
		if (VERSION.SDK_INT >= VERSION_CODES.O) {
			BackupNotifications.registerNotificationChannels(this);
		}
	}

	@SuppressWarnings("WrongThread") // TODEL https://issuetracker.google.com/issues/37094658
	@Override public void onTerminate() {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			db().getWritableDatabase().close();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
		super.onTerminate();
	}

	public static CharSequence getError(@NonNull Throwable ex, @StringRes int errorResource, Object... args) {
		return getError(ex, getAppContext().getString(errorResource, args));
	}
	public static CharSequence getError(@NonNull Throwable ex, @NonNull CharSequence message) {
		CharSequence errorMessage = ex.toString();
		Resources res = getAppContext().getResources();
		String msg = ex.getMessage();
		if (ex instanceof SQLiteConstraintException && NAME_ERRORS.contains(msg)) {
			errorMessage = res.getString(R.string.generic_error_unique_name);
		} else if (ex instanceof SQLiteConstraintException && EMPTY_ERRORS.contains(msg)) {
			errorMessage = res.getString(R.string.generic_error_length_name);
		}
		return TextUtils.concat(message, "\n", TextTools.color(Color.LTGRAY, errorMessage));
	}

	// 2013-03-18 (SQLite 3.7.16): Added new extended error codes for all SQLITE_CONSTRAINT errors
	private static final Collection<String> EMPTY_ERRORS = new HashSet<>(Arrays.asList(
			// Galaxy S4 4.4.2 (19) 3.7.11
			"constraint failed (code 19)",
			// Galaxy S5 5.0.1 (21) 3.8.6.1
			// SDK Google APIs x86_64 7.0 (24) 3.9.2
			// SDK Google APIs x86_64 7.1.1 (25) 3.9.2
			"CHECK constraint failed: Property (code 275)", // should be guarded by BaseEditFragment
			"CHECK constraint failed: Room (code 275)", // should be guarded by BaseEditFragment
			"CHECK constraint failed: Item (code 275)", // should be guarded by BaseEditFragment
			"CHECK constraint failed: List (code 275)", // not guarded when list added
			// SDK Google Play x86_64 9.0 (28) 3.22.0
			"CHECK constraint failed: Property (code 275 SQLITE_CONSTRAINT_CHECK)",
			"CHECK constraint failed: Room (code 275 SQLITE_CONSTRAINT_CHECK)",
			"CHECK constraint failed: Item (code 275 SQLITE_CONSTRAINT_CHECK)",
			"CHECK constraint failed: List (code 275 SQLITE_CONSTRAINT_CHECK)",
			// Galaxy S5 6.0.1 (23) 3.8.10.2
			"CHECK constraint failed: List (code 275)\n"
					+ "#################################################################\n"
					+ "Error Code : 275 (SQLITE_CONSTRAINT_CHECK)\n"
					+ "Caused By : Abort due to constraint violation.\n"
					+ "\t(CHECK constraint failed: List (code 275))\n"
					+ "#################################################################"
	));

	// SQLite 3.7.16 released 2013-03-18: Added new extended error codes for all SQLITE_CONSTRAINT errors
	private static final Collection<String> NAME_ERRORS = new HashSet<>(Arrays.asList(
			// Galaxy S2 (10) 2.3.7 3.6.22
			"error code 19: constraint failed",
			// Galaxy S4 (19) 4.4.2 3.7.11
			"column name is not unique (code 19)", // property
			"column name is not unique (code 19)", // list
			"columns property, name are not unique (code 19)", // room
			"columns parent, name are not unique (code 19)", // item
			// Galaxy S5 5.0.1 (21) 3.8.6.1
			// SDK Google APIs x86_64 7.0 (24) 3.9.2
			// SDK Google APIs x86_64 7.1.1 (25) 3.9.2
			"UNIQUE constraint failed: Property.name (code 2067)",
			"UNIQUE constraint failed: Room.property, Room.name (code 2067)",
			"UNIQUE constraint failed: Item.parent, Item.name (code 2067)",
			"UNIQUE constraint failed: List.name (code 2067)",
			// SDK Google Play x86_64 9.0 (28) 3.22.0
			"UNIQUE constraint failed: Property.name (code 2067 SQLITE_CONSTRAINT_UNIQUE)",
			"UNIQUE constraint failed: Room.property, Room.name (code 2067 SQLITE_CONSTRAINT_UNIQUE)",
			"UNIQUE constraint failed: Item.parent, Item.name (code 2067 SQLITE_CONSTRAINT_UNIQUE)",
			"UNIQUE constraint failed: List.name (code 2067 SQLITE_CONSTRAINT_UNIQUE)",
			// Samsung A50 (https://mail.google.com/mail/u/0/#inbox/FMfcgxwHNqFmBnxRghcqFhxxncKgpxfh)
			"UNIQUE constraint failed: Property.name (code 2067 SQLITE_CONSTRAINT_UNIQUE[2067])",
			"UNIQUE constraint failed: Room.property, Room.name (code 2067 SQLITE_CONSTRAINT_UNIQUE[2067])",
			"UNIQUE constraint failed: Item.parent, Item.name (code 2067 SQLITE_CONSTRAINT_UNIQUE[2067])",
			"UNIQUE constraint failed: List.name (code 2067 SQLITE_CONSTRAINT_UNIQUE[2067])",
			// Galaxy S5 6.0.1 (23) 3.8.10.2
			"UNIQUE constraint failed: Property.name (code 2067)\n"
					+ "#################################################################\n"
					+ "Error Code : 2067 (SQLITE_CONSTRAINT_UNIQUE)\n"
					+ "Caused By : Abort due to constraint violation.\n"
					+ "\t(UNIQUE constraint failed: Property.name (code 2067))\n"
					+ "#################################################################",
			"UNIQUE constraint failed: Room.property, Room.name (code 2067)\n"
					+ "#################################################################\n"
					+ "Error Code : 2067 (SQLITE_CONSTRAINT_UNIQUE)\n"
					+ "Caused By : Abort due to constraint violation.\n"
					+ "\t(UNIQUE constraint failed: Room.property, Room.name (code 2067))\n"
					+ "#################################################################",
			"UNIQUE constraint failed: Item.parent, Item.name (code 2067)\n"
					+ "#################################################################\n"
					+ "Error Code : 2067 (SQLITE_CONSTRAINT_UNIQUE)\n"
					+ "Caused By : Abort due to constraint violation.\n"
					+ "\t(UNIQUE constraint failed: Item.parent, Item.name (code 2067))\n"
					+ "#################################################################",
			"UNIQUE constraint failed: List.name (code 2067)\n"
					+ "#################################################################\n"
					+ "Error Code : 2067 (SQLITE_CONSTRAINT_UNIQUE)\n"
					+ "Caused By : Abort due to constraint violation.\n"
					+ "\t(UNIQUE constraint failed: List.name (code 2067))\n"
					+ "#################################################################"
	));

	@Override
	@SuppressWarnings({"deprecation", "RedundantSuppression"}) // Needed to implement it.
	public BaseComponent getBaseComponent() {
		return new BaseComponent() {
			@Override public @NonNull Database db() {
				return App.db();
			}
		};
	}
}
