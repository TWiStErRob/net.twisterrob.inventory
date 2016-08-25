package net.twisterrob.inventory.android.backup.concurrent;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.*;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.support.annotation.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BackupActivity;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter;
import net.twisterrob.java.exceptions.StackTrace;

import static net.twisterrob.inventory.android.backup.ProgressDisplayer.*;

public class BackupService extends NotificationProgressService<Progress> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupService.class);

	private static final String ACTION_EXPORT_PFD_WORKAROUND = "net.twisterrob.inventory.intent.action.EXPORT_PFD";
	public static final String ACTION_EXPORT = "net.twisterrob.inventory.intent.action.EXPORT";
	public static final String ACTION_EXPORT_DIR = "net.twisterrob.inventory.intent.action.EXPORT_DIR";
	public static final String ACTION_IMPORT = "net.twisterrob.inventory.intent.action.IMPORT";
	public static final String EXTRA_PROGRESS = "net.twisterrob.inventory:backup_progress";

	private /*final*/ ProgressDisplayer displayer;
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final WorkaroundQueue queue = new WorkaroundQueue();
	private final ProgressDispatcher progress = new ProgressDispatcher() {
		@Override public void dispatchProgress(@NonNull Progress progress) throws CancellationException {
			if (cancelled.compareAndSet(true, false)) {
				throw new CancellationException();
			}
			reportProgress(progress);
		}
	};

	public BackupService() {
		super(BackupService.class.getSimpleName());
	}

	@Override public void onCreate() {
		displayer = new ProgressDisplayer(this);
		super.onCreate();
	}

	@Override protected @NonNull Builder createOnGoingNotification(Intent intent) {
		return new android.support.v7.app.NotificationCompat.Builder(this)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setTicker(isImport(intent)
						? getString(R.string.backup_import_progress_background)
						: getString(R.string.backup_export_progress_background)
				)
				;
	}
	@Override protected boolean fillNotification(@NonNull Builder notification, @NonNull Progress progress) {
		super.fillNotification(notification, progress);
		displayer.setProgress(progress);
		notification.setContentTitle(displayer.getTitle());
		notification.setContentText(displayer.getMessage());
		notification.setProgress(displayer.getTotal(), displayer.getDone(), displayer.isIndeterminate());
		return isVeryDifferentFrom(getLastProgressSentToNotification(), progress);
	}

	@Override protected @NonNull Builder createFinishedNotification(@NonNull Progress result) {
		displayer.setProgress(result);
		return new android.support.v7.app.NotificationCompat.Builder(this)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setTicker(displayer.getMessage())
				.setContentTitle(displayer.getTitle())
				.setContentText(displayer.getMessage())
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				;
	}

	@Override protected @Nullable Intent createInProgressIntent() {
		return new Intent(getApplicationContext(), BackupActivity.class);
	}

	@Override protected @Nullable Intent createFinishedIntent(@NonNull Progress progress) {
		Intent intent = new Intent(getApplicationContext(), BackupActivity.class);
		intent.putExtra(EXTRA_PROGRESS, progress); // TODO Parcelable or ProgressDisplayer?
		return intent;
	}

	@Override protected void fillBroadcast(@NonNull Intent intent, @NonNull Progress progress) {
		super.fillBroadcast(intent, progress);
		intent.putExtra(EXTRA_PROGRESS, progress); // TODO Parcelable or ProgressDisplayer?
	}

	private boolean isImport(Intent intent) {
		return ACTION_IMPORT.equals(intent.getAction());
	}
	@WorkerThread
	@Override protected void onHandleIntent(Intent intent) {
		super.onHandleIntent(intent);
		try {
			if (ACTION_EXPORT_PFD_WORKAROUND.equals(intent.getAction())) {
				WorkaroundQueue.Job job = queue.next();
				try {
					BackupStreamExporter exporter = new BackupStreamExporter(new ZippedXMLExporter(), progress);
					Progress export = exporter.export(job.open());
					if (IOTools.isEPIPE(export.failure)) {
						Exception newFailure = new CancellationException();
						newFailure.initCause(export.failure);
						export.failure = newFailure;
					}
					finished(export);
				} finally {
					job.finished();
				}
			} else if (ACTION_EXPORT.equals(intent.getAction())) {
				OutputStream stream = getContentResolver().openOutputStream(intent.getData());
				BackupStreamExporter exporter = new BackupStreamExporter(new ZippedXMLExporter(), progress);
				finished(exporter.export(stream));
			} else if (ACTION_EXPORT_DIR.equals(intent.getAction())) {
				BackupDirExporter exporter =
						new BackupDirExporter(getApplicationContext(), new ZippedXMLExporter(), progress);
				File dir = new File(intent.getData().getPath());
				finished(exporter.exportTo(dir));
			}
		} catch (Throwable ex) {
			finished(new Progress(Progress.Type.Export, ex));
		}
		try {
			if (ACTION_IMPORT.equals(intent.getAction())) {
				Uri uri = intent.getData();
				finished(importFrom(uri));
			}
		} catch (Throwable ex) {
			finished(new Progress(Progress.Type.Import, ex));
		}
		displayer.setProgress(null);
	}

	private Progress importFrom(Uri input) {
		ImportProgressHandler progress = new ImportProgressHandler(dispatcher);
		progress.begin();

		ZipImporter<Uri> importer = new BackupTransactingImporter<>(new BackupZipUriImporter(this, progress), progress);
		try {
			importer.importFrom(input);
		} catch (Exception ex) {
			progress.fail(ex);
		}
		return progress.end();
	}

	@Override public IBinder onBind(Intent intent) {
		super.onBind(intent);
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public void export(ParcelFileDescriptor pfd, Runnable doneCallback) {
			queue.add(pfd, doneCallback);
			startService(new Intent(ACTION_EXPORT_PFD_WORKAROUND, (Uri)null, BackupService.this, BackupService.class));
		}

		public void cancel() {
			BackupService.this.cancel();
		}
		public boolean isInProgress() {
			return BackupService.this.isInProgress();
		}
		public Progress getLastProgress() {
			return BackupService.this.getLastProgress();
		}
	}

	private void cancel() {
		// FIXME interrupt/close output, so that XSLT transform dies
		LOG.info("Cancelling", new StackTrace());
		if (!cancelled.compareAndSet(false, true)) {
			throw new IllegalStateException("Already cancelled, but cancellation not yet picked up.");
		}
	}

	private final ProgressDispatcher dispatcher = new ProgressDispatcher() {
		@Override public void dispatchProgress(@NonNull Progress progress)
				throws CancellationException {
			if (cancelled.compareAndSet(true, false)) {
				throw new CancellationException();
			}
			reportProgress(progress);
		}
	};

	/**
	 * Not all parcelables can be put into Extras of an Intent.
	 * Specifically {@link ParcelFileDescriptor} can't: <q>"Not allowed to write file descriptors here"</q>.
	 * This means that the {@link BackupService} can't be just started nicely with an {@link Intent}
	 * from {@link net.twisterrob.inventory.android.content.InventoryProvider},
	 * but it needs to bind and pass the target differently.
	 * @see <a href="http://stackoverflow.com/q/18706062/253468"></a>
	 */
	private static class WorkaroundQueue {
		private final Queue<Job> queue = new LinkedBlockingDeque<>();

		public Job next() throws FileNotFoundException {
			Job job = queue.poll();
			if (job == null) {
				throw new FileNotFoundException("Cannot find job to work on, did you forget to set data?");
			}
			return job;
		}

		public void add(ParcelFileDescriptor pfd, Runnable doneCallback) {
			WorkaroundQueue.Job job = new WorkaroundQueue.Job();
			job.pfd = pfd;
			job.doneCallback = doneCallback;
			queue.add(job);
		}

		private static class Job {
			private Runnable doneCallback;
			private ParcelFileDescriptor pfd;
			public OutputStream open() {
				return new AutoCloseOutputStream(pfd);
			}
			public void finished() {
				doneCallback.run();
			}
		}
	}
}
