package net.twisterrob.inventory.android.test;

import android.content.*;

import androidx.test.core.app.ApplicationProvider;

import net.twisterrob.android.test.espresso.idle.IntentServiceIdlingResource;
import net.twisterrob.inventory.android.content.db.DatabaseService;

import static net.twisterrob.inventory.android.content.BroadcastTools.getLocalBroadcastManager;

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
		getLocalBroadcastManager(ApplicationProvider.getApplicationContext())
				.registerReceiver(onShutdown, new IntentFilter(DatabaseService.ACTION_SERVICE_SHUTDOWN));
	}

	@Override protected void transitionToIdle() {
		getLocalBroadcastManager(ApplicationProvider.getApplicationContext())
				.unregisterReceiver(onShutdown);
		super.transitionToIdle();
	}
}
