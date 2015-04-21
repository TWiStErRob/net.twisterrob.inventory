package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.util.zip.*;

import android.database.Cursor;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

public abstract class ZippedExporter<T> implements ExporterTask.Exporter {
	private final String fileName;
	private ZipOutputStream zip;
	private T dataOutput;

	protected ZippedExporter(String fileName) {
		this.fileName = fileName;
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
		zip.putNextEntry(new ZipEntry(fileName));
		dataOutput = initData(zip, cursor);
	}
	protected abstract T initData(OutputStream dataStream, Cursor cursor) throws Throwable;

	@Override public void writeData(Cursor cursor) throws Throwable {
		writeData(dataOutput, cursor);
	}
	protected abstract void writeData(T output, Cursor cursor) throws Throwable;

	@Override public void finishData(Cursor cursor) throws Throwable {
		finishData(dataOutput, cursor);
		zip.closeEntry();
	}
	protected abstract void finishData(T output, Cursor cursor) throws Throwable;

	@Override public void initImages(Cursor cursor) {
		// nop
	}
	@Override public void saveImage(@Deprecated File file, Cursor cursor) throws IOException {
		byte[] image = getImage(cursor);
		long unixUtc = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE_TIME));
		String fileName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		String comment = ExporterTask.buildComment(cursor);
		IOTools.store(zip, image, unixUtc, fileName, comment);
	}

	@Override public void noImage(Cursor cursor) throws Throwable {
		// nop
	}
	@Override public void finishImages(Cursor cursor) throws Throwable {
		// nop
	}
	public byte[] getImage(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		Type type = Type.from(cursor, CommonColumns.TYPE);
		switch (type) {
			case Property:
				cursor = App.db().getPropertyImage(id);
				break;
			case Room:
				cursor = App.db().getRoomImage(id);
				break;
			case Item:
				cursor = App.db().getItemImage(id);
				break;
		}
		return DatabaseTools.singleBlob(cursor, "_dataBlob");
	}
}
