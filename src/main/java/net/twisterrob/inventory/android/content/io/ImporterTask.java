package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.zip.*;

import static java.lang.String.*;

import org.slf4j.*;

import android.content.Context;
import android.support.annotation.StringRes;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.Type;
import net.twisterrob.inventory.android.content.io.ImporterTask.ImportCallbacks.Progress;
import net.twisterrob.inventory.android.content.io.ImporterTask.ImportCallbacks.Progress.Phase;
import net.twisterrob.inventory.android.content.io.csv.CSVImporter;
import net.twisterrob.inventory.android.content.io.xml.XMLImporter;

public class ImporterTask extends SimpleAsyncTask<File, Progress, Progress> implements ImportProgressHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ImporterTask.class);

	private ImportCallbacks callbacks = DUMMY_CALLBACK;
	private Progress progress;
	private Importer importer;
	private ZipFile zip;

	public interface ImportCallbacks {
		void importStarting();
		void importProgress(Progress progress);
		void importFinished(Progress progress);

		final class Progress implements Cloneable {
			public File input;
			@Deprecated
			public Phase phase;
			public long done;
			public long total;
			public Throwable failure;
			public final List<CharSequence> conflicts = new ArrayList<>();

			@Override public Progress clone() {
				try {
					return (Progress)super.clone();
				} catch (CloneNotSupportedException ex) {
					throw new InternalError(ex.toString());
				}
			}
			@Override public String toString() {
				return format(Locale.ROOT, "%2$s: data=%3$d/%4$d: %5$s for %1$s", input, phase, done, total, failure);
			}

			public enum Phase {
				Init,
				Data
			}
		}
	}

	private final Context context;

	public ImporterTask(Context context) {
		this.context = context;
	}

	public void setCallbacks(ImportCallbacks callbacks) {
		this.callbacks = callbacks != null? callbacks : DUMMY_CALLBACK;
	}

	@Override protected void onPreExecute() {
		callbacks.importStarting();
	}
	@Override protected void onProgressUpdate(Progress progress) {
		callbacks.importProgress(progress);
	}
	@Override protected void onPostExecute(Progress progress) {
		if (progress.failure != null) {
			LOG.warn("Export failed", progress.failure);
		}
		callbacks.importFinished(progress);
	}

	public void publishStart(long size) {
		progress.done = 0;
		progress.total = size;
		publishProgress();
	}
	public void publishIncrement() {
		progress.done++;
		publishProgress();
	}
	private void publishProgress() {
		if (isCancelled()) {
			throw new CancellationException();
		}
		publishProgress(progress.clone());
	}

	public void warning(@StringRes int stringID, Object... args) {
		String message = context.getString(stringID, args);
		//LOG.warn("Warning: {}", message);
		//progress.conflicts.add(message);
	}

	public void error(String message) {
		LOG.warn("Error: {}", message);
		progress.conflicts.add(message);
	}

	@Override protected Progress doInBackground(File file) {
		progress = new Progress();
		progress.input = file;
		progress.phase = Phase.Data;
		zip = null;
		try {
			// TODO wakelock?

			publishStart(-1);
			App.db().getWritableDatabase().beginTransaction();

			zip = new ZipFile(file);

			ZipEntry xml = zip.getEntry(Constants.Paths.BACKUP_XML_FILENAME);
			ZipEntry csv = zip.getEntry(Constants.Paths.BACKUP_CSV_FILENAME);
			InputStream stream;
			if (xml != null) {
				stream = zip.getInputStream(xml);
				importer = new XMLImporter(this);
			} else if (csv != null) {
				stream = zip.getInputStream(csv);
				importer = new CSVImporter(this);
			} else {
				throw new IllegalArgumentException(
						format("The file %s is not a valid " + context.getString(R.string.app_name) + " backup: %s",
								zip.getName(), "missing data file"));
			}
			importer.doImport(stream);

			App.db().getWritableDatabase().setTransactionSuccessful();
		} catch (ZipException ex) {
			progress.failure = new IllegalArgumentException(format("%s: %s", file, "invalid zip file"), ex);
		} catch (Throwable ex) {
			progress.failure = ex;
		} finally {
			IOTools.ignorantClose(zip);
			App.db().getWritableDatabase().endTransaction();
		}
		return progress;
	}

	public void importImage(Type type, long id, String name, String image) throws IOException {
		ZipEntry imageEntry = zip.getEntry(image);
		if (imageEntry != null) {
			File imageFile = Constants.Paths.getImageFile(context, image);
			//noinspection ResultOfMethodCallIgnored FileOutputStream will fail nicely
			imageFile.getParentFile().mkdirs();
			InputStream imageStream = zip.getInputStream(imageEntry);
			IOTools.copyStream(imageStream, new FileOutputStream(imageFile));
		} else {
			warning(R.string.backup_import_invalid_image, name, image);
		}
	}

	/** To prevent NullPointerException and null-checks in code */
	private static final ImportCallbacks DUMMY_CALLBACK = new ImportCallbacks() {
		@Override public void importStarting() {
		}
		@Override public void importProgress(Progress progress) {
		}
		@Override public void importFinished(Progress progress) {
		}
	};
}

