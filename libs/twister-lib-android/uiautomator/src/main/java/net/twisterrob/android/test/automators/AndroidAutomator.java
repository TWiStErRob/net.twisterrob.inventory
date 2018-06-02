package net.twisterrob.android.test.automators;

import android.os.Build.*;
import android.support.annotation.RequiresApi;
import android.support.test.uiautomator.UiObjectNotFoundException;

import net.twisterrob.android.utils.tools.ResourceTools;

public class AndroidAutomator {

	@RequiresApi(UiAutomatorExtensions.UI_AUTOMATOR_VERSION)
	public static String getChooserTitle() throws UiObjectNotFoundException {
		// TOFIX use android.R.id.title_default when targeting 23+
		int id = VERSION.SDK_INT >= VERSION_CODES.M
				? ResourceTools.getIDResourceID(null, "title_default")
				: android.R.id.title;
		return UiAutomatorExtensions.getText(UiAutomatorExtensions.androidId(id));
	}
}
