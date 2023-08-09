package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.*;

import net.twisterrob.android.test.automators.AndroidAutomator;
import net.twisterrob.android.test.automators.UiAutomatorExtensions;
import net.twisterrob.inventory.android.test.ExternalAppKiller;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.UI_AUTOMATOR_VERSION;
import static net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupSendChooserActor;

@RunWith(AndroidJUnit4.class)
@Category({On.Export.class})
public class BackupActivityTest_Send {

	@Rule(order = 0) public final TestRule externalAppKiller = new ExternalAppKiller();

	@SuppressWarnings("deprecation")
	@Rule(order = 1) public final androidx.test.rule.ActivityTestRule<BackupActivity> activity =
			new InventoryActivityRule<>(BackupActivity.class);

	@Rule(order = 2) public final TestRule backupService =
			new BackupServiceInBackupActivityIdlingRule(activity);

	@Rule(order = 3) public final TestRule sanityRule = new TestWatcher() {
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

	@Category({Op.Cancels.class})
	@Test public void testCancelWarning() {
		backup
				.send()
				.cancel();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@OpensExternalApp({
			AndroidAutomator.PACKAGE_CHOOSER,
	})
	@Test public void testCancelChooser() throws Exception {
		BackupSendChooserActor.assumeFunctional();

		backup
				.send()
				.continueToChooser()
				.cancel();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@OpensExternalApp({
			AndroidAutomator.PACKAGE_CHOOSER,
			AndroidAutomator.PACKAGE_PACKAGE_INSTALLER,
			AndroidAutomator.PACKAGE_PERMISSION_CONTROLLER,
			"*"
	})
	// Example: API 28 Google Play device has a "Bluetooth" send target,
	// but even when cancelling the dialog to "Turn on Bluetooth", it opens the stream anyway.
	@Ignore("This is going to be flaky by definition, because it's randomly choosing an app with undefined behavior.")
	@Test public void testCancelApp() throws Exception {
		BackupSendChooserActor.assumeFunctional();

		backup
				.send()
				.continueToChooser()
				.choose(0);

		// Try to exit the chosen app.
		UiAutomatorExtensions.pressBackExternalUnsafe();
	}
}
