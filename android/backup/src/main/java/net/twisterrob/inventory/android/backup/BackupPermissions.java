package net.twisterrob.inventory.android.backup;

import java.util.Arrays;

import org.slf4j.*;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import net.twisterrob.android.utils.tools.DialogTools;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.activity.BackupActivity;
import net.twisterrob.java.exceptions.StackTrace;

public class BackupPermissions {

	private static final Logger LOG = LoggerFactory.getLogger(BackupPermissions.class);

	private static final int PERMISSIONS_REQUEST = 34625;
	@TargetApi(VERSION_CODES.JELLY_BEAN)
	private static final String READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
	private static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
	private static final String[] storagePermissions = {
			READ_PERMISSION,
			WRITE_PERMISSION
	};

	private static boolean hasReadPermission(Context context) {
		return hasPermission(context, READ_PERMISSION);
	}

	private static boolean hasWritePermission(Context context) {
		return hasPermission(context, WRITE_PERMISSION);
	}

	private static boolean hasPermission(Context context, String permission) {
		int permissionState = PermissionChecker.checkSelfPermission(context, permission);
		return permissionState == PermissionChecker.PERMISSION_GRANTED;
	}
	public static void checkAndRequest(final BackupActivity activity) {
		if (hasReadPermission(activity) && hasWritePermission(activity)) {
			grantedPermanently(activity);
			return;
		}
		if (shouldShowRequestPermissionRationale(activity)) {
			DialogTools
					.confirm(activity, new PopupCallbacks<Boolean>() {
						@Override public void finished(Boolean value) {
							if (Boolean.TRUE.equals(value)) {
								requestPermissions(activity);
							} else {
								activity.finish();
							}
						}
					})
					.setTitle(R.string.backup_permission_title)
					.setMessage(R.string.backup_permission_message)
					.show();
		} else {
			requestPermissions(activity);
		}
	}

	private static void requestPermissions(BackupActivity activity) {
		ActivityCompat.requestPermissions(activity, storagePermissions, PERMISSIONS_REQUEST);
	}

	private static boolean shouldShowRequestPermissionRationale(BackupActivity activity) {
		return ActivityCompat.shouldShowRequestPermissionRationale(activity, READ_PERMISSION)
				|| ActivityCompat.shouldShowRequestPermissionRationale(activity, WRITE_PERMISSION);
	}

	public static boolean onRequestPermissionsResult(
			BackupActivity activity,
			int requestCode,
			@NonNull String[] permissions,
			@NonNull int[] grantResults
	) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST: {
				if (grantResults.length != 2) {
					cancelled(activity);
					return true;
				} else if (grantResults[0] == PackageManager.PERMISSION_GRANTED
						&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {
					grantedFirstTime(activity);
					return true;
				} else if (grantResults[0] == PackageManager.PERMISSION_DENIED
						&& grantResults[1] == PackageManager.PERMISSION_DENIED) {
					if (shouldShowRequestPermissionRationale(activity)) {
						deniedFirstTime(activity);
						return true;
					} else {
						deniedPermanently(activity);
						return true;
					}
				} else {
					LOG.trace("Grant result contains unknown permission result: {}",
							Arrays.toString(grantResults), new StackTrace());
					return false;
				}
			}
			default:
				return false;
		}
	}
	private static void grantedFirstTime(@SuppressWarnings("unused") BackupActivity activity) {
		LOG.trace("Permission granted -> leave the activity as is.");
	}
	private static void grantedPermanently(@SuppressWarnings("unused") BackupActivity activity) {
		LOG.trace("Permission was granted already -> leave the activity as is.");
	}
	private static void deniedFirstTime(BackupActivity activity) {
		LOG.trace("Permission request denied, no permission -> can't use backup.");
		activity.finish();
	}
	private static void deniedPermanently(BackupActivity activity) {
		LOG.trace("Permission request denied permanently -> can't use backup, coach user.");
		Toast.makeText(activity, R.string.backup_permission_coach, Toast.LENGTH_LONG).show();
		activity.finish();
	}
	private static void cancelled(BackupActivity activity) {
		LOG.trace("Permission request cancelled, unknown permission state -> can't use backup.");
		activity.finish();
	}
}
