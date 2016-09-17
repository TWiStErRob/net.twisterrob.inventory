package net.twisterrob.inventory.android.content.db;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.VariantIntentService;
import net.twisterrob.inventory.android.content.model.CategoryDTO;

public class DatabaseService extends VariantIntentService {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseService.class);
	private static final long SLEEP_BETWEEN_VACUUMS = TimeUnit.SECONDS.toMillis(10);
	public static final String ACTION_OPEN_DATABASE = "net.twisterrob.inventory.action.OPEN_DATABASE";
	public static final String ACTION_VACUUM_INCREMENTAL = "net.twisterrob.inventory.action.VACUUM_INCREMENTAL";
	public static final String ACTION_PRELOAD_CATEGORIES = "net.twisterrob.inventory.action.PRELOAD_CATEGORIES";
	public static final String ACTION_UPDATE_LANGUAGE = "net.twisterrob.inventory.action.UPDATE_LANGUAGE";
	public static final String EXTRA_LOCALE = "net.twisterrob.inventory.extra.update_language_locale";
	private static final int CODE_INCREMENTAL_VACUUM = 16336;

	public DatabaseService() {
		super("DB");
	}

	@Override protected void onHandleIntent(Intent intent) {
		super.onHandleIntent(intent);
		String action = String.valueOf(intent.getAction()); // null becomes "null", so we can switch on it
		switch (action) {
			case ACTION_OPEN_DATABASE:
				SQLiteDatabase db = App.db().getWritableDatabase();
				LOG.trace("Database opened: {}", DatabaseTools.dbToString(db));
				break;
			case ACTION_UPDATE_LANGUAGE:
				updateLanguage(intent);
				break;
			case ACTION_PRELOAD_CATEGORIES:
				preloadCategoryCache();
				break;
			case ACTION_VACUUM_INCREMENTAL:
				incrementalVacuum();
				break;
			default:
				throw new UnsupportedOperationException("Action " + action + " is not implemented for " + intent);
		}
	}

	private void updateLanguage(Intent intent) {
		Locale locale = (Locale)intent.getSerializableExtra(EXTRA_LOCALE);
		if (locale == null) {
			LOG.warn("Missing locale from {}", intent);
			locale = Locale.getDefault();
		}
		new LanguageUpdater(App.prefs(), App.db()).updateLanguage(locale);
	}

	private void preloadCategoryCache() {
		try {
			CategoryDTO.getCache(getApplicationContext());
		} catch (Exception ex) {
			// if fails we'll crash later when used, but at least let the app start up
			LOG.error("Failed to initialize Category cache", ex);
		}
	}

	private void incrementalVacuum() {
		try {
			if (Boolean.TRUE.equals(new IncrementalVacuumer(App.db().getWritableDatabase()).call())) {
				scheduleNextIncrementalVacuum();
			}
		} catch (Exception ex) {
			if (!(ex instanceof RuntimeException)) {
				throw new IllegalStateException("Incremental vacuum failed.", ex);
			} else {
				throw (RuntimeException)ex;
			}
		}
	}

	private void scheduleNextIncrementalVacuum() {
		long next = System.currentTimeMillis() + SLEEP_BETWEEN_VACUUMS;
		LOG.trace("Scheduling another incremental vacuuming at {}", String.format(Locale.ROOT, "%tFT%<tT", next));
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = createVacuumIntent(this);
		am.set(AlarmManager.RTC, next, pendingIntent);
	}
	private static PendingIntent createVacuumIntent(Context context) {
		Intent intent = new Intent(ACTION_VACUUM_INCREMENTAL).setPackage(context.getPackageName());
		return PendingIntent.getService(context, CODE_INCREMENTAL_VACUUM, intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
	}

	public static void clearVacuumAlarm(Context context) {
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = createVacuumIntent(context);
		pendingIntent.cancel();
		am.cancel(pendingIntent);
	}
}
