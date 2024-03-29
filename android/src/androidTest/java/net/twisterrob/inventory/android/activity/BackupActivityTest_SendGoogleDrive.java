package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.*;
import org.slf4j.*;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.*;

import net.twisterrob.android.test.automators.AndroidAutomator;
import net.twisterrob.android.test.automators.GoogleDriveAutomator;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.UI_AUTOMATOR_VERSION;

@RunWith(AndroidJUnit4.class)
@Category({On.Export.class})
public class BackupActivityTest_SendGoogleDrive {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivityTest_SendGoogleDrive.class);

	@Rule(order = 1) public final TestName testName = new TestName();

	@SuppressWarnings("deprecation")
	@Rule(order = 2) public final androidx.test.rule.ActivityTestRule<BackupActivity> activity =
			new InventoryActivityRule<>(BackupActivity.class);

	@Rule(order = 3) public final TestRule backupService =
			new BackupServiceInBackupActivityIdlingRule(activity);

	@Rule(order = 4) public final TestRule sanityRule = new TestWatcher() {
		@Override protected void succeeded(Description description) {
			// @After: if it succeeded let's check a few more things, but only if not failed
			backup.assertIsInFront();
			backup.assertEmptyState();
		}
	};

	private final BackupActivityActor backup = new BackupActivityActor();

	@Before public void assertBackupActivityIsClean() {
		backup.assertEmptyState();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@OpensExternalApp({
			AndroidAutomator.PACKAGE_CHOOSER,
			GoogleDriveAutomator.PACKAGE_GOOGLE_DRIVE,
			GoogleDriveAutomator.PACKAGE_GOOGLE_SIGN_IN,
			AndroidAutomator.PACKAGE_PACKAGE_INSTALLER,
			AndroidAutomator.PACKAGE_PERMISSION_CONTROLLER
	})
	@Test public void testCancelDrive() throws Exception {
		DriveBackupActor.assumeDriveFunctional();
		backup
				.send()
				.continueToChooser()
				.chooseDrive()
				.cancel();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({UseCase.Complex.class, On.External.class})
	@OpensExternalApp({
			AndroidAutomator.PACKAGE_CHOOSER,
			GoogleDriveAutomator.PACKAGE_GOOGLE_DRIVE,
			GoogleDriveAutomator.PACKAGE_GOOGLE_SIGN_IN
	})
	@Test public void testSuccessfulFullExport() throws Exception {
		DriveBackupActor.assumeDriveFunctional();
		BackupSendActor exportActor = backup.send();
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

	private @NonNull String generateFolderName() {
		return String.format(Locale.ROOT, "%s.%s@%tFT%<tH-%<tM-%<tS",
				getClass().getSimpleName(), testName.getMethodName(), Calendar.getInstance());
	}
}
