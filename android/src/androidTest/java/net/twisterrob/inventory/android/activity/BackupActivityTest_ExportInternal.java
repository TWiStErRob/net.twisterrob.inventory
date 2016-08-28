package net.twisterrob.inventory.android.activity;

import java.io.File;
import java.util.*;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.model.Statement;

import static org.hamcrest.Matchers.*;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.contrib.RecyclerViewActions.*;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.InventoryActivityRule;

import static net.twisterrob.android.test.DialogMatchers.*;
import static net.twisterrob.android.test.EspressoExtensions.*;
import static net.twisterrob.java.utils.CollectionTools.*;

@RunWith(AndroidJUnit4.class)
public class BackupActivityTest_ExportInternal {
	private final ActivityTestRule<BackupActivity> activity = new InventoryActivityRule<>(BackupActivity.class);
	private final TemporaryFolder temp = new TemporaryFolder(Paths.getPhoneHome());
	private final CheckExportedFiles files = new CheckExportedFiles();

	@Rule public final RuleChain rules = RuleChain
			.outerRule(activity)
			.around(temp)
			.around(files);

	@Test public void testBackupCompletes() throws Exception {
		assertNoDialogIsDisplayed();
		// progress is not displayed
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));

		clickExport();

		assertDialogIsDisplayed();
		clickNeutralInDialog();

		// check progress also disappeared TODO check it was ever displayed
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}

	@Test public void testBackupDialogStaysWhenRotated() throws Exception {
		clickExport();

		rotateDevice(activity);

		assertDialogIsDisplayed();
	}

	@Test public void testBackupDialogDoesNotReappearWhenRotated() throws Exception {
		clickExport();
		clickNeutralInDialog();

		rotateDevice(activity);

		assertNoDialogIsDisplayed();
	}

	@Test public void testBackupProgressDoesNotAppearWhenRotated() throws Exception {
		clickExport();
		clickNeutralInDialog();

		rotateDevice(activity);

		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}

	@Test public void testBackupShownInFileList() throws Exception {
		clickExport();
		clickNeutralInDialog();

		String date = String.format(Locale.ROOT, "%tF", Calendar.getInstance());
		String justExported = only(files.getCreatedFiles());
		assertThat(justExported, containsStringIgnoringCase(date)); // validates if file exists

		Matcher<View> justExportedLabel = withText(containsString(date));
		onView(withId(R.id.backups)).perform(scrollTo(hasDescendant(justExportedLabel)));
		onView(justExportedLabel).check(matches(isDisplayed())); // validates if shown
	}

	private void clickExport() {
		onView(withId(R.id.fab)).perform(click());
	}

	private class CheckExportedFiles implements TestRule {
		private int expectedSize = 1;
		private String[] preContents;

		public void setExpectedSize(int expectedSize) {
			this.expectedSize = expectedSize;
		}

		public Collection<String> getCreatedFiles() {
			Set<String> files = new HashSet<>(Arrays.asList(temp.getRoot().list()));
			files.removeAll(Arrays.asList(preContents));
			return files;
		}
		public Collection<String> getDeletedFiles() {
			Set<String> files = new HashSet<>(Arrays.asList(preContents));
			files.removeAll(Arrays.asList(temp.getRoot().list()));
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
					File extraFile = temp.newFile(); // to check for devastating deletion
					preContents = temp.getRoot().list();
					assertThat(preContents, arrayContaining(extraFile.getName()));
				}

				@Before private void navigateToFolder() {
					onView(withId(R.id.action_export_home)).perform(click());
					Matcher<View> tempFolder = hasDescendant(withText(temp.getRoot().getName()));
					onView(withId(R.id.backups)).perform(actionOnItem(tempFolder, click()));
				}

				@After public void checkFolderContents() throws Exception {
					assertThat(getDeletedFiles(), is(empty()));
					assertThat(getCreatedFiles(), hasSize(expectedSize));
				}
			};
		}
	}
}

