package net.twisterrob.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.graphics.Color;
import android.net.Uri;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.UiObjectNotFoundException;

import static androidx.test.espresso.intent.Intents.*;

import net.twisterrob.android.view.SelectionView.SelectionStatus;

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
		captureImage.verifyState(SelectionStatus.NORMAL);
		captureImage.intendExternalChooser(fakeUri);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		captureImage.verifyState(SelectionStatus.FOCUSED);
		Intents.assertNoUnverifiedIntents();
		captureImage.verifyImageColor(equalTo(Color.RED));
	}

	@Test public void fallsBackPreviousImageIfPickCancelled()
			throws UiObjectNotFoundException, IOException {
		activity.launchActivity(null);
		captureImage.allowPermissions();
		Uri fakeUri = captureImage.createFakeImage(activity.getTemp().newFile(), Color.GREEN);
		captureImage.verifyState(SelectionStatus.NORMAL);
		captureImage.intendExternalChooser(fakeUri);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		captureImage.verifyState(SelectionStatus.FOCUSED);
		captureImage.intendExternalChooserCancelled();
		captureImage.pick();
		captureImage.verifyExternalChooser(times(2));
		captureImage.verifyState(SelectionStatus.BLURRY);
		Intents.assertNoUnverifiedIntents();
		captureImage.verifyImageColor(equalTo(Color.GREEN));
	}

	@Test public void doesNotFallBackToImageFromClosedActivityIfPickCancelled()
			throws UiObjectNotFoundException, IOException {
		activity.launchActivity(null);
		captureImage.allowPermissions();
		Uri fakeUri = captureImage.createFakeImage(activity.getTemp().newFile(), Color.BLUE);
		captureImage.intendExternalChooser(fakeUri);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		Intents.assertNoUnverifiedIntents();
		activity.finishActivity();

		activity.launchActivity(null);
		captureImage.verifyState(SelectionStatus.NORMAL);
		captureImage.intendExternalChooserCancelled();
		captureImage.pick();
		captureImage.verifyExternalChooser();
		Intents.assertNoUnverifiedIntents();
		captureImage.verifyNoImage();
		captureImage.verifyState(SelectionStatus.BLURRY);
	}

	@Test public void doesNotFallBackToImageFromClosedActivityIfPickInvalid()
			throws UiObjectNotFoundException, IOException {
		activity.launchActivity(null);
		captureImage.allowPermissions();
		Uri fakeUri = captureImage.createFakeImage(activity.getTemp().newFile(), Color.YELLOW);
		captureImage.intendExternalChooser(fakeUri);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		Intents.assertNoUnverifiedIntents();
		activity.finishActivity();

		activity.launchActivity(null);
		captureImage.verifyState(SelectionStatus.NORMAL);
		captureImage.intendExternalChooser(null);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		Intents.assertNoUnverifiedIntents();
		captureImage.verifyErrorImage();
		captureImage.verifyState(SelectionStatus.BLURRY);
	}
}
