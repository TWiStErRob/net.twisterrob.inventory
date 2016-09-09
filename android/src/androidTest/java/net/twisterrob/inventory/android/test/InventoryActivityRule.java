package net.twisterrob.inventory.android.test;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.*;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.test.InstrumentationRegistry;

import com.bumptech.glide.Glide;

import net.twisterrob.android.test.espresso.idle.*;
import net.twisterrob.android.test.junit.*;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.db.DatabaseService;

import static net.twisterrob.android.app.BaseApp.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

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

	@Override public Statement apply(Statement base, Description description) {
		base = super.apply(base, description);
		base = DrawerIdlingResource.rule().apply(base, description);
		base = new IdlingResourceRule(new IntentServiceIdlingResource(DatabaseService.class)).apply(base, description);
		return base;
	}

	private void waitForIdleSync() {
		try {
			runOnUiThread(new Runnable() {
				@Override public void run() {
					getUIControllerHack().loopMainThreadUntilIdle();
				}
			});
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	@Override protected void beforeActivityLaunched() {
		waitForIdleSync();
		reset();
		setDefaults();
		super.beforeActivityLaunched();
	}

	@Override protected void afterActivityFinished() {
		super.afterActivityFinished();
		// TODO is it really necessary?
		//waitForIdleSync();
		//reset();
	}

	public final void reset() {
		resetGlide();
		resetDB();
		resetPreferences();
		resetFiles();
	}

	@CallSuper
	protected void setDefaults() {
		if (clearWelcomeFlag) {
			prefs().edit().putBoolean(R.string.pref_showWelcome, false).apply();
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
		prefs().edit().clear().apply();
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
		try {
			Glide.get(InstrumentationRegistry.getTargetContext()).clearDiskCache();
			Field glide = Glide.class.getDeclaredField("glide");
			glide.setAccessible(true);
			glide.set(null, null);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}
}
