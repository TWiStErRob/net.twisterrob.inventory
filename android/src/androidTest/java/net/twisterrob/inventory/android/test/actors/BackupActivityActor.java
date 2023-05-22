package net.twisterrob.inventory.android.test.actors;

import java.io.*;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.zip.*;

import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;
import static org.junit.Assume.assumeTrue;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build.*;
import android.os.Environment;

import androidx.annotation.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import static androidx.test.core.app.ApplicationProvider.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.intent.Intents.*;
import static androidx.test.espresso.intent.matcher.IntentMatchers.*;
import static androidx.test.espresso.matcher.RootMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.automators.AndroidAutomator;
import net.twisterrob.android.test.automators.DocumentsUiAutomator;
import net.twisterrob.android.test.automators.GoogleDriveAutomator;
import net.twisterrob.android.test.automators.UiAutomatorExtensions;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.android.utils.tools.ResourceTools;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BackupActivity;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.BackupImportActor.BackupImportResultActor;
import net.twisterrob.java.utils.ObjectTools;

import static net.twisterrob.android.test.automators.AndroidAutomator.*;
import static net.twisterrob.android.test.automators.DocumentsUiAutomator.drawerTitleOpenFrom;
import static net.twisterrob.android.test.automators.DocumentsUiAutomator.drawerTitleSaveTo;
import static net.twisterrob.android.test.automators.GoogleDriveAutomator.*;
import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;
import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class BackupActivityActor extends ActivityActor {
	public BackupActivityActor() {
		super(BackupActivity.class);
	}

	public void openedViaIntent() {
		intended(allOf(isInternal(), hasComponent(BackupActivity.class.getName())));
	}

	public BackupSendActor send() {
		onView(withId(R.id.backup_send)).perform(click());
		BackupSendActor dialog = new BackupSendActor();
		dialog.assertDisplayed();
		return dialog;
	}
	public BackupImportActor importBackup() {
		onView(withId(R.id.backup_import)).perform(click());
		BackupImportActor dialog = new BackupImportActor();
		dialog.assertDisplayed();
		return dialog;
	}
	public BackupExportPickerActor exportBackup()
			throws NameNotFoundException, UiObjectNotFoundException, TimeoutException {
		onView(withId(R.id.backup_export)).perform(click());
		// The DocumentsUi will have a filename field, which gets the focus on open and triggers IME.
		UiAutomatorExtensions.ensureNoSoftKeyboard();
		BackupExportPickerActor picker = new BackupExportPickerActor();
		picker.assertDisplayed();
		return picker;
	}

	public void assertEmptyState() {
		// no result is picked up from saved state or notification intent
		assertNoDialogIsDisplayed();
		// progress bar is not displayed
		assertNoProgressDisplayed();
	}
	public void assertNoProgressDisplayed() {
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}

	public static class BackupImportActor extends AlertDialogActor {
		public BackupImportStubActor continueToPickerStubbed() {
			clickPositiveInDialog();
			return new BackupImportStubActor();
		}

		public BackupImportPickerActor continueToPicker()
				throws NameNotFoundException, UiObjectNotFoundException {
			clickPositiveInDialog();
			BackupImportPickerActor result = new BackupImportPickerActor();
			result.assertDisplayed();
			return result;
		}

		@Override public void assertDisplayed() {
			onView(isDialogTitle())
					.inRoot(isDialog())
					.check(matches(isCompletelyDisplayed()))
					.check(matches(withText(R.string.backup_import)));
		}
		
		public void cancel() {
			clickNegativeInDialog();
		}
		public static class BackupImportResultActor extends BackupResultActor {
			@Override public void assertDisplayed() {
				onView(isDialogTitle())
						.inRoot(isDialog())
						.check(matches(isCompletelyDisplayed()))
						.check(matches(withText(R.string.backup_import_result_finished)))
				;
			}
		}
	}

	public static class BackupImportStubActor {
		public static Uri mockImportFromFile(File inventory) throws IOException {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(inventory));
			zip.putNextEntry(new ZipEntry(Paths.BACKUP_DATA_FILENAME));
			IOTools.copyStream(getApplicationContext().getAssets().open("demo.xml"), zip);
			Uri data = Uri.fromFile(inventory);
			Intent mockIntent = new Intent().setData(data);
			intending(not(isInternal()))
					.respondWith(new ActivityResult(Activity.RESULT_OK, mockIntent));
			return data;
		}
		public static void mockImportFromAnyFile() {
			intending(not(isInternal()))
					.respondWith(new ActivityResult(Activity.RESULT_CANCELED, null));
		}

		public BackupImportResultActor verifyMockImport() {
			intended(allOf(
					hasAction(Intent.ACTION_OPEN_DOCUMENT),
					hasCategories(Collections.singleton(Intent.CATEGORY_OPENABLE)),
					hasType("*/*")
			));
			BackupImportResultActor result = new BackupImportResultActor();
			result.assertDisplayed();
			return result;
		}

		public static void verifyNoImport() {
			assertNoUnverifiedIntents();
		}
	}

	public static class BackupImportPickerActor {
		public static void prepareImportableFile(File temp) throws IOException {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(temp));
			zip.putNextEntry(new ZipEntry(Paths.BACKUP_DATA_FILENAME));
			IOTools.copyStream(getApplicationContext().getAssets().open("demo.xml"), zip);
		}

		public void assertDisplayed() throws UiObjectNotFoundException, NameNotFoundException {
			// Open the drawer for a moment to check its label,
			// because otherwise it's unclear if it's save or load.
			if (!exists(DocumentsUiAutomator.drawerToolbar())) {
				shortClickOnDescriptionLabel(DocumentsUiAutomator.showRoots());
			}
			assertThat(DocumentsUiAutomator.getDrawerTitle(), is(drawerTitleOpenFrom()));
			clickOnBottomRight(DocumentsUiAutomator.toolbar());
		}

		public void cancel() {
			pressBackExternalUnsafe();
		}
		public void selectInDrawer(@NonNull String root) throws UiObjectNotFoundException, NameNotFoundException {
			shortClickOnDescriptionLabel(DocumentsUiAutomator.showRoots());
			DocumentsUiAutomator.selectRootInDrawer(root).click();
			assertThat(DocumentsUiAutomator.getActivityTitle(), is(root));
		}
		public BackupImportResultActor selectFile(@NonNull String name) throws UiObjectNotFoundException {
			if (VERSION_CODES.N == VERSION.SDK_INT || VERSION_CODES.N_MR1 == VERSION.SDK_INT) {
				// WTF https://stackoverflow.com/q/48067076/253468#comment134489567_48067076
				// Workaround: long click and click OPEN/SELECT from Action Mode toolbar.
				// Except long click doesn't seem to work, it just normal clicks.
				// androidx.test.uiautomator 2.3.0-alpha03 fixed this long-click problem:
				// https://android-review.googlesource.com/c/platform/frameworks/support/+/2491488
				DocumentsUiAutomator.selectItemInList(name).longClick();
				clickOn(DocumentsUiAutomator.open());
			} else {
				DocumentsUiAutomator.selectItemInList(name).click();
			}
			BackupImportResultActor result = new BackupImportResultActor();
			result.assertDisplayed();
			return result;
		}
	}

	public static class BackupExportPickerActor {
		public static void assumeFunctional() {
			// Without SD card pressing "Save" in the CreateDocument() picker's "Download" provider
			// will fail with "Failed to save document" toast.
			assumeThat(
					"No SD card present",
					Environment.getExternalStorageState(), is(Environment.MEDIA_MOUNTED)
			);
		}

		public void assertDisplayed() throws UiObjectNotFoundException, NameNotFoundException {
			// Open the drawer for a moment to check its label,
			// because otherwise it's unclear if it's save or load.
			if (!exists(DocumentsUiAutomator.drawerToolbar())) {
				shortClickOnDescriptionLabel(DocumentsUiAutomator.showRoots());
			}
			assertThat(DocumentsUiAutomator.getDrawerTitle(), is(drawerTitleSaveTo()));
			// CONSIDER RTL: click on top right
			clickOnBottomRight(DocumentsUiAutomator.toolbar());
		}
		public void assertNotDisplayed() {
			assertThat(getCurrentAppPackageName(), not(DocumentsUiAutomator.PACKAGE_DOCUMENTS_UI));
		}
		public void cancel() {
			pressBackExternalUnsafe();
		}
		public void selectInDrawer(@NonNull String root) throws UiObjectNotFoundException, NameNotFoundException {
			shortClickOnDescriptionLabel(DocumentsUiAutomator.showRoots());
			DocumentsUiAutomator.selectRootInDrawer(root).click();
			assertThat(DocumentsUiAutomator.getActivityTitle(), is(root));
		}
		public BackupExportResultActor save() throws UiObjectNotFoundException {
			clickPositiveInExternalDialog();
			BackupExportResultActor actor = new BackupExportResultActor();
			actor.assertDisplayed();
			return actor;
		}
		public static class BackupExportResultActor extends BackupResultActor {
			@Override public void assertDisplayed() {
				onView(isDialogTitle())
						.inRoot(isDialog())
						.check(matches(isCompletelyDisplayed()))
						.check(matches(withText(R.string.backup_export_result_finished)))
				;
			}
		}
	}

	public static class BackupSendActor extends AlertDialogActor {
		@Override public void assertDisplayed() {
			onView(isDialogTitle())
					.inRoot(isDialog())
					.check(matches(isCompletelyDisplayed()))
					.check(matches(withText(R.string.backup_send)))
			;
		}
		public void cancel() {
			clickNegativeInDialog();
		}
		public BackupSendChooserActor continueToChooser() throws UiObjectNotFoundException {
			clickPositiveInDialog();
			BackupSendChooserActor chooser = new BackupSendChooserActor();
			chooser.assertDialogDisplayed();
			return chooser;
		}
		public BackupSendResultActor assertFinished() {
			BackupSendResultActor actor = new BackupSendResultActor();
			actor.assertDisplayed();
			return actor;
		}
		public static class BackupSendResultActor extends BackupResultActor {
			@Override public void assertDisplayed() {
				onView(isDialogTitle())
						.inRoot(isDialog())
						.check(matches(isCompletelyDisplayed()))
						.check(matches(withText(R.string.backup_export_result_finished)))
				;
			}
		}
	}

	public static class BackupSendChooserActor {
		public static void assumeFunctional() {
			assumeThat(
					"There aren't enough apps installed to show a chooser for Backup > Send.",
					new Intent(Intent.ACTION_SEND).setType(InventoryContract.Export.TYPE_BACKUP),
					canBeResolved(hasSize(greaterThanOrEqualTo(2)))
			);
		}
		public void assertDialogDisplayed() throws UiObjectNotFoundException {
			if (VERSION_CODES.S <= VERSION.SDK_INT) {
				// From Android 12, the title of Intent.createChooser is removed.
				assertThat(getChooserTitle(), allOf(startsWith("Inventory_"), endsWith(".zip")));
			} else if (VERSION_CODES.Q <= VERSION.SDK_INT) {
				// In Android 10, the title of Intent.createChooser is blatantly ignored.
				assertThat(getChooserTitle(), equalTo("Share"));
			} else {
				assertThat(getChooserTitle(), isString(R.string.backup_send));
			}
		}

		public void cancel() {
			pressBackExternalUnsafe();
		}

		@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
		public void choose(int index) throws UiObjectNotFoundException {
			String listId = UiAutomatorExtensions.androidId(ResourceTools.getIDResourceID(null, "resolver_list"));
			UiSelector iconId = new UiSelector().resourceId(UiAutomatorExtensions.androidId(android.R.id.icon));
			UiScrollable list = new UiScrollable(new UiSelector().resourceId(listId));
			UiObject item = list.getChildByInstance(iconId, index);
			item.clickAndWaitForNewWindow();
			AndroidAutomator.acceptAnyPermissions();
		}

		@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
		public DriveBackupActor chooseDrive() throws UiObjectNotFoundException, NameNotFoundException {
			DriveBackupActor drive = new DriveBackupActor();
			drive.selectSaveToDriveFromChooser();
			drive.assertSaveToDriveDisplayed();
			return drive;
		}
	}

	private static class BackupResultActor extends AlertDialogActor {
		public void dismiss() {
			dismissWithNeutral();
		}
	}

	@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
	public static class DriveBackupActor {
		private static final Logger LOG = LoggerFactory.getLogger(DriveBackupActor.class);

		public static void assumeDriveInstalled() {
			assumeThat(
					"Google Drive is not installed",
					InstrumentationRegistry.getInstrumentation().getContext(),
					hasPackageInstalled(PACKAGE_GOOGLE_DRIVE)
			);
		}

		public static void assumeDriveFunctional() throws UiObjectNotFoundException {
			assumeDriveInstalled();
			try {
				launchAppAndWait(PACKAGE_GOOGLE_DRIVE);
				if (exists(GoogleDriveAutomator.welcomeScreen())) {
					clickOn(GoogleDriveAutomator.skipWelcome());
				}
				assumeThat("Google Drive not logged in, cannot continue",
						getCurrentAppPackageName(), not(PACKAGE_GOOGLE_SIGN_IN));
				assumeThat("Google Drive didn't launch properly",
						getCurrentAppPackageName(), is(PACKAGE_GOOGLE_DRIVE));
				assumeTrue("Google Drive doesn't have a bottom navigation",
						exists(GoogleDriveAutomator.bottomNavigation()));
				assumeTrue("Google Drive doesn't have a search bar",
						exists(GoogleDriveAutomator.searchBar()));
			} catch (Throwable ex) {
				// closing logic is duplicated, because it needs to not hide the error in try { ... }
				try {
					// try to close whatever was launched
					pressBackExternalUnsafe();
					assertThat(getCurrentAppPackageName(), is(getApplicationContext().getPackageName()));
				} catch (Throwable failEx) {
					//noinspection ThrowableNotThrown but used to set cause
					ObjectTools.getRootCause(failEx).initCause(ex);
					throw failEx;
				}
				throw ex;
			}
			// try to close whatever was launched
			pressBackExternalUnsafe();
			assertThat(getCurrentAppPackageName(), is(getApplicationContext().getPackageName()));
		}

		public void selectSaveToDriveFromChooser() throws UiObjectNotFoundException, NameNotFoundException {
			// TODO refactor this as AndroidAutomator.chooseItem(saveToDrive())
			// TODO handle the case when the chosen action is not on the first page of the chooser
			clickOnLabel(saveToDriveChooserTitle());
			acceptAnyPermissions();
			// TODO make sure to let the chooser reorganize its items on 23+ (waitForIdle not enough)
		}

		public void assertSaveToDriveDisplayed() throws UiObjectNotFoundException, NameNotFoundException {
			assertThat(GoogleDriveAutomator.getActivityTitle(), is(saveToDriveDialogTitle()));
		}

		public void cancel() throws UiObjectNotFoundException, NameNotFoundException {
			clickOn(GoogleDriveAutomator.getUpNavigation());
			assertThat(GoogleDriveAutomator.getAlertTitle(), equalTo(GoogleDriveAutomator.cancelUploadDialogTitle()));
			clickPositiveInExternalDialog();
		}

		public void saveToAndroidTests(String folder) throws UiObjectNotFoundException, NameNotFoundException {
			assertSaveToDriveDisplayed();
			// TODO this looks like it should be multiple actors
			clickOn(uploadFolder());
			{
				while (!myDrive().equals(GoogleDriveAutomator.getActivityTitle())) {
					shortClickOn(GoogleDriveAutomator.getUpNavigation());
				}
				selectTitleInList("Android Tests").click();

				clickOn(newFolder());
				{
					setText(newFolderTitle(), folder);
					clickOn(confirmCreateFolder());
				}
				clickOn(GoogleDriveAutomator.selectFolder());
			}
		}

		public void save() throws UiObjectNotFoundException, NameNotFoundException {
			clickOn(GoogleDriveAutomator.saveToDriveDialogSave());
		}

		public String getSaveFileName() throws UiObjectNotFoundException {
			return getText(GoogleDriveAutomator.saveToDriveFileName());
		}
	}
}
