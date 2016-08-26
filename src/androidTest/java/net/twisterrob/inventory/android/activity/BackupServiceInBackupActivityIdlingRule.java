package net.twisterrob.inventory.android.activity;

import android.support.annotation.*;
import android.support.test.rule.ActivityTestRule;

import net.twisterrob.android.test.IdlingResourceRule;
import net.twisterrob.inventory.android.backup.concurrent.BackupService.LocalBinder;
import net.twisterrob.inventory.android.test.BackupServiceIdlingResource;
import net.twisterrob.inventory.android.test.BackupServiceIdlingResource.BindingProvider;

public class BackupServiceInBackupActivityIdlingRule extends IdlingResourceRule {
	public BackupServiceInBackupActivityIdlingRule(final @NonNull ActivityTestRule<BackupActivity> activity) {
		super(new BackupServiceIdlingResource(new BindingProvider() {
			@Override public @Nullable LocalBinder getBinding() {
				BackupActivity backupActivity = activity.getActivity();
				return backupActivity != null? backupActivity.backupService.getBinding() : null;
			}
		}));
	}
}
