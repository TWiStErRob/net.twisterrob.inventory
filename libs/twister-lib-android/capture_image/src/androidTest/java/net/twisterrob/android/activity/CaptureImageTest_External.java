package net.twisterrob.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.graphics.Color;
import android.net.Uri;
import android.support.test.espresso.intent.Intents;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import static android.support.test.espresso.intent.Intents.*;

/**
 * @see CaptureImage
 */
@RunWith(AndroidJUnit4.class)
public class CaptureImageTest_External {

	@Rule
	public final CaptureImageActivityTestRule activity = new CaptureImageActivityTestRule();

	private final CaptureImageActivityActor captureImage = new CaptureImageActivityActor();

	@Test public void loadsImageFromExternalSource_resultIntentDataUriFile()
			throws UiObjectNotFoundException, IOException {
		activity.launchActivity(null);
		captureImage.allowPermissions();
		Uri fakeUri = captureImage.createFakeImage(activity.getTemp().newFile(), Color.RED);
		captureImage.intendExternalChooser(fakeUri);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		Intents.assertNoUnverifiedIntents();
		captureImage.verifyImageColor(equalTo(Color.RED));
	}

	@Test public void fallsBackPreviousImageIfPickCancelled()
			throws UiObjectNotFoundException, IOException {
		activity.launchActivity(null);
		captureImage.allowPermissions();
		Uri fakeUri = captureImage.createFakeImage(activity.getTemp().newFile(), Color.RED);
		captureImage.intendExternalChooser(fakeUri);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		captureImage.intendExternalChooserCancelled();
		captureImage.pick();
		captureImage.verifyExternalChooser(times(2));
		Intents.assertNoUnverifiedIntents();
		captureImage.verifyImageColor(equalTo(Color.RED));
	}
}
