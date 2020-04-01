package net.twisterrob.android.activity;

import org.junit.*;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

/**
 * @see CaptureImage
 */
@RunWith(AndroidJUnit4.class)
public class CaptureImageTest {

	@Rule
	public final CaptureImageActivityTestRule activity = new CaptureImageActivityTestRule();

	private final CaptureImageActivityActor captureImage = new CaptureImageActivityActor();

	@Test public void flashStateRememberedBetweenLaunches_off() throws UiObjectNotFoundException {
		captureImage.clearPreferences();
		activity.launchActivity(null);
		captureImage.allowPermissions();
		captureImage.turnFlashOn();
		captureImage.turnFlashOff();
		activity.finishActivity();

		activity.launchActivity(null);

		captureImage.assertFlashOff();
	}

	@Test public void flashStateRememberedBetweenLaunches_on() throws UiObjectNotFoundException {
		captureImage.clearPreferences();
		activity.launchActivity(null);
		captureImage.allowPermissions();
		captureImage.turnFlashOn();
		activity.finishActivity();

		activity.launchActivity(null);

		captureImage.assertFlashOn();
	}
}
