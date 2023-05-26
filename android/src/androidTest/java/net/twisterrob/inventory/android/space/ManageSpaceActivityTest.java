package net.twisterrob.inventory.android.space;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.junit.MatcherAssume.assumeThat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.twisterrob.android.utils.tools.PackageManagerTools;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.ManageSpaceActivityActor;
import net.twisterrob.inventory.android.test.categories.On;
import net.twisterrob.inventory.android.test.categories.Op;

@RunWith(AndroidJUnit4.class)
@Category({On.Space.class})
public class ManageSpaceActivityTest {

	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<ManageSpaceActivity> activity =
			new InventoryActivityRule<>(ManageSpaceActivity.class);

	private final ManageSpaceActivityActor space = new ManageSpaceActivityActor();

	@BeforeClass public static void canRun() throws PackageManager.NameNotFoundException {
		Context appContext = ApplicationProvider.getApplicationContext();
		PackageManager pm = appContext.getPackageManager();
		ComponentName component = new ComponentName(appContext, ManageSpaceActivity.class);
		ActivityInfo activityInfo = PackageManagerTools.getActivityInfo(pm, component, 0);
		assumeThat(
				"Different processes cannot be tested (yet?)",
				activityInfo.processName,
				equalTo(appContext.getPackageName())
		);
	}

	@Before public void startup() {
		space.assertDisplayed();
		space.assertNoProgress();
	}

	@Test public void testOpens() {
		// No need to do anything, just opening the activity is enough. Assertions are in @Before.
	}

	@Test public void testClearImageCache() {
		space.clearImageCache().confirm();
		space.assertNoProgress();
	}

	@Category({Op.Cancels.class})
	@Test public void testRebuildSearchIndex() {
		space.rebuildSearchIndex().cancel();
		space.assertNoProgress();
	}
}
