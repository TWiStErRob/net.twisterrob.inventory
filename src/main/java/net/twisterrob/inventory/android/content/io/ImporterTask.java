package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.zip.*;

import static java.lang.String.*;

import org.slf4j.*;

import android.content.Context;
import android.support.annotation.*;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.Type;
import net.twisterrob.inventory.android.content.io.ImporterTask.ImportCallbacks.Progress;
import net.twisterrob.inventory.android.content.io.csv.CSVImporter;
import net.twisterrob.inventory.android.content.io.xml.XMLImporter;

public class ImporterTask extends SimpleAsyncTask<File, Progress, Progress> implements ImportProgressHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ImporterTask.class);

	private ImportCallbacks callbacks = DUMMY_CALLBACK;
	private Progress progress;
	private ZipFile zip;

	public interface ImportCallbacks {
		void importStarting();
		void importProgress(@NonNull Progress progress);
		void importFinished(@NonNull Progress progress);

		final class Progress implements Cloneable {
			public File input;
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
				return format(Locale.ROOT, "data=%2$d/%3$d: %4$s for %1$s", input, done, total, failure);
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
		LOG.warn("Warning: {}", message);
		progress.conflicts.add(message);
	}

	public void error(String message) {
		LOG.warn("Error: {}", message);
		progress.conflicts.add(message);
	}

	@Override protected Progress doInBackground(File file) {
		progress = new Progress();
		progress.input = file;
		zip = null;
		try {
			// TODO wakelock?

			publishStart(-1);
			App.db().getWritableDatabase().beginTransaction();

			zip = new ZipFile(file);

			ZipEntry xml = zip.getEntry(Constants.Paths.BACKUP_XML_FILENAME);
			ZipEntry csv = zip.getEntry(Constants.Paths.BACKUP_CSV_FILENAME);
			InputStream stream;
			Importer importer;
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
			InputStream zipImage = zip.getInputStream(imageEntry);
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream((int)imageEntry.getSize());
			IOTools.copyStream(zipImage, byteOut);
			byte[] imageContents = byteOut.toByteArray();
			// XXX remove CSV handling
			long time = imageEntry.getTime();
			ZipEntry csv = zip.getEntry(Constants.Paths.BACKUP_CSV_FILENAME);
			long dataTime = csv == null? Long.MAX_VALUE : csv.getTime();
			if (dataTime < time) { // jpeg was written later than csv file, so date is invalid, use name
				try {
					// Item_1004_20150206_154157
					String datePart = image.replaceFirst("^.*_(\\d{8}_\\d{6}).jpg$", "$1");
					time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).parse(datePart).getTime();
				} catch (ParseException ex) {
					LOG.warn("Cannot parse: {}", image, ex);
				}
			}
			Long dbTime = time != -1? time : null;
			switch (type) {
				case Property:
					App.db().setPropertyImage(id, imageContents, dbTime);
					break;
				case Room:
					App.db().setRoomImage(id, imageContents, dbTime);
					break;
				case Item:
					App.db().setItemImage(id, imageContents, dbTime);
					break;
				default:
					throw new IllegalArgumentException(type + " cannot have images.");
			}
		} else {
			warning(R.string.backup_import_invalid_image, name, image);
		}
	}

	/** To prevent NullPointerException and null-checks in code */
	private static final ImportCallbacks DUMMY_CALLBACK = new ImportCallbacks() {
		@Override public void importStarting() {
		}
		@Override public void importProgress(@NonNull Progress progress) {
		}
		@Override public void importFinished(@NonNull Progress progress) {
		}
	};
}

