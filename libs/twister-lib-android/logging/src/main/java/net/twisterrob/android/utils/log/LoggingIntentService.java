package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import androidx.annotation.*;

import net.twisterrob.android.annotation.TrimMemoryLevel;
import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
@SuppressLint("Registered") // allow registration if wanted without needing to subclass
public class LoggingIntentService extends IntentService {
	private static final Logger LOG = LoggerFactory.getLogger("IntentService");
	private final String name;

	public LoggingIntentService(String name) {
		super(name);
		this.name = name;
		log("<ctor>");
	}
	@Override public void setIntentRedelivery(boolean enabled) {
		log("setIntentRedelivery", enabled);
		super.setIntentRedelivery(enabled);
	}
	@Override public void onCreate() {
		log("onCreate");
		super.onCreate();
	}
	@Override public void onStart(@Nullable Intent intent, int startId) {
		log("onStart", intent, startId);
		super.onStart(intent, startId);
	}
	@Override public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		log("onStartCommand", intent, flags, startId);
		return super.onStartCommand(intent, flags, startId);
	}
	@Override public void onDestroy() {
		log("onDestroy");
		super.onDestroy();
	}
	@Override public @Nullable IBinder onBind(Intent intent) {
		log("onBind", intent);
		return super.onBind(intent);
	}
	@Override public void onConfigurationChanged(Configuration newConfig) {
		log("onConfigurationChanged", newConfig);
		super.onConfigurationChanged(newConfig);
	}
	@Override public void onLowMemory() {
		log("onLowMemory");
		super.onLowMemory();
	}
	@Override public void onTrimMemory(@TrimMemoryLevel int level) {
		log("onTrimMemory", StringerTools.toTrimMemoryString(level));
		super.onTrimMemory(level);
	}
	@Override public boolean onUnbind(Intent intent) {
		log("onUnbind", intent);
		return super.onUnbind(intent);
	}
	@Override public void onRebind(Intent intent) {
		log("onRebind", intent);
		super.onRebind(intent);
	}
	@Override public void onTaskRemoved(Intent rootIntent) {
		log("onTaskRemoved", rootIntent);
		super.onTaskRemoved(rootIntent);
	}
	@Override protected void onHandleIntent(@Nullable Intent intent) {
		log("onHandleIntent", intent);
	}
	private void log(@NonNull String method, @NonNull Object... args) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this) + "[" + name + "]", method, null, args);
	}
}
