package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.util.Locale;
import java.util.concurrent.CancellationException;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks.Progress;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks.Progress.Phase;

public class ExporterTask extends SimpleAsyncTask<Void, Progress, Progress> {
	private static final Logger LOG = LoggerFactory.getLogger(ExporterTask.class);
	public static final String IMAGE_NAME = "imageName";

	private Cursor cursor;
	private ExportCallbacks callbacks = DUMMY_CALLBACK;
	private Progress progress;
	private Exporter exporter;

	public interface ExportCallbacks {
		void exportStarting();
		void exportProgress(@NonNull Progress progress);
		void exportFinished(@NonNull Progress progress);

		final class Progress implements Cloneable {
			public Phase phase;
			/** number of images done from total */
			public int imagesDone;
			/** total number of images */
			public int imagesTotal;
			/** number of items done from total */
			public int done;
			/** total number of items */
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
						phase, done, total, imagesDone, imagesTotal, failure);
			}

			public enum Phase {
				Init,
				Data,
				Images
			}
		}
	}

	private final Context context;

	public ExporterTask(Exporter exporter, Context context) {
		this.exporter = exporter;
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

	protected void publishStart() {
		progress.done = 0;
		publishProgress();
	}
	protected void publishIncrement() {
		progress.done++;
		publishProgress();
	}
	protected void publishProgress() {
		if (isCancelled()) {
			throw new CancellationException();
		}
		publishProgress(progress.clone());
	}

	@Override protected Progress doInBackground(Void ignore) {
		File file = null;
		try {
			file = Paths.getExportFile();
			OutputStream os = new FileOutputStream(file); // will be closed by finalizeExport
			// TODO wakelock?
			progress = new Progress();
			progress.phase = Phase.Init;
			publishStart();
			cursor = App.db().export();
			progress.total = cursor.getCount();
			exporter.initExport(os);

			progress.phase = Phase.Data;
			saveData();

			progress.phase = Phase.Images;
			saveImages();

			exporter.finishExport();
		} catch (Throwable ex) {
			LOG.warn("Export failed", ex);
			progress.failure = ex;
		} finally {
			IOTools.ignorantClose(cursor);
			exporter.finalizeExport();
		}
		if (!BuildConfig.DEBUG && progress.failure != null && file != null && !file.delete()) {
			file.deleteOnExit();
			file = null;
		}
		if (file != null) {
			AndroidTools.makeFileDiscoverable(context, file);
		}

		return progress;
	}

	// TODO check http://stackoverflow.com/questions/13229294/how-do-i-create-a-google-spreadsheet-with-a-service-account-and-share-to-other-g
	protected void saveData() throws Throwable {
		exporter.initData(cursor);

		cursor.moveToPosition(-1);
		publishStart();
		while (cursor.moveToNext()) {
			if (!cursor.isNull(cursor.getColumnIndexOrThrow(IMAGE_NAME))) {
				progress.imagesTotal++;
			}
			exporter.writeData(cursor);
			publishIncrement();
		}

		exporter.finishData(cursor);
	}

	private void saveImages() throws Throwable {
		exporter.initImages(cursor);

		cursor.moveToPosition(-1);
		publishStart();
		while (cursor.moveToNext()) {
			String imageFileName = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_NAME));
			if (imageFileName != null) {
				exporter.saveImage(cursor);
				progress.imagesDone++;
			} else {
				exporter.noImage(cursor);
			}
			publishIncrement();
		}

		exporter.finishImages(cursor);
	}

	public static String buildComment(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		String property = cursor.getString(cursor.getColumnIndexOrThrow(Item.PROPERTY_NAME));
		String room = cursor.getString(cursor.getColumnIndexOrThrow(Item.ROOM_NAME));
		String item = cursor.getString(cursor.getColumnIndexOrThrow("itemName"));
		String parentName = cursor.getString(cursor.getColumnIndexOrThrow(ParentColumns.PARENT_NAME));
		Type type = Type.from(cursor, "type");
		String format = null;
		switch (type) {
			case Property:
				format = "%2$s #%1$d: %3$s";
				break;
			case Room:
				format = "%2$s #%1$d: %4$s in %3$s";
				break;
			case Item:
				if (Type.from(cursor, "parentType") == Type.Room) {
					format = "%2$s #%1$d: %5$s in %4$s in %3$s";
				} else {
					format = "%2$s #%1$d: %5$s in %6$s in %4$s in %3$s";
				}
				break;
		}
		if (format != null) {
			return String.format(Locale.ROOT, format, id, type, property, room, item, parentName);
		} else {
			return null;
		}
	}

	public interface Exporter {
		void initExport(OutputStream os);
		void finishExport() throws Throwable;
		void finalizeExport();

		void initData(Cursor cursor) throws Throwable;
		void writeData(Cursor cursor) throws Throwable;
		void finishData(Cursor cursor) throws Throwable;

		void initImages(Cursor cursor) throws Throwable;
		void saveImage(Cursor cursor) throws Throwable;
		void noImage(Cursor cursor) throws Throwable;
		void finishImages(Cursor cursor) throws Throwable;
	}

	/** To prevent NullPointerException and null-checks in code */
	private static final ExportCallbacks DUMMY_CALLBACK = new ExportCallbacks() {
		@Override public void exportStarting() {
		}
		@Override public void exportProgress(@NonNull Progress progress) {
		}
		@Override public void exportFinished(@NonNull Progress progress) {
		}
	};
}
