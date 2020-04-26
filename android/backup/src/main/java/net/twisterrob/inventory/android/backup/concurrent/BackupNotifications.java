package net.twisterrob.inventory.android.backup.concurrent;

import java.util.Arrays;

import android.app.*;
import android.content.Context;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import static android.os.Build.*;

import net.twisterrob.inventory.android.backup.R;

/**
 * NOTE: if an ID here is renamed, need to delete that via {@link NotificationManager},
 * otherwise it lingers for users until they clear data.
 */
@RequiresApi(VERSION_CODES.O)
public class BackupNotifications {

	private static final String BACKUP_CHANNEL_GROUP_ID = "channel_group__backup";
	public static final String IMPORT_PROGRESS_CHANNEL_ID = "channel__import_progress";
	public static final String EXPORT_PROGRESS_CHANNEL_ID = "channel__export_progress";

	public static void registerNotificationChannels(Context context) {
		NotificationManager service = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		service.createNotificationChannelGroup(
				createBackupGroup(context)
		);
		service.createNotificationChannels(Arrays.asList(
				createImportProgressChannel(context),
				createExportProgressChannel(context)
		));
	}

	private static NotificationChannelGroup createBackupGroup(Context context) {
		NotificationChannelGroup group = new NotificationChannelGroup(
				BACKUP_CHANNEL_GROUP_ID,
				context.getText(R.string.backup_notification_channel_group_name)
		);
		if (VERSION_CODES.P <= VERSION.SDK_INT) {
			group.setDescription(context.getString(R.string.backup_notification_channel_group_description));
		}
		return group;
	}

	private static NotificationChannel createImportProgressChannel(Context context) {
		NotificationChannel channel = new NotificationChannel(
				IMPORT_PROGRESS_CHANNEL_ID,
				context.getText(R.string.backup_import_notification_channel_name),
				NotificationManager.IMPORTANCE_NONE
		);
		channel.setDescription(context.getString(R.string.backup_import_notification_channel_description));
		channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		channel.setGroup(BACKUP_CHANNEL_GROUP_ID);
		return channel;
	}

	private static NotificationChannel createExportProgressChannel(Context context) {
		NotificationChannel channel = new NotificationChannel(
				EXPORT_PROGRESS_CHANNEL_ID,
				context.getText(R.string.backup_export_notification_channel_name),
				NotificationManager.IMPORTANCE_NONE
		);
		channel.setDescription(context.getString(R.string.backup_export_notification_channel_description));
		channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		channel.setGroup(BACKUP_CHANNEL_GROUP_ID);
		return channel;
	}
}
