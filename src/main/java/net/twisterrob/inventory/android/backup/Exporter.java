package net.twisterrob.inventory.android.backup;

import java.io.OutputStream;
import java.util.Locale;

import android.database.Cursor;
import android.support.annotation.NonNull;

@SuppressWarnings("RedundantThrows")
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

	interface ExportCallbacks {
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
			public boolean pending;
			public Throwable failure;

			public Progress() {
				phase = Phase.Init;
				pending = true;
			}

			public Progress(Throwable ex) {
				this();
				failure = ex;
			}

			@Override public Progress clone() {
				try {
					return (Progress)super.clone();
				} catch (CloneNotSupportedException ex) {
					throw new InternalError(ex.toString());
				}
			}

			@Override public String toString() {
				return String.format(Locale.ROOT, "%s: data=%d/%d images=%d/%d, %spending, %s",
						phase, done, total, imagesDone, imagesTotal,
						pending? "" : "not ", failure != null? failure : "no error");
			}

			public enum Phase {
				Init,
				Data,
				Images
			}
		}
	}
}
