package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.content.*;
import android.os.IBinder;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class LoggingServiceConnection implements ServiceConnection {
	private static final Logger LOG = LoggerFactory.getLogger("ServiceConnection");

	@Override public void onServiceConnected(ComponentName name, IBinder service) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), "onServiceConnected", null, name, service);
	}
	@Override public void onServiceDisconnected(ComponentName name) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), "onServiceDisconnected", null, name);
	}
}
