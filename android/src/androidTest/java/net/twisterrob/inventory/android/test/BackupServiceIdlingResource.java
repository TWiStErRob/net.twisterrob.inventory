package net.twisterrob.inventory.android.test;

import android.support.annotation.Nullable;

import net.twisterrob.android.test.espresso.idle.IntentServiceIdlingResource;
import net.twisterrob.inventory.android.backup.concurrent.BackupService;
import net.twisterrob.inventory.android.backup.concurrent.BackupService.*;

public class BackupServiceIdlingResource extends IntentServiceIdlingResource {
	private final BindingProvider provider;

	public interface BindingProvider {
		@Nullable LocalBinder getBinding();
	}
	public BackupServiceIdlingResource(BindingProvider provider) {
		super(BackupService.class);
		this.provider = provider;
	}
	@Override protected boolean isIdle() {
		return super.isIdle() || !isInProgress();
	}
	private boolean isInProgress() {
		LocalBinder binding = provider.getBinding();
		return binding != null && binding.isInProgress();
	}
	@Override protected void waitForIdleAsync() {
		super.waitForIdleAsync();
		final LocalBinder binding = provider.getBinding();
		if (binding == null) {
			return;
		}
		binding.addBackupListener(new BackupListener() {
			@Override public void started() {
				// NO OP, nothing to do, already busy
			}
			@Override public void finished() {
				binding.removeBackupListener(this);
				transitionToIdle();
			}
		});
	}
}   
