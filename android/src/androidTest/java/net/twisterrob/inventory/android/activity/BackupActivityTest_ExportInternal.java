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

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.ExportInternalResultActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.java.utils.CollectionTools.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Export.class})
public class BackupActivityTest_ExportInternal {
	private final ActivityTestRule<BackupActivity> activity = new InventoryActivityRule<>(BackupActivity.class);
	private final TemporaryFolder tempInHomeFolder = new TemporaryFolder(Paths.getPhoneHome());
	private final CheckExportedFiles files = new CheckExportedFiles();
	private final IdlingResourceRule backupService = new BackupServiceInBackupActivityIdlingRule(activity);
	private final BackupActivityActor backup = new BackupActivityActor();

	@Rule public final RuleChain rules = RuleChain
			.outerRule(activity)
			.around(backupService)
			.around(tempInHomeFolder)
			.around(files);

	@Before public void assertBackupActivityIsClean() {
		backup.assertEmptyState();
	}

	@Test public void testBackupCompletes() throws Exception {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.assertDisplayed();

		dialog.dismiss();

		// check progress also disappeared TODO check it was ever displayed
		backup.assertNoProgressDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testBackupDialogStaysWhenRotated() throws Exception {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.assertDisplayed();

		backup.rotate();

		dialog.assertDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testBackupDialogDoesNotReappearWhenRotated() throws Exception {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.dismiss();

		backup.rotate();

		dialog.assertNotDisplayed();
	}

	@Category({Op.Rotates.class})
	@Test public void testBackupProgressDoesNotAppearWhenRotated() throws Exception {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.dismiss();

		backup.rotate();

		backup.assertNoProgressDisplayed();
	}

	@Test public void testBackupShownInFileList() throws Exception {
		ExportInternalResultActor dialog = backup.exportInternal();
		dialog.dismiss();

		String date = String.format(Locale.ROOT, "%tF", Calendar.getInstance());
		File justExported = only(files.getCreatedFiles());
		assertThat(justExported, aFileNamed(containsStringIgnoringCase(date))); // validates if file exists

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
					saveFolderContents();
					navigateToFolder();
					base.evaluate();
					// only if didn't throw
					checkFolderContents();
				}

				@Before public void saveFolderContents() throws Exception {
					File extraFile = tempInHomeFolder.newFile(); // to check for devastating deletion
					preContents = tempInHomeFolder.getRoot().listFiles();
					assertThat(preContents, arrayContaining(extraFile));
				}

				@Before private void navigateToFolder() {
					backup.gotoHomeFolder();
					backup.selectFolder(tempInHomeFolder.getRoot());
				}

				@After public void checkFolderContents() throws Exception {
					assertThat(getDeletedFiles(), is(empty()));
					assertThat(getCreatedFiles(), hasSize(expectedSize));
				}
			};
		}
	}
}

