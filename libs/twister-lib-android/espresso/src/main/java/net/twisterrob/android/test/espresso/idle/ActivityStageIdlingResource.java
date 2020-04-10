package net.twisterrob.android.test.espresso.idle;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.StringDescription.*;

import android.app.Activity;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.runner.lifecycle.*;

import net.twisterrob.android.test.espresso.EspressoExtensions;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class ActivityStageIdlingResource extends AsyncIdlingResource {
	private final ActivityLifecycleCallback callback = new ActivityLifecycleCallback() {
		@Override public void onActivityLifecycleChanged(Activity activity, Stage stage) {
			if (activity == ActivityStageIdlingResource.this.activity
					&& stageMatcher.matches(stage)) {
				monitor.removeLifecycleCallback(callback);
				transitionToIdle();
			}
		}
	};

	private final ActivityLifecycleMonitor monitor;
	private final Activity activity;
	private final Matcher<Stage> stageMatcher;

	public ActivityStageIdlingResource(
			Activity activity,
			Matcher<Stage> stageMatcher) {
		this.activity = activity;
		this.stageMatcher = stageMatcher;
		this.monitor = ActivityLifecycleMonitorRegistry.getInstance();
	}

	@Override public String getName() {
		return "ActivityStageIdlingResource[" + activity + " " + asString(stageMatcher) + "]";
	}

	private boolean isIdleCore() {
		Stage currentStage = monitor.getLifecycleStageOf(activity);
		return stageMatcher.matches(currentStage);
	}

	@Override protected boolean isIdle() {
		monitor.removeLifecycleCallback(callback);
		return isIdleCore();
	}

	@Override protected void waitForIdleAsync() {
		monitor.addLifecycleCallback(callback);
	}

	public static void waitForAtLeast(Activity activity, Stage stage) {
		ActivityStageIdlingResource resource =
				new ActivityStageIdlingResource(activity, greaterThanOrEqualTo(stage));
		IdlingRegistry.getInstance().register(resource);
		try {
			// TODO AndroidX, onIdle doesn't sync registry, so it doesn't wait for the just-registered resource
			//Espresso.onIdle();
			onRoot().perform(EspressoExtensions.loopMainThreadUntilIdle());
		} finally {
			IdlingRegistry.getInstance().unregister(resource);
		}
	}
}
