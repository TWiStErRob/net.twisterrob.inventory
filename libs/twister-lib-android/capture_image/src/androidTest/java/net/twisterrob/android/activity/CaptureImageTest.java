package net.twisterrob.android.activity;

import org.junit.*;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import net.twisterrob.android.view.SelectionView.SelectionStatus;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

/**
 * @see CaptureImage
 */
@RunWith(AndroidJUnit4.class)
public class CaptureImageTest {

	@Rule
	public final CaptureImageActivityTestRule activity = new CaptureImageActivityTestRule();

	private final CaptureImageActivityActor captureImage = new CaptureImageActivityActor();

	@Before public void setUp() {
		captureImage.assumeHasCamera();
	}

	@Test public void rotationKeepsTheImage() throws UiObjectNotFoundException {
		captureImage.clearPreferences();
		activity.launchActivity(null);
		captureImage.allowPermissions();

		captureImage.take();
		onRoot().perform(loopMainThreadForAtLeast(1000));
		// Note: this becomes focused only if the camera can take a focused picture.
		// So this test will fail if the phone is on the table with camera facing down.
		captureImage.verifyState(SelectionStatus.FOCUSED);

		captureImage.rotate();

		// TODO not sure what to wait for, Glide is working, but clearly not started at this point.
		onRoot().perform(loopMainThreadForAtLeast(3000));
		captureImage.verifyNotErrorImage();
		// TODO this should be focused: https://github.com/TWiStErRob/net.twisterrob.inventory/issues/167
		captureImage.verifyState(SelectionStatus.NORMAL);
	}
}
