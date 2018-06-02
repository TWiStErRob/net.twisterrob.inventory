package net.twisterrob.android.utils.tostring.stringers.detailed.activitymanager;

import javax.annotation.Nonnull;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Build.*;

import net.twisterrob.android.annotation.TrimMemoryLevel.Converter;
import net.twisterrob.java.utils.tostring.*;

public class RunningAppProcessInfoStringer extends Stringer<ActivityManager.RunningAppProcessInfo> {
	@Override public void toString(@Nonnull ToStringAppender append, RunningAppProcessInfo info) {
		append.identity(info.pid, info.processName);
		append.rawProperty("uid", info.uid);
		append.rawProperty("lru", info.lru);
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			append.rawProperty("lastTrimLevel", Converter.toString(info.lastTrimLevel));
		}
		append.beginPropertyGroup("importance");
		append.rawProperty("level", info.importance);
		append.rawProperty("reason", info.importanceReasonCode);
		append.rawProperty("component", info.importanceReasonComponent);
		append.rawProperty("pid", info.importanceReasonPid);
		append.endPropertyGroup();
		append.beginSizedList("pkgList", info.pkgList.length);
		for (int i = 0; i < info.pkgList.length; i++) {
			append.item(i, info.pkgList[i]);
		}
		append.endSizedList();
	}
}
