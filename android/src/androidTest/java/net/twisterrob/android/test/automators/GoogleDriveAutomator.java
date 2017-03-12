package net.twisterrob.android.test.automators;

import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.*;
import android.support.test.uiautomator.*;

public class GoogleDriveAutomator {
	public static final String PACKAGE_GOOGLE_DRIVE = "com.google.android.apps.docs";

	public static @IdRes String newFolderTitle() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "title_editor");
	}
	public static @IdRes String dialogTitle() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "title");
	}
	public static @IdRes String folderTitleInList() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "title");
	}
	public static @IdRes String up() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "up_affordance");
	}
	public static @IdRes String documentTitle() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "upload_edittext_document_title");
	}
	public static @IdRes String uploadFolder() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "upload_folder");
	}
	public static @IdRes String newFolder() {
		return UiAutomatorExtensions.externalId(PACKAGE_GOOGLE_DRIVE, "icon_new");
	}
	public static @RawRes String myDrive() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "menu_my_drive", "My Drive");
	}
	public static @RawRes String saveToDrive() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "upload_shared_item_title", "Save to Drive");
	}
	public static @RawRes String selectFolder() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "dialog_select", "Select");
	}
	public static @RawRes String save() throws NameNotFoundException {
		return UiAutomatorExtensions.externalString(PACKAGE_GOOGLE_DRIVE, "upload_shared_item_confirm", "Save");
	}
	public static UiObject selectTitleInList(String folderName) throws UiObjectNotFoundException {
		UiScrollable list =
				new UiScrollable(new UiSelector().resourceId(UiAutomatorExtensions.androidId(android.R.id.list)));
		return list.getChildByText(new UiSelector().resourceId(folderTitleInList()), folderName);
	}
}
