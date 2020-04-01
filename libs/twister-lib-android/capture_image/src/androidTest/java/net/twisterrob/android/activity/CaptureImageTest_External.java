package net.twisterrob.android.activity;

import java.io.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import net.twisterrob.android.test.espresso.idle.GlideIdlingResource;
import net.twisterrob.android.test.junit.SensibleActivityTestRule;

/**
 * @see CaptureImage
 */
@RunWith(AndroidJUnit4.class)
public class CaptureImageTest_External {

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
				private final GlideIdlingResource glideIdler = new GlideIdlingResource();

				@Override protected void beforeActivityLaunched() {
					IdlingRegistry.getInstance().register(glideIdler);
					super.beforeActivityLaunched();
				}

				@Override protected void afterActivityFinished() {
					super.afterActivityFinished();
					IdlingRegistry.getInstance().unregister(glideIdler);
				}

				@Override protected Intent getActivityIntent() {
					return new Intent()
							.putExtra(CaptureImage.EXTRA_OUTPUT, outputFile.getAbsolutePath());
				}
			};

	private final CaptureImageActivityActor captureImage = new CaptureImageActivityActor();

	@Test public void loadsImageFromExternalSource_resultIntentDataUriFile()
			throws UiObjectNotFoundException, IOException {
		activity.launchActivity(null);
		captureImage.allowPermissions();
		Uri fakeUri = captureImage.createFakeImage(temp.newFile(), Color.RED);
		captureImage.intendExternalChooser(fakeUri);
		captureImage.pick();
		captureImage.verifyExternalChooser();
		Intents.assertNoUnverifiedIntents();
		captureImage.verifyImageColor(equalTo(Color.RED));
	}
}
