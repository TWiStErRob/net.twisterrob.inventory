package net.twisterrob.android.test.automators;

import android.support.test.uiautomator.UiObjectNotFoundException;

public class AndroidAutomator {
	public static String getChooserTitle() throws UiObjectNotFoundException {
		return UiAutomatorExtensions.getText(UiAutomatorExtensions.androidId(android.R.id.title));
	}
}
