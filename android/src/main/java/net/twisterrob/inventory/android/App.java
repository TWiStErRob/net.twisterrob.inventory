package net.twisterrob.inventory.android;

import java.util.Locale;

import org.slf4j.*;

import android.content.Intent;
import android.content.res.*;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.annotation.NonNull;

import net.twisterrob.android.app.BaseApp;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.db.DatabaseService;

public class App extends BaseApp {
	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	public App() {
		super(BuildConfig.DEBUG, R.xml.preferences);
	}

	public static @NonNull App getInstance() {
		return (App)BaseApp.getInstance();
	}

	public static Database db() {
		return (Database)getInstance().database;
	}

	@Override protected Database createDatabase() {
		return new Database(this);
	}

	@Override public void onStart() {
		super.onStart();
		startService(new Intent(DatabaseService.ACTION_OPEN_DATABASE).setPackage(getPackageName()));
		updateLanguage(Locale.getDefault());
		startService(new Intent(DatabaseService.ACTION_PRELOAD_CATEGORIES).setPackage(getPackageName()));
		startService(new Intent(DatabaseService.ACTION_VACUUM_INCREMENTAL).setPackage(getPackageName()));
	}

	@Override protected void initPreferences() {
		super.initPreferences();
		new PreferencesMigrator(this, prefs()).migrate();
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLanguage(AndroidTools.getLocale(newConfig));
		// FIXME private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();
	}

	private void updateLanguage(@NonNull Locale newLocale) {
		startService(new Intent(DatabaseService.ACTION_UPDATE_LANGUAGE)
				.setPackage(getPackageName())
				.putExtra(DatabaseService.EXTRA_LOCALE, newLocale));
	}

	@SuppressWarnings("WrongThread") // TODEL http://b.android.com/207302
	@Override public void onTerminate() {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			db().getWritableDatabase().close();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
		super.onTerminate();
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
}
