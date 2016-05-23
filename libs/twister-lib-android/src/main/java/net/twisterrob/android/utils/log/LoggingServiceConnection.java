package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.content.*;
import android.os.IBinder;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.AndroidTools;

public class LoggingServiceConnection implements ServiceConnection {
	private static final Logger LOG = LoggerFactory.getLogger("ServiceConnection");

	@Override public void onServiceConnected(ComponentName name, IBinder service) {
		LoggingHelper.log(LOG, AndroidTools.toNameString(this), "onServiceConnected", null, name, service);
	}
	@Override public void onServiceDisconnected(ComponentName name) {
		LoggingHelper.log(LOG, AndroidTools.toNameString(this), "onServiceDisconnected", null, name);
	}
}
