package net.twisterrob.android.test.automators;

import android.os.Build.*;
import android.support.test.uiautomator.UiObjectNotFoundException;

import net.twisterrob.android.utils.tools.AndroidTools;

public class AndroidAutomator {
	public static String getChooserTitle() throws UiObjectNotFoundException {
		// TOFIX use android.R.id.title_default when targeting 23+
		int id = VERSION.SDK_INT >= VERSION_CODES.M
				? AndroidTools.getIDResourceID(null, "title_default")
				: android.R.id.title;
		return UiAutomatorExtensions.getText(UiAutomatorExtensions.androidId(id));
	}
}
