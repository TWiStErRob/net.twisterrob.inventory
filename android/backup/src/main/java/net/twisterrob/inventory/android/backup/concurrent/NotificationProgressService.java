package net.twisterrob.inventory.android.backup.concurrent;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.*;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.inventory.android.backup.BuildConfig;
import net.twisterrob.inventory.android.content.VariantIntentService;

import static net.twisterrob.inventory.android.content.BroadcastTools.getLocalBroadcastManager;

public abstract class NotificationProgressService<Progress> extends VariantIntentService {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private static final int ONGOING_NOTIFICATION_OFFSET = 1;
	private static final int DONE_NOTIFICATION_OFFSET = 2;
	public static final String ACTION_PROGRESS_BROADCAST = "net.twisterrob.intent.action.PROGRESS_BROADCAST";
	public static final String ACTION_FINISHED_BROADCAST = "net.twisterrob.intent.action.FINISHED_BROADCAST";
	public static final String ACTION_PROGRESS_NOTIFICATION = "net.twisterrob.intent.action.PROGRESS_NOTIFICATION";
	public static final String ACTION_FINISHED_NOTIFICATION = "net.twisterrob.intent.action.FINISHED_NOTIFICATION";

	/**
	 * Special action which is only valid for {@link Context#bindService},
	 * so that {@link #onBind(Intent)} and {@link #onUnbind(Intent)} can distinguish between
	 * the framework binding to the job service, or the app binding for progress display.
	 */
	public static final String ACTION_BIND_UI = "net.twisterrob.inventory.intent.action.BIND_UI";

