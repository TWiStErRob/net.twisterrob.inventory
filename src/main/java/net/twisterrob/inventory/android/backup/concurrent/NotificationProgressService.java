package net.twisterrob.inventory.android.backup.concurrent;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.os.IBinder;
import android.support.annotation.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.LocalBroadcastManager;

import net.twisterrob.android.utils.log.LoggingIntentService;

public abstract class NotificationProgressService<Progress> extends LoggingIntentService {
	public NotificationProgressService(String name) {
		super(name);
	}

	private static final Logger LOG = LoggerFactory.getLogger(NotificationProgressService.class);

	private static final int ONGOING_NOTIFICATION_ID = 1;
	private static final int DONE_NOTIFICATION_ID = 2;
	public static final String ACTION_PROGRESS = "net.twisterrob.intent.action.PROGRESS";
	/**
	 * A {@code boolean} to signify that the {@link Context#bindService(Intent, ServiceConnection, int) bindService}
	 * action should not clear the current notification.
	 * Normal binds don't need this, because they're giving a UI for the service.
	 * Background binds however, still don't have a UI, so the notification should be kept.
	 *
	 * @see #isBindWithUI extending services have the ability to ignore this and implement custom logic
	 */
	public static final String EXTRA_BACKGROUND_BIND = "net.twisterrob:background-bind";
	/**
	 * @see #EXTRA_STATE_IN_PROGRESS
	 * @see #EXTRA_STATE_FINISHED
	 */
	public static final String EXTRA_STATE = "net.twisterrob:progress";
	public static final int EXTRA_STATE_IN_PROGRESS = 0;
	public static final int EXTRA_STATE_FINISHED = 1;
	private NotificationCompat.Builder onGoingNotification;
	private boolean inBackground;
	/** Used to generate unique notification for each job that this service starts. */
	private long currentJobStarted;

	@Override public void onCreate() {
		super.onCreate();
		onGoingNotification = createOnGoingNotification().setOngoing(true);
		setIntent(onGoingNotification, createInProgressPendingIntent());
	}

	protected @NonNull Builder createOnGoingNotification() {
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
		if (intent != null) {
			intent.putExtra(EXTRA_STATE, EXTRA_STATE_IN_PROGRESS);
			return PendingIntent.getActivity(getApplicationContext(), getClass().getName().hashCode() + 1, intent, 0);
		} else {
			return null;
		}
	}

	protected @Nullable Intent createFinishedIntent() {
		return null;
	}
	protected @Nullable PendingIntent createFinishedPendingIntent() {
		Intent intent = createFinishedIntent();
		if (intent != null) {
			intent.putExtra(EXTRA_STATE, EXTRA_STATE_FINISHED);
			return PendingIntent.getActivity(getApplicationContext(), getClass().getName().hashCode() + 2, intent, 0);
		} else {
			return null;
		}
	}

	protected final void finished(Progress result) {
		LOG.info("Done: {}", result);
		if (inBackground) {
			LOG.info("In background, sending replacing progress with done notification");
			stopNotification();
			NotificationManager notificationManager =
					(NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

			NotificationCompat.Builder doneNotification = createFinishedNotification(result);
			setIntent(doneNotification, createFinishedPendingIntent());
			notificationManager.notify((int)currentJobStarted + DONE_NOTIFICATION_ID, doneNotification.build());
		} else {
			LOG.info("Not in background, no notification is needed (should be bound)");
		}
	}

	private void setIntent(@NonNull NotificationCompat.Builder doneNotification, @Nullable PendingIntent intent) {
		if (intent != null) {
			doneNotification.setContentIntent(intent);
		} else {
			doneNotification.setAutoCancel(true);
		}
	}

	@Override public IBinder onBind(Intent intent) {
		super.onBind(intent);
		if (isBindWithUI(intent)) {
			stopNotification();
		}
		return null;
	}

	@Override protected void onHandleIntent(Intent intent) {
		super.onHandleIntent(intent);
		currentJobStarted = System.currentTimeMillis();
		if (!isBindWithUI(intent)) {
			startNotification();
		}
	}

	@Override public void onRebind(Intent intent) {
		super.onRebind(intent);
		if (isBindWithUI(intent)) {
			stopNotification();
		}
	}

	@Override public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		if (isBindWithUI(intent)) {
			startNotification();
		}
		return true;
	}

	protected boolean isBindWithUI(Intent intent) {
		return !intent.getBooleanExtra(EXTRA_BACKGROUND_BIND, false);
	}

	private void startNotification() {
		LOG.info("Starting notification");
		inBackground = true;
		reportProgress(null);
	}

	private void stopNotification() {
		LOG.info("Stopping notification");
		inBackground = false;
		stopForeground(true);
	}

	protected final void reportProgress(@Nullable Progress progress) {
		if (progress != null) {
			Intent progressIntent = new Intent();
			fillBroadcast(progressIntent, progress);
			progressIntent.setAction(ACTION_PROGRESS);
			progressIntent.putExtra(EXTRA_STATE, EXTRA_STATE_IN_PROGRESS);
			LOG.trace("Broadcasting {}: {}", progress, progressIntent);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(progressIntent);
		}

		if (inBackground) {
			if (progress != null) {
				fillNotification(onGoingNotification, progress);
			}
			startForeground((int)currentJobStarted + ONGOING_NOTIFICATION_ID, onGoingNotification.build());
		}
	}

	protected void fillBroadcast(@NonNull Intent intent, @NonNull Progress progress) {
		//intent.putExtra(EXTRA_PROGRESS, progress.toString());
	}

	protected void fillNotification(@NonNull NotificationCompat.Builder notification, @NonNull Progress progress) {
		onGoingNotification.setWhen(System.currentTimeMillis());
	}
}
