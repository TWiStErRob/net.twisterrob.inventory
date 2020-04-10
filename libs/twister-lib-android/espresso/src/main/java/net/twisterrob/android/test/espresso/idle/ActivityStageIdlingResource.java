package net.twisterrob.android.test.espresso.idle;

import android.app.Activity;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.runner.lifecycle.*;

import net.twisterrob.android.test.espresso.EspressoExtensions;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class ActivityStageIdlingResource extends AsyncIdlingResource {
	private final ActivityLifecycleCallback callback = new ActivityLifecycleCallback() {
		@Override public void onActivityLifecycleChanged(Activity activity, Stage stage) {
			if (activity == ActivityStageIdlingResource.this.activity
					&& idleStageCallback.isIdleStage(stage)) {
				monitor.removeLifecycleCallback(callback);
				transitionToIdle();
			}
		}
	};

	private final ActivityLifecycleMonitor monitor;
	private final Activity activity;
	private final IdleStageCallback idleStageCallback;

	public ActivityStageIdlingResource(
			Activity activity,
			IdleStageCallback idleStageCallback) {
		this.activity = activity;
		this.idleStageCallback = idleStageCallback;
		this.monitor = ActivityLifecycleMonitorRegistry.getInstance();
	}

	@Override public String getName() {
		return "ActivityStageIdlingResource[" + activity + " " + idleStageCallback.describe() + "]";
	}

	private boolean isIdleCore() {
		Stage currentStage = monitor.getLifecycleStageOf(activity);
		return idleStageCallback.isIdleStage(currentStage);
	}

	@Override protected boolean isIdle() {
		monitor.removeLifecycleCallback(callback);
		return isIdleCore();
	}

	@Override protected void waitForIdleAsync() {
		monitor.addLifecycleCallback(callback);
	}

	public interface IdleStageCallback {
		boolean isIdleStage(Stage stage);
		String describe();
	}

	public static ActivityStageIdlingResource waitForAtMost(Activity activity, final Stage stage) {
		return new ActivityStageIdlingResource(activity, new IdleStageCallback() {
			@Override public String describe() {
				return "at most " + stage;
			}
			@Override public boolean isIdleStage(Stage currentStage) {
				return currentStage.compareTo(stage) <= 0;
			}
		});
	}

	public static ActivityStageIdlingResource waitForAtLeast(Activity activity, final Stage stage) {
		return new ActivityStageIdlingResource(activity, new IdleStageCallback() {
			@Override public String describe() {
				return "at least " + stage;
			}
			@Override public boolean isIdleStage(Stage currentStage) {
				return currentStage.compareTo(stage) >= 0;
			}
		});
	}

	public static void waitForAtLeastNow(Activity activity, Stage stage) {
		ActivityStageIdlingResource resource =
				ActivityStageIdlingResource.waitForAtLeast(activity, stage);
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
