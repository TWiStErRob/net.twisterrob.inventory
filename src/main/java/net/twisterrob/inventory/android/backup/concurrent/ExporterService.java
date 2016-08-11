package net.twisterrob.inventory.android.backup.concurrent;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.*;
import android.os.Build.*;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.support.annotation.*;
import android.support.v4.app.NotificationCompat.Builder;
import android.system.*;

import net.twisterrob.inventory.android.backup.BackupStreamExporter;
import net.twisterrob.inventory.android.backup.BackupStreamExporter.ProgressDispatcher;
import net.twisterrob.inventory.android.backup.Exporter.ExportCallbacks.Progress;
import net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter;

public class ExporterService extends NotificationProgressService<Progress> {
	private static final Logger LOG = LoggerFactory.getLogger(ExporterService.class);
	private static final String ACTION_PFD_WORKAROUND = "net.twisterrob.inventory.intent.action.PFD_WORKAROUND";

	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final WorkaroundQueue queue = new WorkaroundQueue();

	public ExporterService() {
		super(ExporterService.class.getSimpleName());
	}

	@Override protected boolean isBindWithUI(Intent intent) {
		return false;
	}

	@Override protected void fillNotification(@NonNull Builder notification, @NonNull Progress progress) {
		super.fillNotification(notification, progress);
		// TODO phase it
		int current = progress.done;
		int total = progress.total;
		notification.setProgress(total, current, false);
		notification.setContentText(String.format(Locale.ROOT, "Exporting %d/%d", current, total));
	}

	@WorkerThread
	@Override protected void onHandleIntent(Intent intent) {
		super.onHandleIntent(intent);
		try {
			if (ACTION_PFD_WORKAROUND.equals(intent.getAction())) {
				WorkaroundQueue.Job job = queue.next();
				try {
					Progress export = export(job.open());
					if (isEPIPE(export.failure)) {
						Exception newFailure = new CancellationException();
						newFailure.initCause(export.failure);
						export.failure = newFailure;
					}
					finished(export);
				} finally {
					job.finished();
				}
			} else {
				finished(export(getContentResolver().openOutputStream(intent.getData())));
			}
		} catch (FileNotFoundException ex) {
			finished(new Progress(ex));
		}
	}

	private Progress export(OutputStream stream) {
		BackupStreamExporter exporter = new BackupStreamExporter(new ZippedXMLExporter(), new ProgressDispatcher() {
			@Override public void dispatchProgress(@NonNull Progress progress) throws CancellationException {
				if (cancelled.compareAndSet(true, false)) {
					throw new CancellationException();
				}
				reportProgress(progress);
			}
		});
		return exporter.export(stream);
	}

	@TargetApi(VERSION_CODES.LOLLIPOP)
	private boolean isEPIPE(@Nullable Throwable ex) {
		if (ex == null) {
			return false;
		}
		int code = -1;
		if (ex instanceof IOException) {
			ex = ex.getCause();
		}
		if (VERSION_CODES.LOLLIPOP <= VERSION.SDK_INT && ex instanceof ErrnoException) {
			code = ((ErrnoException)ex).errno;
		} else if ("ErrnoException".equals(ex.getClass().getSimpleName())) {
			// before 21 it's libcore.io.ErrnoException
			try {
				Field errno = ex.getClass().getDeclaredField("errno");
				code = (Integer)errno.get(ex);
			} catch (Throwable ignore) {
				// don't bother, we're doing best effort
			}
		}
		int epipe = VERSION_CODES.LOLLIPOP <= VERSION.SDK_INT? OsConstants.EPIPE : 32 /* from errno.h */;
		return code == epipe || ex.getMessage().contains("EPIPE");
	}

	@Override public IBinder onBind(Intent intent) {
		super.onBind(intent);
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public void export(ParcelFileDescriptor pfd, Runnable doneCallback) {
			queue.add(pfd, doneCallback);
			startService(new Intent(ACTION_PFD_WORKAROUND, null, ExporterService.this, ExporterService.class));
		}

		public void cancel() {
			if (!cancelled.compareAndSet(false, true)) {
				throw new IllegalStateException("Already cancelled, but cancellation not yet picked up.");
			}
		}
	}

	/**
	 * Not all parcelables can be put into Extras of an Intent.
	 * Specifically {@link ParcelFileDescriptor} can't: <q>"Not allowed to write file descriptors here"</q>.
	 * This means that the {@link ExporterService} can't be just started nicely with an {@link Intent}
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
