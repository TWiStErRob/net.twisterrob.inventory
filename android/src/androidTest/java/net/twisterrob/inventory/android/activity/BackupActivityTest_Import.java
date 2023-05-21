package net.twisterrob.inventory.android.activity;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;

import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.activity.ScopedStorageSaver;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.categories.On;
import net.twisterrob.inventory.android.test.categories.Op;
import net.twisterrob.inventory.android.test.categories.UseCase;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.UI_AUTOMATOR_VERSION;
import static net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupImportActor;
import static net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupImportActor.*;
import static net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupImportPickerActor;
import static net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupImportStubActor;

@RunWith(AndroidJUnit4.class)
@Category({On.Import.class})
public class BackupActivityTest_Import {

	@SuppressWarnings("deprecation")
	@Rule(order = 1) public final androidx.test.rule.ActivityTestRule<BackupActivity> activity =
			new InventoryActivityRule<>(BackupActivity.class);

	@Rule(order = 2) public final TestRule backupService =
			new BackupServiceInBackupActivityIdlingRule(activity);

	@Rule(order = 3) public final TemporaryFolder temp = new TemporaryFolder();

	private final BackupActivityActor backup = new BackupActivityActor();

	@Before public void assertBackupActivityIsClean() {
		backup.assertEmptyState();
	}

	@Category({Op.Cancels.class})
	@Test public void testImportCancelWarning() {
		BackupImportStubActor.mockImportFromAnyFile();
		BackupImportActor importActor = backup.importBackup();

		importActor.cancel();

		BackupImportStubActor.verifyNoImport();
		backup.assertIsInFront();
		backup.assertEmptyState();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class, On.External.class})
	@Test public void testImportCancelPicker() throws Exception {
		BackupImportActor importActor = backup.importBackup();

		importActor.continueToPicker().cancel();

		backup.assertIsInFront();
		backup.assertEmptyState();
	}

	@Category({Op.CreatesBelonging.class})
	@Test public void testImportStubbed() throws Exception {
		BackupImportStubActor.mockImportFromFile(temp.newFile());
		BackupImportActor importActor = backup.importBackup();

		BackupImportResultActor resultActor = importActor
				.continueToPickerStubbed()
		        .verifyMockImport();// TODO why a failure here crashes all tests?

		resultActor.dismiss();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({UseCase.Complex.class, On.External.class})
	@Test public void testImportReal() throws Exception {
		File file = temp.newFile();
		BackupImportPickerActor.prepareImportableFile(file);
		String name = ScopedStorageSaver.createZipInDownloads(file);

		BackupImportActor importActor = backup.importBackup();
		BackupImportPickerActor pickerActor = importActor.continueToPicker();
		pickerActor.selectInDrawer("Downloads");
		BackupImportResultActor resultActor = pickerActor.selectFile(name);

		resultActor.dismiss();
	}
}
