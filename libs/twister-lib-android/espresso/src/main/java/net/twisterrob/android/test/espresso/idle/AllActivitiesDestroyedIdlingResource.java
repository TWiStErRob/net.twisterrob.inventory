package net.twisterrob.android.test.espresso.idle;

import java.util.*;

import android.app.Activity;
import android.os.*;
import android.support.annotation.*;
import android.support.test.runner.lifecycle.*;

import static android.os.Build.*;

import net.twisterrob.android.test.junit.InstrumentationExtensions;

public class AllActivitiesDestroyedIdlingResource extends AsyncIdlingResource {

	private static final Collection<Stage> IDLE_STAGES = new HashSet<>();

	static {
		IDLE_STAGES.add(Stage.DESTROYED);
		if (VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH) {
			// On API 10 finish() kills the activity, and calls onDestroy properly,
			// but android.app.ActivityThread.performDestroyActivity directly calls onDestroy,
			// instead of going through android.app.Instrumentation.callActivityOnDestroy,
			// which means MonitoringInstrumentation won't call signalLifecycleChange(DESTROYED),
			// so while the activity is destroyed it's stuck in STOPPED stage.
			IDLE_STAGES.add(Stage.STOPPED);
		}
	}

	private final Collection<Activity> activities = new HashSet<>();
	private final ActivityLifecycleCallback callback = new ActivityLifecycleCallback() {
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
			if (!IDLE_STAGES.contains(stage)) {
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
			if (!activity.isFinishing()) {
				// Conditionally try to finish stale activity. Prevents showing this log:
				// system_process W/ActivityTaskManager:
				// Duplicate finish request for ActivityRecord{xxxxxxxx u0 pack.age/pack.ClassName t0000 f}
				activity.finish();
				// Note: If the activity is finished by the @Test, ActivityRule still calls .finish(),
				// so the "Duplicate finish request" will still show up.
			}
		}
	}

	@Override public @NonNull String toString() {
		StringBuilder sb = new StringBuilder();
		if (activities.isEmpty()) {
			sb.append("No activities");
		} else {
			for (Activity activity : activities) {
				sb.append(activity.toString());
				sb.append(": ");
				sb.append(InstrumentationExtensions.getActivityStage(activity));
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
