package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.*;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.twisterrob.android.test.automators.UiAutomatorExtensions;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupExportPickerActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupExportPickerActor.BackupExportResultActor;
import net.twisterrob.inventory.android.test.categories.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Export.class})
public class BackupActivityTest_Export {

	@SuppressWarnings("deprecation")
	@Rule(order = 1) public final androidx.test.rule.ActivityTestRule<BackupActivity> activity =
			new InventoryActivityRule<>(BackupActivity.class);

	@Rule(order = 2) public final TestRule backupService =
			new BackupServiceInBackupActivityIdlingRule(activity);

	@Rule(order = 3) public final TemporaryFolder temp = TemporaryFolder
			.builder()
			.parentFolder(InstrumentationRegistry.getInstrumentation().getContext().getDir("temp", Context.MODE_PRIVATE))
			.assureDeletion()
			.build();

//	@Rule(order = 4) public final CheckExportedFiles files = new CheckExportedFiles();

	private final BackupActivityActor backup = new BackupActivityActor();

	@Before public void assertBackupActivityIsClean() {
		backup.assertEmptyState();
	}

	@Test public void testExportCompletes() throws Exception {
		BackupExportPickerActor pickerActor = backup.exportBackup();
		pickerActor.selectInDrawer("Downloads");

		BackupExportResultActor resultActor = pickerActor.save();

		resultActor.dismiss();
		// check progress also disappeared TODO check it was ever displayed
		backup.assertNoProgressDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testExportCompletesWhenPickerRotated() throws Exception {
		BackupExportPickerActor pickerActor = backup.exportBackup();
		pickerActor.selectInDrawer("Downloads");

		try {
			UiAutomatorExtensions.rotateDevice();
			// On API 26 Google APIs emulator the IME comes back.
			UiAutomatorExtensions.ensureNoSoftKeyboard();
			pickerActor.assertDisplayed();
			pickerActor.save().dismiss();
		} finally {
			UiAutomatorExtensions.stopRotateDevice();
		}
	}

	@Category({Op.Rotates.class})
	@Test public void testExportPickerDoesNotReappearWhenRotated() throws Exception {
		BackupExportPickerActor pickerActor = backup.exportBackup();
		pickerActor.cancel();

		backup.rotate();

		pickerActor.assertNotDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testExportProgressDoesNotAppearWhenRotated()throws Exception  {
		BackupExportPickerActor pickerActor = backup.exportBackup();
		pickerActor.cancel();

		backup.rotate();

		backup.assertNoProgressDisplayed();
	}

//	@Test public void testBackupShownInFileList()throws Exception  {
//		BackupExportActor pickerActor = backup.exportBackup();
//
//		String date = String.format(Locale.ROOT, "%tF", Calendar.getInstance());
//		File justExported = only(files.getCreatedFiles());
//		assertThat("double-check the file exists",
//				justExported, aFileNamed(containsStringIgnoringCase(date)));
//
//		backup.checkFileShown(date);
//
//		pickerActor.cancel();
//	}

//	// TODO move to BackupActivityActor?
//	private class CheckExportedFiles implements TestRule {
//		private int expectedSize;
//		private File[] preContents;
//
//		public CheckExportedFiles() {
//			setExpectedSize(1);
//		}
//
//		public void setExpectedSize(int expectedSize) {
//			this.expectedSize = expectedSize;
//		}
//
//		public Collection<File> getCreatedFiles() {
//			Set<File> files = new HashSet<>(Arrays.asList(temp.getRoot().listFiles()));
//			files.removeAll(Arrays.asList(preContents));
//			return files;
//		}
//		public Collection<File> getDeletedFiles() {
//			Set<File> files = new HashSet<>(Arrays.asList(preContents));
//			files.removeAll(Arrays.asList(temp.getRoot().listFiles()));
//			return files;
//		}
//
//		@Override public Statement apply(final Statement base, Description description) {
//			return new Statement() {
//				@Override public void evaluate() throws Throwable {
//					backup.allowPermissions();
//					saveFolderContents();
//					navigateToFolder();
//					base.evaluate();
//					// only if didn't throw
//					checkFolderContents();
//				}
//
//				/*@Before*/
//				public void saveFolderContents() throws Exception {
//					// To check for devastating deletion,
//					// create an empty file that should persist till the end.
//					File extraFile = temp.newFile();
//					preContents = temp.getRoot().listFiles();
//					assertThat("self-check", preContents, arrayContaining(extraFile));
//				}
//
//				/*@Before*/
//				private void navigateToFolder() {
//					backup.gotoFolder(temp.getRoot());
//				}
//
//				/*@After*/
//				public void checkFolderContents() {
//					assertThat(getDeletedFiles(), is(empty()));
//					assertThat(getCreatedFiles(), hasSize(expectedSize));
//				}
//			};
//		}
//	}
}
