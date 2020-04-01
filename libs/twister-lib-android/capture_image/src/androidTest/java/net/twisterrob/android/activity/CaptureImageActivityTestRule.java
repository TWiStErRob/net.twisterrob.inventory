package net.twisterrob.android.activity;

import java.io.File;

import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.content.Intent;
import android.support.test.espresso.IdlingRegistry;

import net.twisterrob.android.test.espresso.idle.GlideIdlingResource;
import net.twisterrob.android.test.junit.SensibleActivityTestRule;

/**
 * <ul>
 *     <li>Provides control over when the activity is launched.
 *         Must call {@code activity.launchActivity(null)}.</li>
 *     <li>Provides an automatic temp folder.</li>
 *     <li>Provides an output file in temp folder.</li>
 *     <li>Provides a Glide idling resources for preview synchronization</li>
 * </ul>
 */
public class CaptureImageActivityTestRule extends SensibleActivityTestRule<CaptureImage> {

	private File outputFile;
	private final GlideIdlingResource glideIdler = new GlideIdlingResource();
	private final TemporaryFolder temp = new TemporaryFolder() {
		@Override protected void before() throws Throwable {
			super.before();
			outputFile = new File(getRoot(), "output.file");
		}
	};

	public CaptureImageActivityTestRule() {
		super(CaptureImage.class, true, false);
	}

	public File getOutputFile() {
		return outputFile;
	}

	public TemporaryFolder getTemp() {
		return temp;
	}

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
	@Override public Statement apply(Statement base, Description description) {
		base = super.apply(base, description);
		// Wrap activity rule in temp so that folder is available throughout.
		base = temp.apply(base, description);
		return base;
	}
}
