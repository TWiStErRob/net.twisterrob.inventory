package net.twisterrob.android.test.junit;

import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.test.*;

public class SensibleActivityTestRule<T extends Activity> extends ActivityTestRule<T> {
	private final SystemAnimations systemAnimations;
	private final DeviceUnlocker unlocker;

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

	@Override protected void beforeActivityLaunched() {
		systemAnimations.backup();
		systemAnimations.disableAll();
		Intents.init();
		unlocker.wakeUpWithDisabledKeyguard();
		super.beforeActivityLaunched();
	}

	@Override protected void afterActivityLaunched() {
		super.afterActivityLaunched();
	}

	@Override protected void afterActivityFinished() {
		super.afterActivityFinished();
		Intents.release();
		systemAnimations.restore();
	}

	public static Stage getActivityStage(final ActivityTestRule<?> activity) {
		getInstrumentation().waitForIdleSync();
		final AtomicReference<Stage> stage = new AtomicReference<>();
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				Stage currentStage = ActivityLifecycleMonitorRegistry.getInstance().
						getLifecycleStageOf(activity.getActivity());
				stage.set(currentStage);
			}
		});
		return stage.get();
	}
}
