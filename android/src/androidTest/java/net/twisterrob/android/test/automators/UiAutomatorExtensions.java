package net.twisterrob.android.test.automators;

import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import android.support.annotation.*;
import android.support.test.uiautomator.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.test.espresso.DialogMatchers;

import static net.twisterrob.android.test.matchers.AndroidMatchers.hasProperty;

/**
 * {@link RawRes} is intentionally misused in this class.
 * It signifies a normal string of pointing to no particular type of resource, but containing a value of a resource. 
 */
public class UiAutomatorExtensions {
	@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
	public static String getText(@IdRes String id) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		return object.getText();
	}
	@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
	public static void setText(@IdRes String id, @RawRes String value) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		object.setText(value);
	}
	@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
	public static void clickOn(@IdRes String id) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		object.clickAndWaitForNewWindow();
	}
	@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
	public static void clickOnLabel(@RawRes String label) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().text(label));
		object.clickAndWaitForNewWindow();
	}
	@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
	public static void shortClickOn(@IdRes String id) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		object.click();
	}
	@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
	public static void shortClickOnLabel(@RawRes String label) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().text(label));
		object.click();
	}
	public static @IdRes String androidId(@IdRes int resId) {
		return getContext().getResources().getResourceName(resId);
	}
	public static @IdRes String internalId(@IdRes int resId) {
		return getTargetContext().getResources().getResourceName(resId);
	}
	public static @IdRes String externalId(String packageName, @StringRes String resName) {
		return packageName + ":id/" + resName;
	}
	public static @RawRes String externalString(String packageName, @StringRes String resName,
			@RawRes String englishFallback)
			throws NameNotFoundException {
		Resources res = getContext().getPackageManager().getResourcesForApplication(packageName);
		@StringRes int resId = res.getIdentifier(resName, "string", packageName);

		@RawRes String resValue;
		if (resId != 0) {
			resValue = res.getString(resId);
		} else {
			assumeThat(Locale.getDefault(), hasProperty("language", equalTo("en")));
			resValue = englishFallback;
		}
		return resValue;
	}

	private static void clickInExternalDialog(@IdRes int buttonId) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject dialogButton = device.findObject(new UiSelector().resourceId(androidId(buttonId)));
		dialogButton.clickAndWaitForNewWindow();
	}
	public static void clickPositiveInExternalDialog() throws UiObjectNotFoundException {
		clickInExternalDialog(DialogMatchers.BUTTON_POSITIVE);
	}
	public static void clickNegativeInExternalDialog() throws UiObjectNotFoundException {
		clickInExternalDialog(DialogMatchers.BUTTON_NEGATIVE);
	}
	public static void clickNeutralInExternalDialog() throws UiObjectNotFoundException {
		clickInExternalDialog(DialogMatchers.BUTTON_NEUTRAL);
	}
	public static void pressBackExternal() {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		device.pressBack();
	}
}
