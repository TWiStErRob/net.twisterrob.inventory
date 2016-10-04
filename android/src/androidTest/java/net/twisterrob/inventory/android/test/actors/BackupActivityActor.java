package net.twisterrob.inventory.android.test.actors;

import java.io.*;
import java.util.Collections;
import java.util.zip.*;

import org.hamcrest.Matcher;
import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.view.View;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.contrib.RecyclerViewActions.*;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.RootMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.automators.GoogleDriveAutomator;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BackupActivity;

import static net.twisterrob.android.test.automators.AndroidAutomator.*;
import static net.twisterrob.android.test.automators.GoogleDriveAutomator.*;
import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;
import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class BackupActivityActor extends ActivityActor {
	public BackupActivityActor() {
		super(BackupActivity.class);
	}

	public void openedViaIntent() {
		intended(allOf(isInternal(), hasComponent(BackupActivity.class.getName())));
	}
	public ExportInternalActor exportInternal() {
		onView(withId(R.id.fab)).perform(click());
		return new ExportInternalActor();
	}
	public ExportExternalActor exportExternal() {
		onActionMenuView(withText(R.string.backup_export_external)).perform(click());
		ExportExternalActor dialog = new ExportExternalActor();
		dialog.assertDialogDisplayed();
		return dialog;
	}
	public void assertNoProgressDisplayed() {
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}
	public void checkFileShown(String date) {
		Matcher<View> justExportedLabel = withText(containsString(date));
		onView(withId(R.id.backups)).perform(scrollTo(hasDescendant(justExportedLabel)));
		onView(justExportedLabel).check(matches(isDisplayed())); // validates if shown
	}
	public void gotoHomeFolder() {
		onView(withId(R.id.action_export_home)).perform(click());
	}
	public void selectFolder(File folder) {
		Matcher<View> tempFolder = hasDescendant(withText(folder.getName()));
		onView(withId(R.id.backups)).perform(actionOnItem(tempFolder, click()));
	}
	private BackupResultActor assertBackupFinished() {
		BackupResultActor result = new BackupResultActor();
		result.assertDialogDisplayed();
		return result;
	}
	public BackupResultActor assertExportFinished() {
		return assertBackupFinished();
	}
	public BackupResultActor assertImportFinished() {
		return assertBackupFinished();
	}
	public ImportExternalActor importExternal() {
		onActionMenuView(withText(R.string.backup_import_external)).perform(click());
		return new ImportExternalActor();
	}
	public void assertEmptyState() {
		// no result is picked up from saved state or notification intent
		assertNoDialogIsDisplayed();
		// progress bar is not displayed
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}

	public static class ImportExternalActor {
		public Uri mockImportFromFile(File inventory) throws IOException {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(inventory));
			zip.putNextEntry(new ZipEntry(Paths.BACKUP_DATA_FILENAME));
			IOTools.copyStream(getTargetContext().getAssets().open("demo.xml"), zip);
			Uri data = Uri.fromFile(inventory);
			Intent mockIntent = new Intent().setData(data);
			intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, mockIntent));
			return data;
		}
		public void verifyMockImport(Uri dataUri) {
			intended(chooser(allOf(
					hasAction(Intent.ACTION_GET_CONTENT),
					hasCategories(Collections.singleton(Intent.CATEGORY_OPENABLE)),
					hasType("*/*"),
					hasData(dataUri)
			)));
		}
	}

	public static class ExportInternalActor extends BackupResultActor {
	}

	public static class ExportExternalActor extends AlertDialogActor {
		@Override public void assertDialogDisplayed() {
			onView(isDialogTitle())
					.inRoot(isDialog())
					.check(matches(allOf(isDisplayed(), withText(R.string.backup_export_external))));
		}
		public void cancel() {
			clickNegativeInDialog();
		}
		public ExportChooserActor continueToChooser() throws UiObjectNotFoundException {
			clickPositiveInDialog();
			ExportChooserActor chooser = new ExportChooserActor();
			chooser.assertDialogDisplayed();
			return chooser;
		}
	}

	public static class ExportChooserActor {
		public void assertDialogDisplayed() throws UiObjectNotFoundException {
			assertThat(getChooserTitle(), isString(R.string.backup_export_external));
		}
		public void cancel() {
			pressBackExternal();
		}
		public DriveBackupActor chooseDrive() throws UiObjectNotFoundException, NameNotFoundException {
			clickOnLabel(saveToDrive());
			DriveBackupActor drive = new DriveBackupActor();
			drive.assertDialogDisplayed();
			return drive;
		}
	}

	public static class BackupResultActor extends AlertDialogActor {
		@Override public void assertDialogDisplayed() {
			onView(withText(R.string.backup_export_result_finished)).inRoot(isDialog()).check(matches(isDisplayed()));
		}
		public void dismiss() {
			dismissWitNeutral();
		}
	}

	public static class DriveBackupActor {
		private static final Logger LOG = LoggerFactory.getLogger(DriveBackupActor.class);
		public static void assumeIsAvailable() {
			assumeThat(getContext(), hasPackageInstalled(PACKAGE_GOOGLE_DRIVE));
		}

		public void assertDialogDisplayed() throws UiObjectNotFoundException, NameNotFoundException {
			assertThat(getText(dialogTitle()), is(saveToDrive()));
		}

		public void cancel() throws UiObjectNotFoundException {
			clickNegativeInExternalDialog();
		}

		public void saveToAndroidTests(String folder) throws UiObjectNotFoundException, NameNotFoundException {
			assumeThat(getText(dialogTitle()), is(saveToDrive()));
			clickOn(uploadFolder());
			{
				while (!myDrive().equals(getText(dialogTitle()))) {
					shortClickOn(up());
				}
				selectTitleInList("Android Tests").click();

				clickOn(newFolder());
				{
					setText(newFolderTitle(), folder);
					clickPositiveInExternalDialog(); // OK to create folder
				}
				try {
					clickOnLabel(GoogleDriveAutomator.selectFolder());
				} catch (UiObjectNotFoundException ex) {
					LOG.warn("'{}' is flaky, try again", GoogleDriveAutomator.selectFolder(), ex);
					clickOnLabel(GoogleDriveAutomator.selectFolder());
				}
			}
		}

		public void save() throws UiObjectNotFoundException, NameNotFoundException {
			clickOnLabel(GoogleDriveAutomator.save());
		}

		public String getSaveFileName() throws UiObjectNotFoundException, NameNotFoundException {
			return getText(documentTitle());
		}
	}
}
