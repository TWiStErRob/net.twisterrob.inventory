package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.util.Locale;
import java.util.zip.*;

import org.apache.commons.csv.CSVPrinter;
import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.contract.ParentColumns.Type;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks.Progress;
import net.twisterrob.inventory.android.content.io.csv.CSVConstants;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.java.utils.ConcurrentTools;

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

		public static class Progress {
			public boolean copyingImages;
			public int done;
			public int total;

			@Override public String toString() {
				return String.format(Locale.ROOT, "%b: %d/%d", copyingImages, done, total);
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
	@Override protected void onPostExecute(Progress progress) {
		callbacks.exportFinished(progress);
	}
	@Override protected void onProgressUpdate(Progress progress) {
		callbacks.exportProgress(progress);
	}

	@Override protected Progress doInBackground(OutputStream os) {
		zip = new ZipOutputStream(new BufferedOutputStream(os));
		cursor = App.db().export();
		progress = new Progress();
		progress.total = cursor.getCount();
		// TODO wakelock?
		try {
			saveData();
			saveImages();
			zip.close();
		} catch (IOException ex) {
			LOG.error("Cannot finish export", ex);
		} finally {
			IOTools.ignorantClose(zip, cursor);
		}
		return progress;
	}
	private void saveImages() throws IOException {
		cursor.moveToPosition(-1);
		progress.copyingImages = true;
		progress.done = 0;
		publishProgress(progress); // TODO extract and check cancel and throw if isCancelled

		while (cursor.moveToNext()) {
			String imageFileName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
			saveImage(imageFileName);
			ConcurrentTools.ignorantSleep(100);
			progress.done++;
			publishProgress(progress);
		}
	}
	private void saveImage(String imageFileName) throws IOException {
		if (imageFileName != null) {
			try {
				FileInputStream imageFile = new FileInputStream(ImagedDTO.getImage(context, imageFileName));
				ZipEntry entry = new ZipEntry(imageFileName);
				entry.setComment(buildComment());
				zip.putNextEntry(entry);
				IOTools.copyStream(imageFile, zip, false);
				zip.closeEntry();
			} catch (FileNotFoundException ex) {
				LOG.warn("Cannot find image: {}", imageFileName, ex);
			}
		}
	}
	private void saveData() throws IOException {
		zip.putNextEntry(new ZipEntry("data.csv"));
		export();
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

	private void export() throws IOException {
		CSVPrinter printer = CSVConstants.FORMAT.print(new PrintStream(zip, false, CSVConstants.ENCODING));
		cursor.moveToPosition(-1);
		progress.done = 0;
		publishProgress(progress);

		Object[] values = new Object[CSVConstants.FORMAT.getHeader().length];
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
			ConcurrentTools.ignorantSleep(100);
			progress.done++;
			publishProgress(progress);
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
