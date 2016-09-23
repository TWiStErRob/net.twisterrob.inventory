package net.twisterrob.inventory.android.test;

import android.content.*;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.LocalBroadcastManager;

import net.twisterrob.android.test.espresso.idle.IntentServiceIdlingResource;
import net.twisterrob.inventory.android.content.db.DatabaseService;

public class DatabaseServiceIdlingResource extends IntentServiceIdlingResource {
	private final BroadcastReceiver onShutdown = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			if (DatabaseService.class.getName().equals(intent.getComponent().getClassName())) {
				transitionToIdle();
			}
		}
	};

	public DatabaseServiceIdlingResource() {
		super(DatabaseService.class);
	}

	@Override protected void waitForIdleAsync() {
		super.waitForIdleAsync();
		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(InstrumentationRegistry.getTargetContext());
		bm.registerReceiver(onShutdown, new IntentFilter(DatabaseService.ACTION_SERVICE_SHUTDOWN));
	}

	@Override protected void transitionToIdle() {
		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(InstrumentationRegistry.getTargetContext());
		bm.unregisterReceiver(onShutdown);
		super.transitionToIdle();
	}
}
