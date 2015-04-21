package net.twisterrob.inventory.android.activity.space;

import android.app.*;
import android.content.Context;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;

abstract class CleanTask extends SimpleSafeAsyncTask<Activity, Void, Void> implements NoProgressTaskExecutor.UITask {
	private TaskEndListener callbacks;

	@Override public void setCallbacks(TaskEndListener callbacks) {
		this.callbacks = callbacks;
	}

	@Override public void execute(Activity activity) {
		super.execute(activity);
	}

	protected abstract void doClean();

	@Override protected Void doInBackground(Activity activity) throws Exception {
		killProcessesAround(activity);
		doClean();
		killProcessesAround(activity);
		return null;
	}

	@Override protected void onResult(Void aVoid, Activity activity) {
		callbacks.taskDone();
	}

	@Override protected void onError(@NonNull Exception ex, Activity activity) {
		callbacks.taskDone();
	}

	private static void killProcessesAround(Activity activity) {
		ActivityManager am = (ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE);
		String myProcessPrefix = activity.getApplicationInfo().processName;
		String myProcessName = AndroidTools.getActivityInfo(activity, 0).processName;
		for (ActivityManager.RunningAppProcessInfo proc : am.getRunningAppProcesses()) {
			if (proc.processName.startsWith(myProcessPrefix) && !proc.processName.equals(myProcessName)) {
				android.os.Process.killProcess(proc.pid);
			}
		}
	}
}
