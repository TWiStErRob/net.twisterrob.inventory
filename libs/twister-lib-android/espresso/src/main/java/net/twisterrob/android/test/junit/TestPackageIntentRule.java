package net.twisterrob.android.test.junit;

import android.app.Activity;
import android.content.*;
import android.support.test.InstrumentationRegistry;

public class TestPackageIntentRule<T extends Activity> extends SensibleActivityTestRule<T> {
	private final Context testContext;
	/**
	 * @param activityClass must be registered in androidTest/AndroidManifest.xml and process="packageUnderTest" set.
	 */
	public TestPackageIntentRule(Class<T> activityClass) {
		super(activityClass);
		testContext = InstrumentationRegistry.getInstrumentation().getContext();
	}
	@Override protected Intent getActivityIntent() {
		// Using the test package's context instead of target context and not letting anyone change that.
		return new ForcedPackageIntent(testContext);
	}
}
