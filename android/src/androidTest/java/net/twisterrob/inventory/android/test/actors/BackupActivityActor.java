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
import android.os.Build.VERSION_CODES;
import android.support.annotation.*;
import android.support.test.uiautomator.*;
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

import net.twisterrob.android.test.automators.*;
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
	public ExportInternalResultActor exportInternal() {
		onView(withId(R.id.fab)).perform(click());
		return new ExportInternalResultActor();
	}
	public ExportExternalActor exportExternal() {
		clickActionOverflow(R.id.action_export_external);
		ExportExternalActor dialog = new ExportExternalActor();
		dialog.assertDisplayed();
		return dialog;
	}
	public void assertNoProgressDisplayed() {
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}
	public void checkFileShown(String date) {
		Matcher<View> justExportedLabel = withText(containsString(date));
		onView(withId(R.id.backups)).perform(scrollTo(hasDescendant(justExportedLabel)));
		onView(justExportedLabel).check(matches(isCompletelyDisplayed())); // validates if shown
	}
	public void gotoHomeFolder() {
		onView(withId(R.id.action_export_home)).perform(click());
	}
	public void selectFolder(File folder) {
		Matcher<View> tempFolder = hasDescendant(withText(folder.getName()));
		onView(withId(R.id.backups)).perform(actionOnItem(tempFolder, click()));
	}
	public ImportExternalActor importExternal() {
		clickActionOverflow(R.id.action_import_external);
		return new ImportExternalActor();
	}
	public void assertEmptyState() {
		// no result is picked up from saved state or notification intent
		assertNoDialogIsDisplayed();
		// progress bar is not displayed
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}

	public static class ImportExternalActor {
		public static Uri mockImportFromFile(File inventory) throws IOException {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(inventory));
			zip.putNextEntry(new ZipEntry(Paths.BACKUP_DATA_FILENAME));
			IOTools.copyStream(getTargetContext().getAssets().open("demo.xml"), zip);
			Uri data = Uri.fromFile(inventory);
			Intent mockIntent = new Intent().setData(data);
			intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, mockIntent));
			return data;
		}
		public void verifyMockImport() {
			intended(chooser(allOf(
					hasAction(Intent.ACTION_GET_CONTENT),
					hasCategories(Collections.singleton(Intent.CATEGORY_OPENABLE)),
					hasType("*/*")
			)));
		}
		public ImportExternalResultActor assertFinished() {
			ImportExternalResultActor result = new ImportExternalResultActor();
			result.assertDisplayed();
			return result;
		}
		public static class ImportExternalResultActor extends BackupResultActor {
			@Override public void assertDisplayed() {
				@StringRes int title = R.string.backup_import_result_finished;
				onView(isDialogTitle())
						.inRoot(isDialog())
						.check(matches(isCompletelyDisplayed()))
						.check(matches(withText(title)))
				;
			}
		}
	}

	public static class ExportInternalResultActor extends BackupResultActor {
		@Override public void assertDisplayed() {
			onView(isDialogTitle())
					.inRoot(isDialog())
					.check(matches(isCompletelyDisplayed()))
					.check(matches(withText(R.string.backup_export_result_finished)))
			;
		}
	}

	public static class ExportExternalActor extends AlertDialogActor {
		@Override public void assertDisplayed() {
			onView(isDialogTitle())
					.inRoot(isDialog())
					.check(matches(isCompletelyDisplayed()))
					.check(matches(withText(R.string.backup_export_external)))
			;
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
		public ExportExternalResultActor assertFinished() {
			ExportExternalResultActor actor = new ExportExternalResultActor();
			actor.assertDisplayed();
			return actor;
		}
		public static class ExportExternalResultActor extends BackupResultActor {
			@Override public void assertDisplayed() {
				@StringRes int title = R.string.backup_export_result_finished;
				onView(isDialogTitle())
						.inRoot(isDialog())
						.check(matches(isCompletelyDisplayed()))
						.check(matches(withText(title)))
				;
			}
		}
	}

	public static class ExportChooserActor {
		public void assertDialogDisplayed() throws UiObjectNotFoundException {
			assertThat(getChooserTitle(), isString(R.string.backup_export_external));
		}
		public void cancel() {
			pressBackExternal();
		}

		@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
		public DriveBackupActor chooseDrive() throws UiObjectNotFoundException, NameNotFoundException {
			// TODO refactor this as AndroidAutomator.chooseItem(saveToDrive())
			// TODO handle the case when the chosen action is not on the first page of the chooser
			clickOnLabel(saveToDrive());
			// TODO make sure to let the chooser reorganize its items on 23+ (waitForIdle not enough)
			DriveBackupActor drive = new DriveBackupActor();
			drive.assertDialogDisplayed();
			return drive;
		}
	}

	private static class BackupResultActor extends AlertDialogActor {
		public void dismiss() {
			dismissWitNeutral();
		}
	}

	// FIXME handle Marshmallow permission dialog that comes up on first usage of Drive
	@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
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

		public String getSaveFileName() throws UiObjectNotFoundException {
			return getText(documentTitle());
		}
	}
}
