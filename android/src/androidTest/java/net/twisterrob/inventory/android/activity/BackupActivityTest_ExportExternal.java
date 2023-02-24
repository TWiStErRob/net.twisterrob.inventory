package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.*;
import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.*;
import androidx.test.uiautomator.UiObjectNotFoundException;

import net.twisterrob.android.test.automators.UiAutomatorExtensions;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Export.class})
public class BackupActivityTest_ExportExternal {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivityTest_ExportExternal.class);

	@Rule(order = 1) public final TestName testName = new TestName();
	@SuppressWarnings("deprecation")
	@Rule(order = 2) public final androidx.test.rule.ActivityTestRule<BackupActivity> activity =
			new InventoryActivityRule<>(BackupActivity.class);
	@Rule(order = 3) public final TestRule backupService = new BackupServiceInBackupActivityIdlingRule(activity);
	@Rule(order = 4) public final TestRule sanityRule = new TestWatcher() {
		@Override protected void succeeded(Description description) {
			// @After: if it succeeded let's check a few more things, but only if not failed
			backup.assertIsInFront();
			backup.assertEmptyState();
		}
		@Override protected void failed(Throwable e, Description description) {
			// try to exit any external activities that may block the other tests from running
			UiAutomatorExtensions.pressBackExternal();
		}
	};

	private final BackupActivityActor backup = new BackupActivityActor();

	@Before public void assertBackupActivityIsClean() throws UiObjectNotFoundException {
		backup.allowPermissions();
		backup.assertEmptyState();
	}

	@Category({Op.Cancels.class})
	@Test public void testCancelWarning() {
		backup
				.exportExternal()
				.cancel();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@Test public void testCancelChooser() throws Exception {
		assumeThat(new Intent(Intent.ACTION_SEND).setType(InventoryContract.Export.TYPE_BACKUP),
				canBeResolved(hasSize(greaterThanOrEqualTo(2))));
		backup
				.exportExternal()
				.continueToChooser()
				.cancel();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@Test public void testCancelDrive() throws Exception {
		DriveBackupActor.assumeDriveFunctional();
		backup
				.exportExternal()
				.continueToChooser()
				.chooseDrive()
				.cancel();
	}

	@FlakyTest(detail = "Sometimes it doesn't find clickOnLabel(selectFolder()): UiObjectNotFoundException: UiSelector[TEXT=Select folder]")
	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({UseCase.Complex.class, On.External.class})
	@Test public void testSuccessfulFullExport() throws Exception {
		DriveBackupActor.assumeDriveFunctional();
		ExportExternalActor exportActor = backup.exportExternal();
		DriveBackupActor drive = exportActor
				.continueToChooser()
				.chooseDrive();
		backup.assertIsInBackground(activity.getActivity());
		String fileName = drive.getSaveFileName();
		String folder = generateFolderName();
		LOG.info("Saving {}/{}", folder, fileName);
		drive.saveToAndroidTests(folder);
		backup.assertIsInBackground(activity.getActivity());
		drive.save();
		backup.assertIsInFront();
		exportActor.assertFinished().dismiss();
	}

	private String generateFolderName() {
		return String.format(Locale.ROOT, "%s.%s@%tFT%<tH-%<tM-%<tS",
				getClass().getSimpleName(), testName.getMethodName(), Calendar.getInstance());
	}
}
