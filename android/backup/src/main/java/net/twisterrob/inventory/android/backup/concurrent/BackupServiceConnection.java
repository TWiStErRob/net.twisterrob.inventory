package net.twisterrob.inventory.android.backup.concurrent;

import android.app.Service;
import android.content.*;
import android.os.IBinder;

import androidx.annotation.NonNull;

import net.twisterrob.inventory.android.backup.concurrent.BackupService.*;

public abstract class BackupServiceConnection implements ServiceConnection, BackupListener {
	private LocalBinder binding;
	private Context context;
	/**
	 * {@link #onServiceDisconnected(ComponentName)} is only called when service stops itself or other extreme case.
	 * There may be others bound to the same service, in which case no-one is getting disconnected,
	 * because the service is still live and well due to the bindings.
	 * In that case we need manually disconnect to have a consistent callback for unbind.
	 */
	private boolean onServiceDisconnectedCalled;
	private Intent serviceIntent;

	public LocalBinder getBinding() {
		return binding;
	}

	@Override public final void onServiceConnected(ComponentName name, IBinder service) {
		binding = (LocalBinder)service;
		binding.addBackupListener(this);
		serviceBound(name, binding);
	}
	/** {@link Context#registerReceiver(BroadcastReceiver, IntentFilter)} should be called first inside this method. */

	protected void serviceBound(ComponentName name, LocalBinder service) {
		// optional operation
	}

	@Override public final void onServiceDisconnected(ComponentName name) {
		onServiceDisconnectedCalled = true;
		serviceUnbound(name, binding);
		binding.removeBackupListener(this);
		binding = null;
	}
	/** {@link Context#unregisterReceiver(BroadcastReceiver)} should be called last inside this method. */
	protected void serviceUnbound(ComponentName name, LocalBinder service) {
		// optional operation
	}

	@Override public void started() {
		// optional operation
	}
	@Override public void finished() {
		// optional operation
	}

	public void bind(@NonNull Context context) {
		this.context = context;
		this.serviceIntent = new Intent(context, BackupService.class);
		// automatically create the service to be able to query the binder if there's something in progress
		// bind in the background so hopefully the UI still gets more CPU than the export
		int flags = Service.BIND_AUTO_CREATE | Service.BIND_NOT_FOREGROUND;
		context.bindService(serviceIntent, this, flags);
	}

	public void unbind() {
		onServiceDisconnectedCalled = false;
		context.unbindService(this);
		if (!onServiceDisconnectedCalled) {
			if (binding != null) {
				// unbindService is safe to be called without getting connected first, but this is not
				onServiceDisconnected(serviceIntent.getComponent());
			}
		}
		this.context = null;
		this.serviceIntent = null;
	}
}
