package net.twisterrob.android.activity;

import java.io.File;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import net.twisterrob.android.test.junit.SensibleActivityTestRule;

@RunWith(AndroidJUnit4.class)
public class CaptureImageTest {

	private File outputFile;

	@Rule(order = 1)
	public final TemporaryFolder temp = new TemporaryFolder() {
		@Override protected void before() throws Throwable {
			super.before();
			outputFile = new File(getRoot(), "output.file");
		}
	};

	@Rule(order = 2)
	public final ActivityTestRule<CaptureImage> activity =
			new SensibleActivityTestRule<CaptureImage>(CaptureImage.class, true, false) {

				@Override protected Intent getActivityIntent() {
					return new Intent()
							.putExtra(CaptureImage.EXTRA_OUTPUT, outputFile.getAbsolutePath());
				}
			};

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
