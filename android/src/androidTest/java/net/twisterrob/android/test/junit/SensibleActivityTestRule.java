package net.twisterrob.android.test.junit;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;

import net.twisterrob.android.test.*;
import net.twisterrob.android.test.espresso.ScreenshotFailure;

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

	@Override public Statement apply(Statement base, Description description) {
		base = super.apply(base, description);
		//base = new PackageNameShortener().apply(base, description); // TODO make it available
		base = new ScreenshotFailure().apply(base, description);
		return base;
	}

	@Override protected void beforeActivityLaunched() {
		systemAnimations.backup();
		systemAnimations.disableAll();
		unlocker.wakeUpWithDisabledKeyguard();
		super.beforeActivityLaunched();
	}

	@Override protected void afterActivityLaunched() {
		super.afterActivityLaunched();
		Intents.init();
	}

	@Override protected void afterActivityFinished() {
		super.afterActivityFinished();
		Intents.release();
		systemAnimations.restore();
	}
}
