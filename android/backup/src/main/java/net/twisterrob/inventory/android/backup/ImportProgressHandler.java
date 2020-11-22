package net.twisterrob.inventory.android.backup;

import org.slf4j.*;

import androidx.annotation.VisibleForTesting;

import net.twisterrob.inventory.android.backup.Importer.ImportProgress;
import net.twisterrob.inventory.android.backup.Progress.Phase;
import net.twisterrob.java.utils.ObjectTools;

// TODO generalize to import/export and refactor deprecation
@SuppressWarnings("deprecation")
public class ImportProgressHandler implements ImportProgress {
	private static final Logger LOG = LoggerFactory.getLogger(ImportProgressHandler.class);
	private final ProgressDispatcher dispatcher;

	/** @deprecated don't use it directly */
	@Deprecated
	public final Progress progress = new Progress(Progress.Type.Import);

	@VisibleForTesting ImportProgressHandler() {
		this(ProgressDispatcher.IGNORE);
	}
	public ImportProgressHandler(ProgressDispatcher dispatcher) {
		this.dispatcher = ObjectTools.checkNotNull(dispatcher);
	}

	public void publishStart(int size) {
		progress.pending = false;
		progress.done = 0;
		progress.total = size;
		publishProgress();
	}

	public void publishIncrement() {
		progress.pending = false;
		progress.done++;
		publishProgress();
	}
	public void imageIncrement() {
		progress.imagesDone++;
	}
	public void imageTotalIncrement() {
		progress.imagesTotal++;
	}

	public void publishProgress() {
		dispatcher.dispatchProgress(progress.clone());
	}

	@Override public void warning(String message) {
		//LOG.warn("Warning: {}", message);
		progress.warnings.add(message);
	}

	@Override public void error(String message) {
		//LOG.warn("Error: {}", message);
		progress.warnings.add(message);
	}

	public void begin() {
		publishProgress();
	}
	public Progress end() {
		progress.phase = Phase.Finished;
		return progress;
	}

	public void fail(Throwable ex) {
		if (progress.failure != null) {
			LOG.warn("Exception suppressed by {}", progress.failure, ex);
			error(ex.toString());
		} else {
			progress.failure = ex;
		}
	}
}