	private static final long NEVER = Long.MIN_VALUE;
	private NotificationCompat.Builder onGoingNotification;
	// TODO somehow make sure this is not possible:
	// onStop -> unbind -> onServiceDisconnected -> unregisterReceiver is called first
	// then service finishes in background thread and broadcasts and doesn't create notification -> /dev/null
	// then Service.onUnbind
	private boolean inBackground;
	/** Used to generate unique notification for each job that this service starts. */
	private long currentJobStarted = NEVER;
	private Progress lastProgress = null;
	private Progress lastProgressSentToNotification;
	private boolean debugMode = false;

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * @return a {@link NotificationCompat.Builder} for the notification,
	 *         {@link NotificationCompat.Builder#setOngoing} will be automatically added.
	 */
	protected @NonNull NotificationCompat.Builder createOnGoingNotification(@NonNull Intent intent) {
		return new NotificationCompat.Builder(this, BackupNotifications.FAKE_CHANNEL_ID)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setCategory(NotificationCompat.CATEGORY_PROGRESS)
				.setTicker("Action continues in background...")
				.setContentTitle("Action in progress")
				.setSmallIcon(android.R.drawable.stat_sys_download)
				;
	}

	protected @NonNull NotificationCompat.Builder createFinishedNotification(@NonNull Progress result) {
		return new NotificationCompat.Builder(this, BackupNotifications.FAKE_CHANNEL_ID)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setCategory(NotificationCompat.CATEGORY_PROGRESS)
				.setTicker("Finished action")
				.setContentTitle("Action done")
				.setContentText(result.toString())
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				;
	}

	protected @Nullable Intent createInProgressIntent() {
		return null;
	}
	protected @Nullable PendingIntent createInProgressPendingIntent() {
		Intent intent = createInProgressIntent();
		return acquirePendingIntent(intent, ACTION_PROGRESS_NOTIFICATION, ONGOING_NOTIFICATION_OFFSET);
	}

	protected @Nullable Intent createFinishedIntent(@NonNull Progress result) {
		return null;
	}
	protected @Nullable PendingIntent createFinishedPendingIntent(@NonNull Progress result) {
		Intent intent = createFinishedIntent(result);
		return acquirePendingIntent(intent, ACTION_FINISHED_NOTIFICATION, DONE_NOTIFICATION_OFFSET);
	}

	private PendingIntent acquirePendingIntent(@Nullable Intent intent, @NonNull String defaultAction, int codeOffset) {
		if (intent != null) {
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // behaves weirdly when app is already running
			if (intent.getAction() == null) {
				intent.setAction(defaultAction);
			}
			int code = getClass().getName().hashCode() + codeOffset;
			@SuppressLint("InlinedApi") // FLAG_IMMUTABLE is API 23, lower versions should just ignore it.
			int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
			return PendingIntent.getActivity(getApplicationContext(), code, intent, flags);
		} else {
			return null;
		}
	}

	protected final void finished(@NonNull Progress result) {
		if (currentJobStarted == NEVER) {
			throw new IllegalStateException("Did you call super.onHandleIntent?");
		}
		lastProgress = result;
		broadcast(result, ACTION_FINISHED_BROADCAST);
		if (inBackground) {
			LOG.trace("In background, replacing progress notification with done notification");
			stopNotification();
			NotificationManager notificationManager =
					(NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

			NotificationCompat.Builder doneNotification = createFinishedNotification(result);
			setIntentAndDefaults(doneNotification, createFinishedPendingIntent(result));
			Notification notification = buildNotification(doneNotification, null, result);
			notificationManager.notify((int)currentJobStarted + DONE_NOTIFICATION_OFFSET, notification);
		} else {
			LOG.trace("Not in background, no notification is needed (should be bound)");
		}
		currentJobStarted = NEVER;
	}

	private void setIntentAndDefaults(
			@NonNull NotificationCompat.Builder notification,
			@Nullable PendingIntent intent
	) {
		if (BuildConfig.DEBUG) {
			LOG.trace("Updating notification {} with {}", notification, StringerTools.toString(intent));
		}
		notification.setContentIntent(intent);
		notification.setAutoCancel(true);
	}

	protected abstract @NonNull IBinder createBinder();

	@Override public @Nullable IBinder onBind(@NonNull Intent intent) {
		if (ACTION_BIND_UI.equals(intent.getAction())) {
			super.onBind(intent);
			if (needsNotification(intent)) {
				LOG.trace("Stopping notification, because bind has a UI that displays progress.");
				inBackground = false;
				stopNotification();
			}
			return createBinder();
		}
		return super.onBind(intent);
	}

	@Override protected void onHandleWork(@NonNull Intent intent) {
		super.onHandleWork(intent);
		lastProgress = null;
		onGoingNotification = createOnGoingNotification(intent).setOngoing(true);
		setIntentAndDefaults(onGoingNotification, createInProgressPendingIntent());
		currentJobStarted = System.currentTimeMillis();
	}

	@Override public void onRebind(@NonNull Intent intent) {
		if (ACTION_BIND_UI.equals(intent.getAction())) {
			super.onRebind(intent);
			if (needsNotification(intent)) {
				LOG.trace("Stopping notification, because re-bind has a UI that displays progress.");
				inBackground = false;
				stopNotification();
			}
		} else {
			super.onRebind(intent);
		}
	}

	@Override public boolean onUnbind(@NonNull Intent intent) {
		if (ACTION_BIND_UI.equals(intent.getAction())) {
			super.onUnbind(intent);
			if (needsNotification(intent) && isInProgress()) {
				LOG.trace("Starting notification, because it was stopped when this bind happened, but now it's needed.");
				// Ignoring intent here, because it'll be empty (no action, no extras).
				// Using the last progress instead, because we're supposed to be in progress.
				// Which means that at least the initial progress was published already.
				startNotification(null, getLastProgress());
			}
			return true;
		} else {
			return super.onUnbind(intent);
		}
	}

	protected boolean needsNotification(Intent intent) {
		return true;
	}

	protected Progress getLastProgress() {
		return lastProgress;
	}
	public Progress getLastProgressSentToNotification() {
		return lastProgressSentToNotification;
	}
	protected boolean isInProgress() {
		return currentJobStarted != NEVER;
	}

	@SuppressLint("InlinedApi") // ServiceInfo.FOREGROUND_SERVICE_TYPE_* is safe because of ServiceCompat, but REPORT lint to AndroidX.
	private void startNotification(@Nullable Intent intent, @Nullable Progress progress) {
		lastProgressSentToNotification = lastProgress;
		inBackground = true;
		Notification notification = buildNotification(onGoingNotification, intent, progress);
		ServiceCompat.startForeground(
				this,
				(int)currentJobStarted + ONGOING_NOTIFICATION_OFFSET,
				notification,
				ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
		);
	}

	protected @NonNull Notification buildNotification(
			@NonNull NotificationCompat.Builder builder,
			@Nullable Intent intent,
			@Nullable Progress progress
	) {
		return builder.build();
	}

	@SuppressWarnings("deprecation")
	private void stopNotification() {
		if (Build.VERSION_CODES.N <= Build.VERSION.SDK_INT) {
			stopForeground(STOP_FOREGROUND_REMOVE);
		} else {
			stopForeground(true);
		}
	}

	private void broadcast(@NonNull Progress progress, String action) {
		Intent progressIntent = new Intent();
		fillBroadcast(progressIntent, progress);
		progressIntent.setAction(action);
		if (debugMode) {
			LOG.trace("Broadcasting {}: {}", progress, progressIntent);
		}
		getLocalBroadcastManager(getApplicationContext()).sendBroadcast(progressIntent);
	}

	protected final void reportProgress(@NonNull Progress progress) {
		lastProgress = progress;
		broadcast(progress, ACTION_PROGRESS_BROADCAST);
		boolean notify = fillNotification(onGoingNotification, progress);
		if (inBackground && notify) {
			startNotification(null, progress);
		}
	}

	protected void fillBroadcast(@NonNull Intent intent, @NonNull Progress progress) {
		//intent.putExtra(EXTRA_PROGRESS, progress.toString());
	}

	/**
	 * @return {@code false} to skip some updates
	 * @see <a href="http://stackoverflow.com/questions/18043568#comment26397268_18043568">
	 *     Why NotificationManager works so slow during the update progress?</a>
	 */
	protected boolean fillNotification(@NonNull NotificationCompat.Builder notification, @NonNull Progress progress) {
		onGoingNotification.setWhen(System.currentTimeMillis());
		return true;
	}
}
