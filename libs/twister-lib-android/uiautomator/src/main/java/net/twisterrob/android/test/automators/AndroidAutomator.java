package net.twisterrob.android.test.automators;

import android.content.*;
import android.os.Build.*;
import android.support.annotation.*;
import android.support.test.uiautomator.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.annotation.IdResName;
import net.twisterrob.android.test.espresso.DialogMatchers;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;

public class AndroidAutomator {

	public static final String PACKAGE_PACKAGE_INSTALLER = "com.android.packageinstaller";

	public static @IdResName String permissionAllow() {
		return UiAutomatorExtensions.externalId(PACKAGE_PACKAGE_INSTALLER, "permission_allow_button");
	}

	@RequiresApi(api = VERSION_CODES.JELLY_BEAN)
	public static void allowPermissionsIfNeeded() throws UiObjectNotFoundException {
		if (VERSION_CODES.M <= VERSION.SDK_INT) {
			UiDevice device = UiDevice.getInstance(getInstrumentation());
			UiObject allow = device.findObject(new UiSelector().resourceId(permissionAllow()));
			if (allow.exists()) {
				shortClickOn(permissionAllow());
			}
		}
	}

	public static void launchApp(@NonNull String packageName) {
		Context context = getInstrumentation().getContext();
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		if (intent == null) {
			throw new IllegalArgumentException(packageName + " does not have a launchable intent");
		}
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		}
		context.startActivity(intent);
	}

	@RequiresApi(UiAutomatorExtensions.UI_AUTOMATOR_VERSION)
	public static String getPositiveButtonLabel() throws UiObjectNotFoundException {
		return getText(UiAutomatorExtensions.androidId(DialogMatchers.BUTTON_POSITIVE));
	}

	@RequiresApi(UiAutomatorExtensions.UI_AUTOMATOR_VERSION)
	public static String getChooserTitle() throws UiObjectNotFoundException {
		// TOFIX use android.R.id.title_default when targeting 23+
		// on 28 it's title
		int id = /*VERSION.SDK_INT >= VERSION_CODES.M
				? ResourceTools.getIDResourceID(null, "title_default")
				: */android.R.id.title;
		return UiAutomatorExtensions.getText(UiAutomatorExtensions.androidId(id));
	}
}
