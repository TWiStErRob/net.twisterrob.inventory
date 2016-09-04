package net.twisterrob.android.test.automators;

import java.util.*;
import java.util.concurrent.TimeUnit;

import android.app.*;
import android.app.Instrumentation.ActivityMonitor;
import android.content.IntentFilter;
import android.support.test.runner.lifecycle.Stage;

import net.twisterrob.android.test.junit.InstrumentationExtensions;

// FIXME replace with ActivityLifecycleMonitorRegistry.getInstance()?
public class ActivityCollector {
	private final Instrumentation instrumentation;
	private final ActivityMonitor monitor = new ActivityMonitor(new IntentFilter(), null, false);
	private final LinkedHashSet<Activity> activities = new LinkedHashSet<>();
	private final Thread thread = new Thread(new Runnable() {
		@Override public void run() {
			while (true) {
				// monitor.waitForActivity() cannot be interrupted because of the while loop inside it
				Activity activity = monitor.waitForActivityWithTimeout(TimeUnit.MINUTES.toMillis(10));
				if (activity != null) {
					recordActivity(activity);
				} else {
					break;
				}
			}
		}
	}, ActivityCollector.class.getSimpleName());

	public ActivityCollector(Instrumentation instrumentation) {
		this.instrumentation = instrumentation;
		this.thread.setDaemon(true);
	}

	public void start() {
		instrumentation.addMonitor(monitor);
		thread.start();
	}
	public void stop() {
		thread.interrupt();
		instrumentation.removeMonitor(monitor);
	}
	private synchronized void recordActivity(Activity activity) {
		activities.add(activity);
	}
	public synchronized Collection<Activity> getActivities() {
		return Collections.unmodifiableCollection(activities);
	}
	public synchronized Activity getLatestResumed() {
		Activity activity = null;
		for (Activity seen : activities) {
			Stage stage = InstrumentationExtensions.getActivityStage(seen);
			if (stage == Stage.RESUMED) {
				activity = seen;
			}
		}
		return activity;
	}
	@Override public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (Activity activity : activities) {
			Stage stage = InstrumentationExtensions.getActivityStage(activity);
			sb.append(stage).append(": ").append(activity).append(", ");
		}
		if (!activities.isEmpty()) {
			sb.delete(sb.length() - ", ".length(), sb.length());
		}
		sb.append(']');
		return sb.toString();
	}
}
