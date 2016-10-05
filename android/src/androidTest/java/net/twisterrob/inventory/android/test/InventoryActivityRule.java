package net.twisterrob.inventory.android.test;

import java.io.File;
import java.util.HashMap;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.*;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.manager.RequestManagerRetriever;

import net.twisterrob.android.test.espresso.idle.*;
import net.twisterrob.android.test.junit.*;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.java.utils.ReflectionTools;

public class InventoryActivityRule<T extends Activity> extends SensibleActivityTestRule<T> {
	private static final Logger LOG = LoggerFactory.getLogger(InventoryActivityRule.class);
	private final GlideIdlingResource glideIdler = new GlideIdlingResource();

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

	@Override public Statement apply(Statement base, Description description) {
		base = DrawerIdlingResource.rule().apply(base, description);
		base = super.apply(base, description);
		base = new IdlingResourceRule(new DatabaseServiceIdlingResource()).apply(base, description);
		return base;
	}

	@Override protected void beforeActivityLaunched() {
		waitForEverythingToDestroy();
		reset();
		setDefaults();
		Espresso.registerIdlingResources(glideIdler);
		super.beforeActivityLaunched();
	}

	@Override protected void afterActivityFinished() {
		waitForEverythingToDestroy();
		super.afterActivityFinished();
		Espresso.unregisterIdlingResources(glideIdler);
		// CONSIDER is it really necessary?
		//reset();
	}

	public final void reset() {
		resetGlide();
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
		Context context = InstrumentationRegistry.getTargetContext();
		File intDir = context.getFilesDir();
		LOG.info("Deleting {}", intDir);
		if (!IOTools.delete(intDir)) {
			throw new IllegalStateException("Cannot delete " + intDir);
		}
		File extDir = context.getExternalFilesDir(null);
		LOG.info("Deleting {}", extDir);
		if (!IOTools.delete(extDir)) {
			throw new IllegalStateException("Cannot delete " + extDir);
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

	protected void resetGlide() {
		LOG.info("Resetting Glide");
		Context context = InstrumentationRegistry.getTargetContext();
		cleanupGlide(context);
		restrictGlide(context);
		forgetGlide(context);
	}
	private void cleanupGlide(final Context context) {
		InstrumentationExtensions.runOnMainIfNecessary(new Runnable() {
			@Override public void run() {
				Glide.with(context).onDestroy();
				Glide.get(context).clearMemory();
			}
		});
		Glide.get(context).clearDiskCache();
	}
	private void forgetGlide(Context context) {
		// make sure Glide.with(...) never returns the old one
		ReflectionTools.set(RequestManagerRetriever.get(), "applicationManager", null);
		// make sure Glide.get(...) never returns the old one
		ReflectionTools.setStatic(Glide.class, "glide", null);
		// recreate internal Glide wrapper
		Constants.Pic.reset(context);
	}
	private void restrictGlide(Context context) {
		Glide glide = Glide.get(context);
		Engine engine = ReflectionTools.get(glide, "engine");
		assert engine != null;
		ReflectionTools.set(engine, "jobs", new HashMap<Object, Object>() {
			@Override public Object put(Object key, Object value) {
				throw new UnsupportedOperationException("This engine is dead.");
			}
			@Override public Object remove(Object key) {
				throw new UnsupportedOperationException("This engine is dead.");
			}
		});
	}
}
