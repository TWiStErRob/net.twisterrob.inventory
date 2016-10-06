package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.*;

import android.support.test.filters.*;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Export.class})
public class BackupActivityTest_ExportExternal {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivityTest_ExportExternal.class);
	@Rule public final ActivityTestRule<BackupActivity> activity = new InventoryActivityRule<>(BackupActivity.class);
	@Rule public final IdlingResourceRule backupService = new BackupServiceInBackupActivityIdlingRule(activity);
	@Rule public final TestName name = new TestName();
	private final BackupActivityActor backup = new BackupActivityActor();

	@Before public void assertBackupActivityIsClean() {
		backup.assertEmptyState();
	}

	@After public void activityIsActive() {
		backup.assertIsInFront();
		backup.assertEmptyState();
	}

	@Category({Op.Cancels.class})
	@Test public void testCancelWarning() throws Exception {
		backup
				.exportExternal()
				.cancel();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@Test public void testCancelChooser() throws Exception {
		backup
				.exportExternal()
				.continueToChooser()
				.cancel();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@Test public void testCancelDrive() throws Exception {
		DriveBackupActor.assumeIsAvailable();
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
		DriveBackupActor.assumeIsAvailable();
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
				getClass().getSimpleName(), name.getMethodName(), Calendar.getInstance());
	}
}
