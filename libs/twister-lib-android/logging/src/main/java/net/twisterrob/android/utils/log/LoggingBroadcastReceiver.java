package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.content.*;
import android.os.IBinder;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;

public class LoggingBroadcastReceiver extends BroadcastReceiver {
	private static final Logger LOG = LoggerFactory.getLogger("BroadcastReceiver");

	public LoggingBroadcastReceiver() {
		log("<ctor>");
	}
	@Override public IBinder peekService(Context myContext, Intent service) {
		log("peekService", myContext, service);
		return super.peekService(myContext, service);
	}
	@Override public void onReceive(Context context, Intent intent) {
		log("onReceive", context, intent);
	}
	private void log(String method, Object... args) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), method, null, args);
	}
}
