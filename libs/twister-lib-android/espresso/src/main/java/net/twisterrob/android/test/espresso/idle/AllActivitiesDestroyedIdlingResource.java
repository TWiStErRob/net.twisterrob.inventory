package net.twisterrob.android.test.espresso.idle;

import java.util.*;

import android.app.Activity;
import android.os.*;
import android.support.annotation.AnyThread;
import android.support.test.runner.lifecycle.*;

import net.twisterrob.android.test.junit.InstrumentationExtensions;

public class AllActivitiesDestroyedIdlingResource extends AsyncIdlingResource {
	private Collection<Activity> activities = new HashSet<>();
	private ActivityLifecycleCallback callback = new ActivityLifecycleCallback() {
		@Override public void onActivityLifecycleChanged(Activity activity, Stage stage) {
			if (stage == Stage.DESTROYED) {
				activities.remove(activity);
				if (isIdleCore()) {
					// remove our listener in the next loop, otherwise we get a "Recursive looping detected!"
					// from android.support.test.espresso.base.UiControllerImpl.loopUntil
					// or a ConcurrentModificationException in the loop.
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override public void run() {
							monitor.removeLifecycleCallback(callback);
						}
					});
					transitionToIdle();
				}
			}
		}
	};

	private final ActivityLifecycleMonitor monitor;
	public AllActivitiesDestroyedIdlingResource() {
		monitor = ActivityLifecycleMonitorRegistry.getInstance();
	}

	@Override public String getName() {
		return "Death to all!";
	}

	private boolean isIdleCore() {
		return activities.isEmpty();
	}

	@Override protected boolean isIdle() {
		monitor.removeLifecycleCallback(callback);
		activities.clear();
		for (Stage stage : Stage.values()) {
			if (stage != Stage.DESTROYED) {
				activities.addAll(monitor.getActivitiesInStage(stage));
			}
		}
		return isIdleCore();
	}

	@Override protected void waitForIdleAsync() {
		monitor.addLifecycleCallback(callback);
	}

	@AnyThread
	public static void finishAll() {
		for (Activity activity : InstrumentationExtensions.getAllActivities()) {
			activity.finish();
		}
	}
}
