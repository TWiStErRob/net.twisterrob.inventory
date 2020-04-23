package net.twisterrob.inventory.android.backup.concurrent;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.os.IBinder;
import android.support.annotation.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.LocalBroadcastManager;

import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.inventory.android.backup.BuildConfig;
import net.twisterrob.inventory.android.content.VariantIntentService;

public abstract class NotificationProgressService<Progress> extends VariantIntentService {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private static final int ONGOING_NOTIFICATION_OFFSET = 1;
	private static final int DONE_NOTIFICATION_OFFSET = 2;
	public static final String ACTION_PROGRESS_BROADCAST = "net.twisterrob.intent.action.PROGRESS_BROADCAST";
	public static final String ACTION_FINISHED_BROADCAST = "net.twisterrob.intent.action.FINISHED_BROADCAST";
	public static final String ACTION_PROGRESS_NOTIFICATION = "net.twisterrob.intent.action.PROGRESS_NOTIFICATION";
	public static final String ACTION_FINISHED_NOTIFICATION = "net.twisterrob.intent.action.FINISHED_NOTIFICATION";

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

	public NotificationProgressService(String name) {
		super(name);
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	protected @NonNull Builder createOnGoingNotification(Intent intent) {
		return new android.support.v7.app.NotificationCompat.Builder(this)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setTicker("Action continues in background...")
				.setContentTitle("Action in progress")
				.setSmallIcon(android.R.drawable.stat_sys_download)
				;
	}

	protected @NonNull Builder createFinishedNotification(@NonNull Progress result) {
		return new android.support.v7.app.NotificationCompat.Builder(this)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
			return PendingIntent.getActivity(getApplicationContext(), code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
			stopForeground(true);
			NotificationManager notificationManager =
					(NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

			NotificationCompat.Builder doneNotification = createFinishedNotification(result);
			setIntentAndDefaults(doneNotification, createFinishedPendingIntent(result));
			notificationManager.notify((int)currentJobStarted + DONE_NOTIFICATION_OFFSET, doneNotification.build());
		} else {
			LOG.trace("Not in background, no notification is needed (should be bound)");
		}
		currentJobStarted = NEVER;
	}

	private void setIntentAndDefaults(@NonNull NotificationCompat.Builder notification,
			@Nullable PendingIntent intent) {
		if (BuildConfig.DEBUG) {
			LOG.trace("Updating notification {} with {}", notification, StringerTools.toString(intent));
		}
		notification.setContentIntent(intent);
		notification.setAutoCancel(true);
	}

	@Override public IBinder onBind(Intent intent) {
		super.onBind(intent);
		if (needsNotification(intent)) {
			LOG.trace("Stopping notification, because bind has a UI that displays progress.");
			stopNotification();
		}
		return null;
	}

	@Override protected void onHandleIntent(Intent intent) {
		super.onHandleIntent(intent);
		lastProgress = null;
		onGoingNotification = createOnGoingNotification(intent).setOngoing(true);
		setIntentAndDefaults(onGoingNotification, createInProgressPendingIntent());
		currentJobStarted = System.currentTimeMillis();
	}

	@Override public void onRebind(Intent intent) {
		super.onRebind(intent);
		if (needsNotification(intent)) {
			LOG.trace("Stopping notification, because re-bind has a UI that displays progress.");
			stopNotification();
		}
	}

	@Override public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		if (needsNotification(intent) && isInProgress()) {
			LOG.trace("Starting notification, because it was stopped when this bind happened, but now it's needed.");
			startNotification();
		}
		return true;
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

	private void startNotification() {
		lastProgressSentToNotification = lastProgress;
		inBackground = true;
		startForeground((int)currentJobStarted + ONGOING_NOTIFICATION_OFFSET, onGoingNotification.build());
	}

	private void stopNotification() {
		inBackground = false;
		stopForeground(true);
	}

	private void broadcast(@NonNull Progress progress, String action) {
		Intent progressIntent = new Intent();
		fillBroadcast(progressIntent, progress);
		progressIntent.setAction(action);
		if (debugMode) {
			LOG.trace("Broadcasting {}: {}", progress, progressIntent);
		}
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(progressIntent);
	}

	protected final void reportProgress(@NonNull Progress progress) {
		lastProgress = progress;
		broadcast(progress, ACTION_PROGRESS_BROADCAST);
		boolean notify = fillNotification(onGoingNotification, progress);
		if (inBackground && notify) {
			startNotification();
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
