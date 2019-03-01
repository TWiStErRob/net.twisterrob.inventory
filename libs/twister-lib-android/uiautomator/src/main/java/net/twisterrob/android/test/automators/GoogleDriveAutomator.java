package net.twisterrob.android.test.automators;

import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.RequiresApi;
import android.support.test.uiautomator.*;
import android.widget.TextView;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.annotation.IdResName;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;

/**
 * Resource names last updated from {@code com.google.android.apps.docs} version 190320580 (2.19.032.05.80)
 * that was embedded in the API 28 Google Play emulator.
 */
public class GoogleDriveAutomator {
	public static final String PACKAGE_GOOGLE_DRIVE = "com.google.android.apps.docs";
	public static final String PACKAGE_GOOGLE_SIGN_IN = "com.google.android.gms";

	public static @IdResName String newFolderTitle() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "title_editor");
	}

	/**
	 * Container of hamburger, title, search of Drive home activity.
	 */
	public static @IdResName String toolbar() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "toolbar");
	}
	public static @IdResName String dialogTitle() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "title");
	}
	public static @IdResName String folderTitleInList() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "title");
	}

	/**
	 * Up-navigation button in the folder selector, which is a popup activity.
	 */
	public static @IdResName String up() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "up_affordance");
	}
	public static @IdResName String documentTitle() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "upload_edittext_document_title");
	}

	/**
	 * Destination folder spinner in the "Save to Drive" dialog.
	 */
	public static @IdResName String uploadFolder() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "upload_folder");
	}
	public static @IdResName String newFolder() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "icon_new");
	}

	/**
	 * Label of the home activity of Drive in default startup state.
	 */
	public static String myDrive() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "menu_my_drive", "My Drive");
	}

	/**
	 * Title of the dialog that pops up to confirm drive upload from share intent.
	 */
	public static String saveToDrive() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "upload_shared_item_title", "Save to Drive");
	}

	/**
	 * Positive confirmation button of the folder selector, which pops up from Save to Drive dialog.
	 */
	public static String selectFolder() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "dialog_select", "Select")
		                            // this looks risky, consider android:button1
		                            .toUpperCase();
	}

	/**
	 * Positive confirmation button of {@link #saveToDrive()}.
	 */
	public static String save() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "upload_shared_item_confirm", "Save")
		                            // this looks risky, consider android:button1
		                            .toUpperCase();
	}

	/**
	 * Positive acknowledgement button of "Upload to Drive" dialog when user not logged in to Drive.
	 */
	public static String setupAccount() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(
				PACKAGE_GOOGLE_DRIVE, "no_account_for_upload_setup_account", "Setup account");
	}

	public static UiObject selectTitleInList(String folderName) throws UiObjectNotFoundException {
		UiScrollable list =
				new UiScrollable(new UiSelector().resourceId(UiAutomatorExtensions.androidId(android.R.id.list)));
		return list.getChildByText(new UiSelector().resourceId(folderTitleInList()), folderName);
	}

	/**
	 * Try to get the text for the home activity of Drive.
	 */
	@RequiresApi(UI_AUTOMATOR_VERSION)
	public static String getActivityTitle() throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiSelector toolbar = new UiSelector().resourceId(toolbar());
		UiSelector firstTextView = new UiSelector().className(TextView.class);
		UiObject object = device.findObject(toolbar.childSelector(firstTextView));
		return object.getText();
	}
}
