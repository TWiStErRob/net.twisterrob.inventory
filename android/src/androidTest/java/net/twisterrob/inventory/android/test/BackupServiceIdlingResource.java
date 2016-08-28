package net.twisterrob.inventory.android.test;

import android.support.annotation.Nullable;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.test.espresso.IntentServiceIdlingResource;
import net.twisterrob.inventory.android.backup.concurrent.BackupService;
import net.twisterrob.inventory.android.backup.concurrent.BackupService.LocalBinder;

public class BackupServiceIdlingResource extends IntentServiceIdlingResource {
	private final BindingProvider provider;

	public interface BindingProvider {
		@Nullable LocalBinder getBinding();
	}
	public BackupServiceIdlingResource(BindingProvider provider) {
		super(getInstrumentation().getTargetContext(), BackupService.class);
		this.provider = provider;
	}
	@Override protected boolean isIdle() {
		return super.isIdle() || !isInProgress();
	}
	private boolean isInProgress() {
		LocalBinder binding = provider.getBinding();
		return binding != null && binding.isInProgress();
	}
}   
