package net.twisterrob.inventory.android.activity;

import android.Manifest;
import android.os.Build;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;

import net.twisterrob.android.permissions.PermissionProtectedAction;
import net.twisterrob.android.permissions.PermissionState;
import net.twisterrob.android.utils.tools.DialogTools;
import net.twisterrob.inventory.android.backup.R;

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
public class BackupNotificationsMenu implements MenuProvider {
	private final @NonNull PermissionProtectedAction notifications;

	BackupNotificationsMenu(final @NonNull AppCompatActivity activity) {
		notifications = new PermissionProtectedAction(
				activity,
				new String[] {Manifest.permission.POST_NOTIFICATIONS},
				new PermissionProtectedAction.PermissionEvents() {
					@Override public void granted(@NonNull GrantedReason reason) {
						activity.supportInvalidateOptionsMenu(); // Hide the button.
					}

					@Override public void denied(@NonNull DeniedReason reason) {
						activity.supportInvalidateOptionsMenu();
						// Nothing to do, backup feature's actions work with or without notifications.
					}

					@Override public void showRationale(final @NonNull RationaleContinuation continuation) {
						DialogTools
								.confirm(activity, new DialogTools.PopupCallbacks<Boolean>() {
									@Override public void finished(Boolean value) {
										if (Boolean.TRUE.equals(value)) {
											continuation.rationaleAcceptedRetryRequest();
										} else {
											continuation.rationaleRejectedCancelProcess();
										}
									}
								})
								.setTitle(R.string.backup_permission_notification_rationale_title)
								.setMessage(R.string.backup_permission_notification_rationale_body)
								.show();
					}
				}
		);
	}

	@Override public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
		menuInflater.inflate(R.menu.backup_notifications, menu);
	}

	@Override public void onPrepareMenu(@NonNull Menu menu) {
		menu.findItem(R.id.backup_notifications)
		    .setVisible(notifications.currentState() != PermissionState.GRANTED);
	}

	@Override public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
		if (menuItem.getItemId() == R.id.backup_notifications) {
			notifications.executeBehindPermissions();
			return true;
		}
		return false;
	}
}
