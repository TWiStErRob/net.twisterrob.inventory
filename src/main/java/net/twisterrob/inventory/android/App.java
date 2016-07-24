package net.twisterrob.inventory.android;

import java.util.Locale;

import org.slf4j.*;
import org.slf4j.impl.AndroidLoggerFactory;

import android.content.res.*;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.annotation.*;
import android.text.TextUtils;

import net.twisterrob.android.app.BaseApp;
import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.android.utils.concurrent.BackgroundExecution;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.java.utils.StringTools;

public class App extends BaseApp {
	static {
		AndroidLoggerFactory.addReplacement("^net\\.twisterrob\\.inventory\\.android\\.(.+\\.)?", "");
	}

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

	@Override protected void safeOnCreate() {
		super.safeOnCreate();
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
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateLanguage(AndroidTools.getLocale(newConfig));
		// FIXME private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();
	}

	private void updateLanguage(@NonNull Locale newLocale) {
		final ResourcePreferences prefs = prefs();
		final String storedLanguage = prefs.getString(R.string.pref_currentLanguage, null);
		final String currentLanguage = newLocale.toString();
		if (!currentLanguage.equals(storedLanguage)) {
			String from = StringTools.toLocale(storedLanguage).getDisplayName();
			String to = StringTools.toLocale(currentLanguage).getDisplayName();
			String message = getAppContext().getString(R.string.message_locale_changed, from, to);
			LOG.debug(message);
			if (BuildConfig.DEBUG && !TextUtils.isEmpty(from)) {
				App.toast(message);
			}
			new BackgroundExecution(new Runnable() {
				@WorkerThread
				@Override public void run() {
					try {
						db().updateCategoryCache(App.getAppContext());
						LOG.debug("Locale update successful: {} -> {}", storedLanguage, currentLanguage);
						prefs.setString(R.string.pref_currentLanguage, currentLanguage);
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
