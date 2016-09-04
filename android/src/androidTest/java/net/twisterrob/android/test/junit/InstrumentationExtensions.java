package net.twisterrob.android.test.junit;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.os.Looper;
import android.support.annotation.*;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.*;

import static android.support.test.espresso.core.deps.guava.base.Throwables.*;
import static android.support.test.espresso.core.deps.guava.collect.Iterables.*;

public class InstrumentationExtensions {
	public static @NonNull Stage getActivityStage(@NonNull ActivityTestRule<?> activity) {
		return getActivityStage(activity.getActivity());
	}
	public static @NonNull Stage getActivityStage(final @NonNull Activity activity) {
		return runOnMainIfNecessary(new Callable<Stage>() {
			@Override public Stage call() throws Exception {
				return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
			}
		});
	}
	public static @NonNull Collection<Activity> getActivitiesInStage(final @NonNull Stage stage) {
		return runOnMainIfNecessary(new Callable<Collection<Activity>>() {
			@Override public Collection<Activity> call() throws Exception {
				return ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage);
			}
		});
	}
	public static @NonNull Collection<Activity> getAllActivities() {
		return runOnMainIfNecessary(new Callable<Collection<Activity>>() {
			@Override public Collection<Activity> call() throws Exception {
				ActivityLifecycleMonitor monitor = ActivityLifecycleMonitorRegistry.getInstance();
				List<Activity> activities = new ArrayList<>();
				for (Stage stage : Stage.values()) {
					activities.addAll(monitor.getActivitiesInStage(stage));
				}
				return activities;
			}
		});
	}

	/**
	 * @throws NoSuchElementException if there's no such activity
	 * @throws IllegalArgumentException if there are multiple activities
	 */
	public static @NonNull Activity getActivityInStage(@NonNull Stage stage)
			throws NoSuchElementException, IllegalArgumentException {
		Collection<Activity> activities = getActivitiesInStage(stage);
		return getOnlyElement(activities);
	}

	/**
	 * @return {@code null} if there's no such activity
	 * @throws IllegalArgumentException if there are multiple activities
	 */
	public static @Nullable Activity tryGetActivityInStage(@NonNull Stage stage) throws IllegalArgumentException {
		Collection<Activity> activities = getActivitiesInStage(stage);
		return activities.isEmpty()? null : getOnlyElement(activities);
	}

	private static <T> T runOnMainIfNecessary(final @NonNull Callable<T> resultProvider) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			try {
				return resultProvider.call();
			} catch (Exception e) {
				propagateIfPossible(e);
				throw new RuntimeException("Unexpected exception", e);
			}
		}
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();
		final AtomicReference<T> ref = new AtomicReference<>();
		InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				try {
					ref.set(resultProvider.call());
				} catch (Exception e) {
					propagateIfPossible(e);
					throw new RuntimeException("Unexpected exception", e);
				}
			}
		});
		return ref.get();
	}
}
