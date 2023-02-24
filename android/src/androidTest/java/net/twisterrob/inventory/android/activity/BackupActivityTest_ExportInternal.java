package net.twisterrob.inventory.android.activity;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.model.Statement;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.*;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.ExportInternalResultActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.java.utils.CollectionTools.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Export.class})
public class BackupActivityTest_ExportInternal {

	@SuppressWarnings("deprecation")
	@Rule(order = 1) public final androidx.test.rule.ActivityTestRule<BackupActivity> activity =
			new InventoryActivityRule<>(BackupActivity.class);

	@Rule(order = 2) public final TestRule backupService =
			new BackupServiceInBackupActivityIdlingRule(activity);

	@Rule(order = 3) public final TemporaryFolder tempInHomeFolder = TemporaryFolder
			.builder()
			//.parentFolder(Paths.getPhoneHome()) // cannot work because it needs permission
			.parentFolder(InstrumentationRegistry.getInstrumentation().getContext().getDir("temp", Context.MODE_PRIVATE))
			.assureDeletion()
			.build();

	@Rule(order = 4) public final CheckExportedFiles files = new CheckExportedFiles();

	private final BackupActivityActor backup = new BackupActivityActor();

	@Before public void assertBackupActivityIsClean() {
		backup.assertEmptyState();
	}

	@Test public void testBackupCompletes() {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.assertDisplayed();

		dialog.dismiss();

		// check progress also disappeared TODO check it was ever displayed
		backup.assertNoProgressDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testBackupDialogStaysWhenRotated() {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.assertDisplayed();

		backup.rotate();

		dialog.assertDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testBackupDialogDoesNotReappearWhenRotated() {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.dismiss();

		backup.rotate();

		dialog.assertNotDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testBackupProgressDoesNotAppearWhenRotated() {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.dismiss();

		backup.rotate();

		backup.assertNoProgressDisplayed();
	}

	@Test public void testBackupShownInFileList() {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.dismiss();

		String date = String.format(Locale.ROOT, "%tF", Calendar.getInstance());
		File justExported = only(files.getCreatedFiles());
		assertThat("double-check the file exists",
				justExported, aFileNamed(containsStringIgnoringCase(date)));

		backup.checkFileShown(date);
	}

	// TODO move to BackupActivityActor?
	private class CheckExportedFiles implements TestRule {
		private int expectedSize;
		private File[] preContents;

		public CheckExportedFiles() {
			setExpectedSize(1);
		}

		public void setExpectedSize(int expectedSize) {
			this.expectedSize = expectedSize;
		}

		public Collection<File> getCreatedFiles() {
			Set<File> files = new HashSet<>(Arrays.asList(tempInHomeFolder.getRoot().listFiles()));
			files.removeAll(Arrays.asList(preContents));
			return files;
		}
		public Collection<File> getDeletedFiles() {
			Set<File> files = new HashSet<>(Arrays.asList(preContents));
			files.removeAll(Arrays.asList(tempInHomeFolder.getRoot().listFiles()));
			return files;
		}

		@Override public Statement apply(final Statement base, Description description) {
			return new Statement() {
				@Override public void evaluate() throws Throwable {
					backup.allowPermissions();
					saveFolderContents();
					navigateToFolder();
					base.evaluate();
					// only if didn't throw
					checkFolderContents();
				}

				/*@Before*/
				public void saveFolderContents() throws Exception {
					// To check for devastating deletion,
					// create an empty file that should persist till the end.
					File extraFile = tempInHomeFolder.newFile();
					preContents = tempInHomeFolder.getRoot().listFiles();
					assertThat("self-check", preContents, arrayContaining(extraFile));
				}

				/*@Before*/
				private void navigateToFolder() {
					backup.gotoFolder(tempInHomeFolder.getRoot());
				}

				/*@After*/
				public void checkFolderContents() {
					assertThat(getDeletedFiles(), is(empty()));
					assertThat(getCreatedFiles(), hasSize(expectedSize));
				}
			};
		}
	}
}
