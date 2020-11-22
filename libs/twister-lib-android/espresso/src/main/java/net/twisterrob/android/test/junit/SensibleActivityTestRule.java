package net.twisterrob.android.test.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.app.Activity;
import android.content.*;
import android.util.Log;

import androidx.annotation.*;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.platform.app.InstrumentationRegistry;

import net.twisterrob.android.test.*;
import net.twisterrob.android.test.espresso.ScreenshotFailure;
import net.twisterrob.android.test.espresso.idle.AllActivitiesDestroyedIdlingResource;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

@SuppressWarnings("deprecation")
public class SensibleActivityTestRule<T extends Activity> extends androidx.test.rule.ActivityTestRule<T> {
	private static final String TAG = "ActivityTestRule";

	private final SystemAnimations systemAnimations;
	private final DeviceUnlocker unlocker;
	private final ChattyLogCat chatty;
	private Intent startIntent;

	public SensibleActivityTestRule(Class<T> activityClass) {
		this(activityClass, false);
	}
	public SensibleActivityTestRule(Class<T> activityClass, boolean initialTouchMode) {
		this(activityClass, initialTouchMode, true);
	}
	public SensibleActivityTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
		super(activityClass, initialTouchMode, launchActivity);
		Context context = InstrumentationRegistry.getInstrumentation().getContext();
		systemAnimations = new SystemAnimations(context);
		unlocker = new DeviceUnlocker(context);
		chatty = new ChattyLogCat();
	}

	// Note: with the base = foo.apply(base) pattern, these will be executed in reverse when evaluate() is called.
	@Override public Statement apply(Statement base, Description description) {
		// Inner of ActivityTestRule, because it needs to take a screenshot before a failure finishes the activity.
		base = new ScreenshotFailure().apply(base, description);
		// This needs to be right before the super call, so it is the immediate inner of ActivityTestRule.
		base = new TestLogger().apply(base, description);
		base = super.apply(base, description);
		// Anything from above will be wrapped inside the name shortener so that all exceptions are cleaned.
		//base = new PackageNameShortener().apply(base, description); // TODO make it available
		return base;
	}

	public @NonNull Intent getStartIntent() {
		return startIntent;
	}

	/**
	 * Makes sure we have the intent passed in to launchActivity as a non-null and keep the reference to it.
	 * This is the only way to capture what the actual launched intent was,
	 * so in {@link #beforeActivityLaunched()} we can set up the intent extras via {@link #getStartIntent()}.
	 */
	@Override public T launchActivity(@Nullable Intent startIntent) {
		Intent intent = startIntent;
		if (intent == null) {
			intent = getActivityIntent();
		}
		if (intent == null) {
			intent = new Intent();
		}
		this.startIntent = intent;
		return super.launchActivity(this.startIntent);
	}

	@CallSuper
	@Override protected void beforeActivityLaunched() {
		chatty.saveBlackWhiteList();
		chatty.iAmNotChatty();
		waitForEverythingToDestroy();
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
		waitForEverythingToDestroy();
		super.afterActivityFinished();
		Intents.release();
		systemAnimations.restore();
		chatty.restoreLastBlackWhiteList();
	}

	protected void waitForEverythingToDestroy() {
		AllActivitiesDestroyedIdlingResource.finishAll();
		AllActivitiesDestroyedIdlingResource activities = new AllActivitiesDestroyedIdlingResource();
		IdlingRegistry.getInstance().register(activities);
		try {
			waitForIdleSync();
		} finally {
			IdlingRegistry.getInstance().unregister(activities);
		}
	}

	protected void waitForIdleSync() {
		try {
			runOnUiThread(new Runnable() {
				@Override public void run() {
					getUIControllerHack().loopMainThreadUntilIdle();
				}
			});
		} catch (Throwable ex) {
			androidx.test.espresso.core.internal.deps.guava.base.Throwables.throwIfUnchecked(ex);
		}
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
