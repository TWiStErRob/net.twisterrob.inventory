package net.twisterrob.inventory.android.backup;

import java.io.OutputStream;

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
	}
}
