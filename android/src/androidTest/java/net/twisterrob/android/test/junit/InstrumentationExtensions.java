package net.twisterrob.android.test.junit;

import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.*;

public class InstrumentationExtensions {
	public static Stage getActivityStage(ActivityTestRule<?> activity) {
		return getActivityStage(activity.getActivity());
	}
	public static Stage getActivityStage(final Activity activity) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
		}
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();
		final AtomicReference<Stage> stage = new AtomicReference<>();
		InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				Stage currentStage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
				stage.set(currentStage);
			}
		});
		return stage.get();
	}
}
