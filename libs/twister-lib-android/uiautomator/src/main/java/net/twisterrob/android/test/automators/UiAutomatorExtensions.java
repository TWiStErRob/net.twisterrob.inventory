package net.twisterrob.android.test.automators;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;
import static org.junit.Assert.*;

import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import android.support.annotation.*;
import android.support.test.uiautomator.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.annotation.*;
import net.twisterrob.android.test.espresso.DialogMatchers;

/**
 * {@link RawRes} is intentionally misused in this class.
 * It signifies a normal string of pointing to no particular type of resource, but containing a value of a resource. 
 */
public class UiAutomatorExtensions {
	private static final Logger LOG = LoggerFactory.getLogger(UiAutomatorExtensions.class);

	public static final int UI_AUTOMATOR_VERSION = VERSION_CODES.JELLY_BEAN_MR2;

	@RequiresApi(UI_AUTOMATOR_VERSION)
	public static String getText(@IdResName String id) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		return object.getText();
	}
	@RequiresApi(UI_AUTOMATOR_VERSION)
	public static void setText(@IdResName String id, String value) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		assertTrue("expected to set text", object.setText(value));
	}
	@RequiresApi(UI_AUTOMATOR_VERSION)
	public static void clickOn(@IdResName String id) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		assertTrue("expected to click and new window appear", object.clickAndWaitForNewWindow());
	}
	@RequiresApi(UI_AUTOMATOR_VERSION)
	public static void clickOnLabel(String label) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().text(label));
		assertTrue("expected to click and new window appear", object.clickAndWaitForNewWindow());
	}
	@RequiresApi(UI_AUTOMATOR_VERSION)
	public static void shortClickOn(@IdResName String id) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().resourceId(id));
		assertTrue("expected to click", object.click());
	}
	@RequiresApi(UI_AUTOMATOR_VERSION)
	public static void shortClickOnLabel(String label) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject object = device.findObject(new UiSelector().text(label));
		assertTrue("expected to click", object.click());
	}
	public static @IdResName String androidId(@IdRes int resId) {
		return getContext().getResources().getResourceName(resId);
	}
	public static @IdResName String internalId(@IdRes int resId) {
		return getTargetContext().getResources().getResourceName(resId);
	}
	public static @IdResName String externalId(String packageName, @StringResName String resName) {
		return packageName + ":id/" + resName;
	}
	public static String externalString(String packageName, @StringResName String resName,
			String englishFallback)
			throws NameNotFoundException {
		Resources res = getContext().getPackageManager().getResourcesForApplication(packageName);
		@StringRes int resId = res.getIdentifier(resName, "string", packageName);

		String resValue;
		if (resId != 0) {
			resValue = res.getString(resId);
		} else {
			String warning = String.format("Missing resource: @%s:string/%s", packageName, resName);
			assumeThat(warning + "; can't use English fallback",
					Locale.getDefault().getLanguage(), is("en"));
			LOG.warn(warning + "; using English fallback: " + englishFallback);
			resValue = englishFallback;
		}
		return resValue;
	}

	@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
	private static void clickInExternalDialog(@IdRes int buttonId) throws UiObjectNotFoundException {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		UiObject dialogButton = device.findObject(new UiSelector().resourceId(androidId(buttonId)));
		assertTrue("expected to click and new window appear", dialogButton.clickAndWaitForNewWindow());
	}
	@RequiresApi(UiAutomatorExtensions.UI_AUTOMATOR_VERSION)
	public static void clickPositiveInExternalDialog() throws UiObjectNotFoundException {
		clickInExternalDialog(DialogMatchers.BUTTON_POSITIVE);
	}
	@RequiresApi(UiAutomatorExtensions.UI_AUTOMATOR_VERSION)
	public static void clickNegativeInExternalDialog() throws UiObjectNotFoundException {
		clickInExternalDialog(DialogMatchers.BUTTON_NEGATIVE);
	}
	@RequiresApi(UiAutomatorExtensions.UI_AUTOMATOR_VERSION)
	public static void clickNeutralInExternalDialog() throws UiObjectNotFoundException {
		clickInExternalDialog(DialogMatchers.BUTTON_NEUTRAL);
	}

	@RequiresApi(VERSION_CODES.JELLY_BEAN)
	public static void pressBackExternal() {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		assertTrue("expected to press Back button", device.pressBack());
	}

	@RequiresApi(VERSION_CODES.JELLY_BEAN)
	public static String getCurrentAppPackageName() {
		UiDevice device = UiDevice.getInstance(getInstrumentation());
		return device.getCurrentPackageName();
	}

	@RequiresApi(VERSION_CODES.JELLY_BEAN)
	public static void waitForAppToBeBackgrounded() {
		final long timeout = TimeUnit.SECONDS.toMillis(10);

		UiDevice device = UiDevice.getInstance(getInstrumentation());
		BySelector appPackage = By.pkg(getTargetContext().getPackageName()).depth(0);
		assertTrue("expected " + appPackage + " to disappear", device.wait(Until.gone(appPackage), timeout));
	}

	@RequiresApi(VERSION_CODES.JELLY_BEAN)
	public static void waitForAppToBeForegrounded(@NonNull String packageName) {
		final long timeout = TimeUnit.SECONDS.toMillis(10);

		UiDevice device = UiDevice.getInstance(getInstrumentation());
		BySelector appPackage = By.pkg(packageName).depth(0);
		assertTrue("expected " + appPackage + " to appear", device.wait(Until.hasObject(appPackage), timeout));
	}

	/**
	 * @param previousPackageName a saved value from {@link #getCurrentAppPackageName()} before an action was performed
	 */
	@RequiresApi(VERSION_CODES.JELLY_BEAN)
	public static void waitForAnAppToBeForegrounded(@NonNull String previousPackageName) {
		final long timeout = TimeUnit.SECONDS.toMillis(10);

		UiDevice device = UiDevice.getInstance(getInstrumentation());
		BySelector appPackage = By.pkg(previousPackageName).depth(0);
		assertTrue("expected an app other than " + appPackage + " to appear",
				device.wait(Until.gone(appPackage), timeout));
	}
}
