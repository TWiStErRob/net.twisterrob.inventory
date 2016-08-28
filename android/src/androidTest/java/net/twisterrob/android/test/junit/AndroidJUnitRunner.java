package net.twisterrob.android.test.junit;

import android.os.*;
import android.os.StrictMode.ThreadPolicy;

public class AndroidJUnitRunner extends android.support.test.runner.AndroidJUnitRunner {
	@Override public void onCreate(Bundle arguments) {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			/* StrictMode: read needed because of this:
			StrictMode policy violation; ~duration=122 ms: android.os.StrictMode$StrictModeDiskReadViolation: policy=2815 violation=2
				at android.os.StrictMode$AndroidBlockGuardPolicy.onReadFromDisk(StrictMode.java:1137)
				at libcore.io.BlockGuardOs.access(BlockGuardOs.java:67)
				at java.io.File.doAccess(File.java:283)
				at java.io.File.exists(File.java:363)
				at android.app.ContextImpl.getDir(ContextImpl.java:2668)
				at android.support.test.runner.MonitoringInstrumentation.specifyDexMakerCacheProperty(MonitoringInstrumentation.java:187)
				at android.support.test.runner.MonitoringInstrumentation.onCreate(MonitoringInstrumentation.java:152)
				at android.support.test.runner.AndroidJUnitRunner.onCreate(AndroidJUnitRunner.java:209)
			*/
			/* StrictMode: write needed because of this:
			StrictMode policy violation; ~duration=79 ms: android.os.StrictMode$StrictModeDiskWriteViolation: policy=2813 violation=1
				at android.os.StrictMode$AndroidBlockGuardPolicy.onWriteToDisk(StrictMode.java:1111)
				at libcore.io.BlockGuardOs.mkdir(BlockGuardOs.java:172)
				at java.io.File.mkdirErrno(File.java:874)
				at java.io.File.mkdir(File.java:865)
				at android.app.ContextImpl.getDir(ContextImpl.java:2669)
				at android.support.test.runner.MonitoringInstrumentation.specifyDexMakerCacheProperty(MonitoringInstrumentation.java:187)
				at android.support.test.runner.MonitoringInstrumentation.onCreate(MonitoringInstrumentation.java:152)
				at android.support.test.runner.AndroidJUnitRunner.onCreate(AndroidJUnitRunner.java:209)
			 */
			super.onCreate(arguments);
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}
}
