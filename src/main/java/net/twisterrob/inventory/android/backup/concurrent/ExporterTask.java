package net.twisterrob.inventory.android.backup.concurrent;

import java.io.File;
import java.util.concurrent.CancellationException;

import org.slf4j.*;

import android.content.Context;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.backup.*;

// FIXME convert to Service
public class ExporterTask extends SimpleAsyncTask<File, Progress, Progress> implements ProgressDispatcher {
	private static final Logger LOG = LoggerFactory.getLogger(ExporterTask.class);

	private Exporter.ExportCallbacks callbacks = DUMMY_CALLBACK;
	private final BackupDirExporter exporter;

	public ExporterTask(Exporter exporter, Context context) {
		this.exporter = new BackupDirExporter(context, exporter, this);
	}

	public void setCallbacks(Exporter.ExportCallbacks callbacks) {
		this.callbacks = callbacks != null? callbacks : DUMMY_CALLBACK;
	}

	@Override protected void onPreExecute() {
		callbacks.exportStarting();
	}
	@Override protected void onProgressUpdate(Progress progress) {
		callbacks.exportProgress(progress);
	}
	@Override protected void onPostExecute(Progress progress) {
		callbacks.exportFinished(progress);
	}

	@Override public void dispatchProgress(@NonNull Progress progress) throws CancellationException {
		if (isCancelled()) {
			throw new CancellationException();
		}
		publishProgress(progress);
	}

	@Override protected @NonNull Progress doInBackground(File dir) {
		try {
			return exporter.exportTo(dir);
		} catch (Throwable ex) {
			return new Progress(ex);
		}
	}

	/** To prevent NullPointerException and null-checks in code */
	private static final Exporter.ExportCallbacks DUMMY_CALLBACK = new Exporter.ExportCallbacks() {
		@Override public void exportStarting() {
		}
		@Override public void exportProgress(@NonNull Progress progress) {
		}
		@Override public void exportFinished(@NonNull Progress progress) {
		}
	};
}
