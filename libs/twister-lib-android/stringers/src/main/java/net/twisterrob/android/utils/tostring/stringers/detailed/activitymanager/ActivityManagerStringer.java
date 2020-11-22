package net.twisterrob.android.utils.tostring.stringers.detailed.activitymanager;

import java.util.List;

import javax.annotation.Nonnull;

import android.app.ActivityManager;
import android.app.ActivityManager.*;

import net.twisterrob.android.annotation.*;
import net.twisterrob.java.utils.tostring.*;

@SuppressWarnings("deprecation")
public class ActivityManagerStringer extends Stringer<ActivityManager> {
	@Override public void toString(@Nonnull ToStringAppender append, ActivityManager am) {
		// TODO am.dumpPackageState();
		append.beginPropertyGroup("memory");
		append.measuredProperty("class", "MiB", am.getMemoryClass());
		append.measuredProperty("largeClass", "MiB", am.getLargeMemoryClass());
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			append.booleanProperty(am.isLowRamDevice(), "low RAM device");
		}
		MemoryInfo memory = new MemoryInfo();
		am.getMemoryInfo(memory);
		append.complexProperty("memoryInfo", memory);
		append.endPropertyGroup();

		append.beginPropertyGroup("config");
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			append.complexProperty("task thumbnail", am.getAppTaskThumbnailSize());
		}
		append.measuredProperty("launcher large icon", "px", am.getLauncherLargeIconSize());
		append.beginPropertyGroup("launcher large icon density");
		append.rawProperty("dpi", am.getLauncherLargeIconDensity());
		//noinspection WrongConstant
		append.selfDescribingProperty(Density.Converter.toString(am.getLauncherLargeIconDensity()));
		append.endPropertyGroup();
		append.complexProperty("deviceConfigurationInfo", am.getDeviceConfigurationInfo());
		append.endPropertyGroup();

		append.beginPropertyGroup("lock task mode");
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			append.booleanProperty(am.isInLockTaskMode(), "on", "off");
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			//noinspection WrongConstant
			append.selfDescribingProperty(LockTaskMode.Converter.toString(am.getLockTaskModeState()));
		}
		append.endPropertyGroup();

		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
		if (runningAppProcesses != null) {
			list(append, "runningAppProcesses", runningAppProcesses);
		}
		List<ProcessErrorStateInfo> processesInErrorState = am.getProcessesInErrorState();
		if (processesInErrorState != null) {
			list(append, "processesInErrorState", processesInErrorState);
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			list(append, "appTasks", am.getAppTasks());
		}
		list(append, "recentTasks", am.getRecentTasks(Integer.MAX_VALUE, 0));
		list(append, "runningTasks", am.getRunningTasks(Integer.MAX_VALUE));
		list(append, "runningServices", am.getRunningServices(Integer.MAX_VALUE));
	}
	private void list(@Nonnull ToStringAppender append, String name, List<?> recentTasks) {
		append.beginSizedList(name, recentTasks.size());
		for (int i = 0; i < recentTasks.size(); i++) {
			append.item(i, recentTasks.get(i));
		}
		append.endSizedList();
	}
}
