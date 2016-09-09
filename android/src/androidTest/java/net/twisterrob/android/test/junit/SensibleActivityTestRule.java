package net.twisterrob.android.test.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.app.Activity;
import android.content.*;
import android.support.annotation.*;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import net.twisterrob.android.test.*;
import net.twisterrob.android.test.espresso.ScreenshotFailure;

public class SensibleActivityTestRule<T extends Activity> extends ActivityTestRule<T> {
	private static final String TAG = "ActivityTestRule";

	private final SystemAnimations systemAnimations;
	private final DeviceUnlocker unlocker;
	private Intent startIntent;

	public SensibleActivityTestRule(Class<T> activityClass) {
		this(activityClass, false);
	}
	public SensibleActivityTestRule(Class<T> activityClass, boolean initialTouchMode) {
		this(activityClass, initialTouchMode, true);
	}
	public SensibleActivityTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
		super(activityClass, initialTouchMode, launchActivity);
		Context context = InstrumentationRegistry.getContext();
		systemAnimations = new SystemAnimations(context);
		unlocker = new DeviceUnlocker(context);
	}

	@Override public Statement apply(Statement base, Description description) {
		base = new TestLogger().apply(base, description);
		base = super.apply(base, description);
		//base = new PackageNameShortener().apply(base, description); // TODO make it available
		base = new ScreenshotFailure().apply(base, description);
		return base;
	}

	public Intent getStartIntent() {
		return startIntent;
	}

	@Override public T launchActivity(@Nullable Intent startIntent) {
		this.startIntent = startIntent;
		return super.launchActivity(startIntent);
	}

	@CallSuper
	@Override protected void beforeActivityLaunched() {
		systemAnimations.backup();
		systemAnimations.disableAll();
		unlocker.wakeUpWithDisabledKeyguard();
		Log.i("ViewInteraction", "Launching activity at the beginning of test.");
		super.beforeActivityLaunched();
	}

	@CallSuper
	@Override protected void afterActivityLaunched() {
		Log.d(TAG, "Activity launched at the beginning of test.");
		super.afterActivityLaunched();
		Intents.init();
	}

	@CallSuper
	@Override protected void afterActivityFinished() {
		super.afterActivityFinished();
		Intents.release();
		systemAnimations.restore();
	}

	/**
	 * This needs to be applied right inside {@link ActivityTestRule.ActivityStatement}
	 * so the logging happens at the correct time.
	 */
	private static class TestLogger implements TestRule {
		@Override public Statement apply(final Statement base, Description description) {
			return new Statement() {
				@Override public void evaluate() throws Throwable {
					try {
						base.evaluate();
					} finally {
						Log.i(TAG, "Finishing activity at the end of test.");
					}
				}
			};
		}
	}
}
