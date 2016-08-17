package net.twisterrob.android.inventory;

import java.io.File;
import java.lang.reflect.Field;

import org.slf4j.*;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.bumptech.glide.Glide;

import net.twisterrob.android.test.SensibleActivityTestRule;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Database;

import static net.twisterrob.android.app.BaseApp.*;

public class InventoryActivityTestRule<T extends Activity> extends SensibleActivityTestRule<T> {
	private static final Logger LOG = LoggerFactory.getLogger(InventoryActivityTestRule.class);

	public InventoryActivityTestRule(Class<T> activityClass) {
		super(activityClass);
	}
	public InventoryActivityTestRule(Class<T> activityClass, boolean initialTouchMode) {
		super(activityClass, initialTouchMode);
	}
	public InventoryActivityTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
		super(activityClass, initialTouchMode, launchActivity);
	}

	@Override protected void beforeActivityLaunched() {
		reset();
		super.beforeActivityLaunched();
	}

	@Override protected void afterActivityFinished() {
		super.afterActivityFinished();
		reset();
	}

	public void reset() {
		Context context = InstrumentationRegistry.getTargetContext();
		LOG.info("Resetting Glide");
		try {
			Glide.get(context).clearDiskCache();
			Field glide = Glide.class.getDeclaredField("glide");
			glide.setAccessible(true);
			glide.set(null, null);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		Database db = App.db();
		File dbFile = db.getFile();
		LOG.info("Closing and deleting DB {}", dbFile);
		db.getHelper().close();
		if (dbFile.exists() && !dbFile.delete()) {
			throw new IllegalStateException("Cannot delete Database");
		}

		LOG.info("Clearing preferences");
		prefs().edit().clear().apply();

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
}
