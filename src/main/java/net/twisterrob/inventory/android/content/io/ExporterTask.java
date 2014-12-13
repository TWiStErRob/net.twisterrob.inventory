package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.zip.*;

import org.apache.commons.csv.CSVPrinter;
import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.contract.ParentColumns.Type;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks.Progress;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks.Progress.Phase;

public class ExporterTask extends SimpleAsyncTask<OutputStream, Progress, Progress> {
	private static final Logger LOG = LoggerFactory.getLogger(ExporterTask.class);

	private Cursor cursor;
	private ZipOutputStream zip;
	private ExportCallbacks callbacks = DUMMY_CALLBACK;
	private Progress progress;

	public interface ExportCallbacks {
		void exportStarting();
		void exportProgress(Progress progress);
		void exportFinished(Progress progress);

		public static final class Progress implements Cloneable {
			public Phase phase;
			public int imagesTried;
			public int imagesFailed;
			public int done;
			public int total;
			public Throwable failure;

			@Override public Progress clone() {
				try {
					return (Progress)super.clone();
				} catch (CloneNotSupportedException ex) {
					throw new InternalError(ex.toString());
				}
			}
			@Override public String toString() {
				return String.format(Locale.ROOT, "%s: data=%d/%d images=%d/%d, %s",
						phase, done, total, imagesFailed, imagesTried, failure);
			}

			public enum Phase {
				Init,
				Data,
				Images
			}
		}
	}

	private final Context context;

	public ExporterTask(Context context) {
		this.context = context;
	}

	public void setCallbacks(ExportCallbacks callbacks) {
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

	private void publishStart() {
		progress.done = 0;
		publishProgress();
	}
	private void publishIncrement() {
		progress.done++;
		publishProgress();
	}
	private void publishProgress() {
		if (isCancelled()) {
			throw new CancellationException();
		}
		publishProgress(progress.clone());
	}

	@Override protected Progress doInBackground(OutputStream os) {
		progress = new Progress();
		try {
			// TODO wakelock?
			progress.phase = Phase.Init;
			cursor = App.db().export();
			progress.total = cursor.getCount();
			zip = new ZipOutputStream(new BufferedOutputStream(os));

			progress.phase = Phase.Data;
			saveData();

			progress.phase = Phase.Images;
			saveImages();

			zip.close();
		} catch (Throwable ex) {
			LOG.warn("Export failed", ex);
			progress.failure = ex;
		} finally {
			IOTools.ignorantClose(zip, cursor);
		}
		return progress;
	}
	private void saveImages() throws IOException {
		cursor.moveToPosition(-1);
		publishStart();
		while (cursor.moveToNext()) {
			String imageFileName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
			if (imageFileName != null) {
				try {
					progress.imagesTried++;
					saveImage(imageFileName);
				} catch (FileNotFoundException ex) {
					progress.imagesFailed++;
					LOG.warn("Cannot find image: {}", imageFileName, ex);
				}
			}
			publishIncrement();
		}
	}

	private void saveImage(String imageFileName) throws IOException {
		FileInputStream imageFile = new FileInputStream(Constants.Paths.getImageFile(context, imageFileName));
		try {
			ZipEntry entry = new ZipEntry(imageFileName);
			entry.setComment(buildComment());
			zip.putNextEntry(entry);
			IOTools.copyStream(imageFile, zip, false);
			zip.closeEntry();
		} finally {
			IOTools.ignorantClose(imageFile);
		}
	}
	private void saveData() throws IOException {
		zip.putNextEntry(new ZipEntry("data.csv"));
		export(zip);
		zip.closeEntry();
	}

	private String buildComment() {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		String typeName = cursor.getString(cursor.getColumnIndexOrThrow("type"));
		String property = cursor.getString(cursor.getColumnIndexOrThrow(Item.PROPERTY_NAME));
		String room = cursor.getString(cursor.getColumnIndexOrThrow(Item.ROOM_NAME));
		String item = cursor.getString(cursor.getColumnIndexOrThrow("itemName"));
		Type type = Type.from(typeName);
		String format = null;
		switch (type) {
			case Property:
				format = "%2$s #%1$d in %5$s";
				break;
			case Room:
				format = "%2$s #%1$d in %5$s in %4$s";
				break;
			case Item:
				format = "%2$s #%1$d in %5$s in %4$s in %3$s";
				break;
		}
		if (format != null) {
			return String.format(Locale.ROOT, format, id, typeName, property, room, item);
		} else {
			return null;
		}
	}

	// TODO check http://stackoverflow.com/questions/13229294/how-do-i-create-a-google-spreadsheet-with-a-service-account-and-share-to-other-g
	private void export(OutputStream out) throws IOException {
		CSVPrinter printer = CSVConstants.FORMAT.print(new PrintStream(out, false, CSVConstants.ENCODING));
		Object[] values = new Object[CSVConstants.FORMAT.getHeader().length];

		cursor.moveToPosition(-1);
		publishStart();
		while (cursor.moveToNext()) {
			for (int i = 0; i < values.length; ++i) {
				String column = CSVConstants.COLUMNS[i];
				if (column != null) {
					values[i] = cursor.getString(cursor.getColumnIndexOrThrow(column));
				} else {
					values[i] = null;
				}
			}
			printer.printRecord(values);
			publishIncrement();
		}
		printer.flush();
	}

	/** To prevent NullPointerException and null-checks in code */
	private static final ExportCallbacks DUMMY_CALLBACK = new ExportCallbacks() {
		@Override public void exportStarting() {
		}
		@Override public void exportProgress(Progress progress) {
		}
		@Override public void exportFinished(Progress progress) {
		}
	};
}
