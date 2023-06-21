package net.twisterrob.inventory.android.backup.concurrent;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.*;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.*;

import androidx.annotation.*;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.inventory.android.activity.BackupActivity;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.exporters.*;
import net.twisterrob.inventory.android.backup.exporters.ExportComponent.ExportEntryPoint;
import net.twisterrob.inventory.android.backup.importers.*;
import net.twisterrob.inventory.android.backup.importers.ImportComponent.ImportEntryPoint;
import net.twisterrob.java.utils.ObjectTools;

import static net.twisterrob.inventory.android.Constants.*;
import static net.twisterrob.inventory.android.backup.StrictProgressInfoProvider.*;

@AndroidEntryPoint
public class BackupService extends NotificationProgressService<Progress> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupService.class);

	private static final String ACTION_EXPORT_PFD_WORKAROUND = "net.twisterrob.inventory.intent.action.EXPORT_PFD";
	public static final String ACTION_EXPORT = "net.twisterrob.inventory.intent.action.EXPORT";
	public static final String ACTION_IMPORT = "net.twisterrob.inventory.intent.action.IMPORT";
	// TODO Parcelable or ProgressDisplayer?
	public static final String EXTRA_PROGRESS = "net.twisterrob.inventory:backup_progress";

	private /*final*/ ProgressInfoProvider displayer;
	private final BackupListeners listeners = new BackupListeners();
	/**
	 * Not all parcelables can be put into Extras of an Intent.
	 * Specifically {@link ParcelFileDescriptor} can't: <q>"Not allowed to write file descriptors here"</q>.
	 * This means that the {@link BackupService} can't be just started nicely with an {@link Intent}
	 * from {@link net.twisterrob.inventory.android.content.InventoryProvider},
	 * but it needs to bind and pass the target differently.
	 * @see <a href="http://stackoverflow.com/q/18706062/253468">Exception with sending ParcelFileDescriptor via Intent</a>
	 */
	@SuppressWarnings("JavadocReference")
	private final Queue<ParcelFileDescriptor> queue = new LinkedBlockingDeque<>();
	private final ProgressDispatcher dispatcher = new ProgressDispatcher();

	@Inject Provider<ExportComponent.Builder> exportFactory;
	@Inject Provider<ImportComponent.Builder> importFactory;

	public BackupService() {
		setDebugMode(DISABLE && BuildConfig.DEBUG);
	}

	public static void enqueueWork(@NonNull Context context, @NonNull Intent work) {
		enqueueWork(
				context,
				BackupService.class,
				BackupService.class.getName().hashCode(),
				work
		);
	}
	
	@Override public void onCreate() {
		displayer = new LenientProgressInfoProvider(this);
		super.onCreate();
	}

	@Override protected @NonNull NotificationCompat.Builder createOnGoingNotification(
			@NonNull Intent intent
	) {
		return new NotificationCompat.Builder(this, BackupNotifications.FAKE_CHANNEL_ID)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setCategory(NotificationCompat.CATEGORY_PROGRESS)
				.setColor(ContextCompat.getColor(this, R.color.accent))
				.setTicker(getProgressType(intent, null) == Progress.Type.Import
						? getString(R.string.backup_import_progress_background)
						: getString(R.string.backup_export_progress_background)
				)
				;
	}

	@Override protected boolean fillNotification(
			@NonNull NotificationCompat.Builder notification,
			@NonNull Progress progress
	) {
		super.fillNotification(notification, progress);
		displayer.setProgress(progress);
		notification.setContentTitle(displayer.getTitle());
		notification.setContentText(displayer.getMessage());
		notification.setProgress(displayer.getTotal(), displayer.getDone(), displayer.isIndeterminate());
		return isVeryDifferentFrom(getLastProgressSentToNotification(), progress);
	}

	@Override protected @NonNull NotificationCompat.Builder createFinishedNotification(
			@NonNull Progress result
	) {
		displayer.setProgress(result);
		String title = displayer.getTitle();
		String message = displayer.getMessage();
		return new NotificationCompat.Builder(this, BackupNotifications.FAKE_CHANNEL_ID)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setCategory(NotificationCompat.CATEGORY_PROGRESS)
				.setColor(ContextCompat.getColor(this, R.color.accent))
				.setTicker(title)
				.setContentTitle(title)
				.setContentText(message)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				;
	}

	@Override protected @NonNull Notification buildNotification(
			@NonNull NotificationCompat.Builder builder,
			@Nullable Intent intent,
			@Nullable Progress progress
	) {
		if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
			String channelId = getProgressType(intent, progress) == Progress.Type.Import
					? BackupNotifications.IMPORT_PROGRESS_CHANNEL_ID
					: BackupNotifications.EXPORT_PROGRESS_CHANNEL_ID;
			builder.setChannelId(channelId);
		}
		return super.buildNotification(builder, intent, progress);
	}

	@Override protected @Nullable Intent createInProgressIntent() {
		return new Intent(getApplicationContext(), BackupActivity.class);
	}

	@Override protected @Nullable Intent createFinishedIntent(@NonNull Progress progress) {
		Intent intent = new Intent(getApplicationContext(), BackupActivity.class);
		intent.putExtra(EXTRA_PROGRESS, progress);
		return intent;
	}

	@Override protected void fillBroadcast(@NonNull Intent intent, @NonNull Progress progress) {
		super.fillBroadcast(intent, progress);
		intent.putExtra(EXTRA_PROGRESS, progress);
	}


	private @NonNull Progress.Type getProgressType(
			@Nullable Intent intent,
			@Nullable Progress progress
	) {
		if (intent == null && progress != null) {
			return progress.type;
		} else if (intent != null && progress == null) {
			switch (String.valueOf(intent.getAction())) { // valueOf for using null in switch.
				case ACTION_IMPORT:
					return Progress.Type.Import;
				case ACTION_EXPORT:
				case ACTION_EXPORT_PFD_WORKAROUND:
					return Progress.Type.Export;
				case "null":
				default:
					throw new IllegalStateException("Unexpected intent action: " + intent);
			}
		} else {
			throw new IllegalStateException(
					"Only one of intent (" + intent + ") and (" + progress + ") must be (non)null");
		}
	}

	@WorkerThread
	@Override protected void onHandleWork(@NonNull Intent intent) {
		super.onHandleWork(intent);
		dispatcher.reset(); // call before started, so the listener may cancel immediately
		listeners.started();
		try {
			if (ACTION_EXPORT_PFD_WORKAROUND.equals(intent.getAction())) {
				dispatcher.setCancellable(false);
				BackupParcelExporter exporter = ExportEntryPoint.Companion
						.get(exportFactory.get().progress(dispatcher))
						.parcelExporter();
				ParcelFileDescriptor file = queue.remove();
				finish(exporter.exportTo(file));
			} else if (ACTION_EXPORT.equals(intent.getAction())) {
				dispatcher.setCancellable(false);
				BackupUriExporter exporter = ExportEntryPoint.Companion
						.get(exportFactory.get().progress(dispatcher))
						.uriExporter();
				Uri uri = intent.getData();
				finish(exporter.exportTo(uri));
			}
		} catch (Throwable ex) {
			finish(new Progress(Progress.Type.Export, ex));
		}
		// Looks like this will call multiple finish methods, but in fact they're all distinct ifs.
		try {
			if (ACTION_IMPORT.equals(intent.getAction())) {
				Uri uri = intent.getData();
				finish(importFrom(uri));
			}
		} catch (Throwable ex) {
			finish(new Progress(Progress.Type.Import, ex));
		}
		if (isInProgress()) {
			// no finish methods were called
			throw new IllegalStateException("Unknown intent action: " + intent.getAction());
		}
		listeners.finished();
		displayer.setProgress(null);
	}

	private void finish(Progress result) {
		LOG.info("Finished with: {}", result.toString(true), result.failure);
		finished(result);
	}

	private Progress importFrom(Uri input) {
		ImportProgressHandler progress = new ImportProgressHandler(dispatcher);
		progress.begin();

		ZipImporter<Uri> importer = ImportEntryPoint.Companion
				.get(importFactory.get().progress(progress))
				.importer();
		try {
			importer.importFrom(input);
		} catch (Exception ex) {
			progress.fail(ex);
		}
		return progress.end();
	}

	@Override
	protected @NonNull IBinder createBinder() {
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public void export(@NonNull ParcelFileDescriptor pfd) {
			queue.add(ObjectTools.checkNotNull(pfd));
			BackupService context = BackupService.this;
			Intent intent = new Intent(BackupService.ACTION_EXPORT_PFD_WORKAROUND, null, context, BackupService.class);
			enqueueWork(context, intent);
		}

		public void cancel() {
			dispatcher.cancel();
		}
		public boolean isInProgress() {
			return BackupService.this.isInProgress();
		}
		public Progress getLastProgress() {
			return BackupService.this.getLastProgress();
		}

		public void addBackupListener(@NonNull BackupListener listener) {
			listeners.add(listener);
		}
		public void removeBackupListener(@NonNull BackupListener listener) {
			listeners.remove(ObjectTools.checkNotNull(listener));
		}
		/** Signifies whether the current backup operation can clean up after itself. */
		public boolean isCancellable() {
			return dispatcher.isCancellable();
		}
		public boolean isCancelled() {
			return dispatcher.isCancelled();
		}
	}

	@AnyThread
	private class ProgressDispatcher implements net.twisterrob.inventory.android.backup.ProgressDispatcher {
		private final AtomicReference<CancellationException> cancelled = new AtomicReference<>(null);
		private volatile boolean cancellable = true;

		public void reset() {
			cancelled.set(null);
			setCancellable(true);
		}

		public void cancel() {
			if (!isInProgress()) {
				throw new IllegalStateException("There's nothing to cancel.");
			}
			// FIXME interrupt/close output, so that XSLT transform dies
			if (!cancelled.compareAndSet(null, new CancellationException("initiating stack trace"))) {
				throw new IllegalStateException("Already cancelled, but cancellation not yet picked up.");
			}
		}

		@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
		public boolean isCancelled() {
			return cancelled.get() != null;
		}

		@Override public void dispatchProgress(@NonNull Progress progress) throws CancellationException {
			CancellationException cancellationCause = cancelled.getAndSet(null);
			if (cancellationCause != null) {
				CancellationException realCancel = new CancellationException("user cancelled");
				realCancel.initCause(cancellationCause);
				throw realCancel;
			}
			reportProgress(progress);
		}
		public boolean isCancellable() {
			return cancellable;
		}
		public void setCancellable(boolean cancellable) {
			this.cancellable = cancellable;
		}
	}

	@UiThread
	public interface BackupListener {
		void started();
		void finished();
	}

	@AnyThread
	private static class BackupListeners implements BackupListener {
		private final Collection<BackupListener> listeners = new LinkedList<>();
		private final Handler main = new Handler(Looper.getMainLooper());

		public void add(@NonNull BackupListener listener) {
			synchronized (listeners) {
				listeners.add(ObjectTools.checkNotNull(listener));
			}
		}

		public void remove(@NonNull BackupListener listener) {
			synchronized (listeners) {
				listeners.remove(ObjectTools.checkNotNull(listener));
			}
		}

		@Override public void started() {
			synchronized (listeners) {
				for (final BackupListener listener : listeners) {
					main.post(new Runnable() {
						@Override public void run() {
							synchronized (listeners) {
								if (listeners.contains(listener)) {
									listener.started();
								} else {
									LOG.warn("Listener {} has been unregistered since post().", listener);
								}
							}
						}
					});
				}
			}
		}

		@Override public void finished() {
			synchronized (listeners) {
				for (final BackupListener listener : listeners) {
					main.post(new Runnable() {
						@Override public void run() {
							listener.finished();
						}
					});
				}
			}
		}
	}
}
