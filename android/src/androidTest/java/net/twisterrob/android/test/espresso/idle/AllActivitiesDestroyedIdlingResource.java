package net.twisterrob.android.test.espresso.idle;

import java.util.*;

import android.app.Activity;
import android.support.test.runner.lifecycle.*;

public class AllActivitiesDestroyedIdlingResource extends AsyncIdlingResource {
	private Collection<Activity> activities = new HashSet<>();
	private ActivityLifecycleCallback callback = new ActivityLifecycleCallback() {
		@Override public void onActivityLifecycleChanged(Activity activity, Stage stage) {
			if (stage == Stage.DESTROYED) {
				activities.remove(activity);
				if (isIdleCore()) {
					monitor.removeLifecycleCallback(callback);
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
}
