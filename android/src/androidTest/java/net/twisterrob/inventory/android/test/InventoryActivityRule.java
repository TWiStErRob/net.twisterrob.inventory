package net.twisterrob.inventory.android.test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.*;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.CallSuper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;

import net.twisterrob.android.test.espresso.idle.DrawerIdlingResource;
import net.twisterrob.android.test.espresso.idle.GlideIdlingRule;
import net.twisterrob.android.test.junit.*;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;

public class InventoryActivityRule<T extends Activity> extends SensibleActivityTestRule<T> {
	private static final Logger LOG = LoggerFactory.getLogger(InventoryActivityRule.class);

	public InventoryActivityRule(Class<T> activityClass) {
		super(activityClass);
	}
	public InventoryActivityRule(Class<T> activityClass, boolean initialTouchMode) {
		super(activityClass, initialTouchMode);
	}
	public InventoryActivityRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
		super(activityClass, initialTouchMode, launchActivity);
	}

	private boolean clearWelcomeFlag = true;
	public InventoryActivityRule<T> dontClearWelcomeFlag() {
		this.clearWelcomeFlag = false;
		return this;
	}

	// Note: with the base = foo.apply(base) pattern, these will be executed in reverse when evaluate() is called.
	@Override public Statement apply(Statement base, Description description) {
		// Only idle on drawer when there's an activity running inside ActivityTestRule.
		base = DrawerIdlingResource.rule().apply(base, description);
		base = super.apply(base, description);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			// Make sure to check for DatabaseService outside the activity.
			// The activity startup and hence beforeActivityLaunched() should be blocked until this is ready.
			base = new IdlingResourceRule(new DatabaseServiceIdlingResource()).apply(base, description);
		} else {
			LOG.warn("DatabaseServiceIdlingResource is not supported on API {}, database is not synchronized in tests.", Build.VERSION.SDK_INT);
		}
		base = new GlideIdlingRule().apply(base, description);
		base = new InventoryGlideResetRule().apply(base, description);
		base = new ExternalAppKiller().apply(base, description);
		base = new TimeoutRule(20, TimeUnit.SECONDS).apply(base, description);
		return base;
	}

	@Override protected void beforeActivityLaunched() {
		// Wait for registered idling resources before continuing.
		// In particular DatabaseServiceIdlingResource above needs to be waited for before we can delete the database.
		// The database could already be initializing in the background because the App is already created before this.
		Espresso.onIdle();
		reset();
		setDefaults();
		super.beforeActivityLaunched();
	}

	@Override protected void afterActivityFinished() {
		super.afterActivityFinished();
		// CONSIDER is it really necessary?
		//reset();
	}

	public final void reset() {
		resetDB();
		resetPreferences();
		resetFiles();
	}

	/**
	 * Used to set up the database and/or preferences right before launching the activity.
	 * The Database and preferences are reset to an empty state already (to mimic state right after installation).
	 * Use {@link #getActivityIntent()} in this method to add extras to the launch.
	 *
	 * @see #getActivityIntent()
	 */
	// CONSIDER extract overriding methods and reset() to separate rule and wrap the activity rule inside that
	// It is essentially unnecessary to delay resetting this late,
	// see how it makes setting prefs per test awkward: PropertyViewActivityTest_View
	@CallSuper
	protected void setDefaults() {
		if (clearWelcomeFlag) {
			App.prefs().edit().putBoolean(R.string.pref_showWelcome, false).apply();
		}
	}

	protected void resetFiles() {
		Context context = ApplicationProvider.getApplicationContext();
		File intDir = context.getFilesDir();
		LOG.info("Deleting getFilesDir: {}", intDir);
		if (!IOTools.delete(intDir)) {
			throw new IllegalStateException("Cannot delete " + intDir);
		}
		File extDir = context.getExternalFilesDir(null);
		LOG.info("Deleting getExternalFilesDir(null): {}", extDir);
		if (extDir != null) {
			if (!IOTools.delete(extDir)) {
				throw new IllegalStateException("Cannot delete " + extDir);
			}
		}
	}

	protected void resetPreferences() {
		LOG.info("Clearing preferences");
		App.prefs().edit().clear().apply();
	}

	protected void resetDB() {
		Database db = App.db();
		File dbFile = db.getFile();
		LOG.info("Closing and deleting DB {}", dbFile);
		db.getHelper().close();
		if (dbFile.exists() && !dbFile.delete()) {
			throw new IllegalStateException("Cannot delete Database");
		}
	}
}
