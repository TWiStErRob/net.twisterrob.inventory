package net.twisterrob.inventory.android.backup.exporters;

import java.io.*;
import java.util.zip.*;

import android.database.Cursor;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.backup.Exporter;
import net.twisterrob.inventory.android.backup.xml.CursorExporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;

@SuppressWarnings("RedundantThrows")
public abstract class ZippedExporter implements Exporter {
	protected final @NonNull String fileName;
	protected ZipOutputStream zip;
	private final @NonNull Database db;
	private final @NonNull CursorExporter dataOutput;

	public ZippedExporter(
			@NonNull String fileName,
			@NonNull Database db,
			@NonNull CursorExporter exporter
	) {
		this.fileName = fileName;
		this.db = db;
		this.dataOutput = exporter;
	}

	@Override public void initExport(OutputStream os) {
		zip = new ZipOutputStream(new BufferedOutputStream(os));
	}
	@Override public void finishExport() throws IOException {
		zip.finish();
	}
	@Override public void finalizeExport() {
		IOTools.ignorantClose(zip);
	}

	@Override public void initData(Cursor cursor) throws Throwable {
		dataOutput.start(startStream(zip), cursor);
	}
	protected OutputStream startStream(ZipOutputStream zip) throws IOException {
		zip.putNextEntry(new ZipEntry(fileName));
		return zip;
	}

	@Override public void writeData(Cursor cursor) throws Throwable {
		dataOutput.processEntry(cursor);
	}

	@Override public void finishData(Cursor cursor) throws Throwable {
		dataOutput.finish(cursor);
		endStream();
	}
	protected void endStream() throws IOException {
		zip.closeEntry();
	}

	@Override public void initImages(Cursor cursor) {
		// nop
	}
	@Override public void saveImage(Cursor cursor) throws IOException {
		byte[] image = getImage(cursor);
		long unixUtc = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE_TIME));
		String fileName = cursor.getString(cursor.getColumnIndexOrThrow(BackupStreamExporter.IMAGE_NAME));
		String comment = BackupStreamExporter.buildComment(cursor);
		IOTools.store(zip, fileName, image, unixUtc, comment);
	}

	@Override public void noImage(Cursor cursor) throws Throwable {
		// nop
	}
	@Override public void finishImages(Cursor cursor) throws Throwable {
		// nop
	}
	@SuppressWarnings("resource") // Cursors are closed in singleBlob
	public byte[] getImage(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		Type type = Type.from(cursor, CommonColumns.TYPE);
		switch (type) {
			case Property:
				cursor = db.getPropertyImage(id);
				break;
			case Room:
				cursor = db.getRoomImage(id);
				break;
			case Item:
				cursor = db.getItemImage(id);
				break;
		}
		return DatabaseTools.singleBlob(cursor, ImageDataColumns.COLUMN_BLOB);
	}
}
