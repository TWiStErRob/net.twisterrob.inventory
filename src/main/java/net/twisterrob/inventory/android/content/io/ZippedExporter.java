package net.twisterrob.inventory.android.content.io;

import java.io.*;
import java.util.zip.*;

import android.database.Cursor;

import net.twisterrob.android.utils.tools.IOTools;

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
	@Override public void saveImage(File file, Cursor cursor) throws IOException {
		FileInputStream imageFile = new FileInputStream(file);
		try {
			ZipEntry entry = new ZipEntry(file.getName());
			entry.setTime(file.lastModified());
			entry.setMethod(ZipEntry.STORED);
			entry.setSize(file.length());
			entry.setCrc(IOTools.crc(file));
			entry.setComment(ExporterTask.buildComment(cursor));
			zip.putNextEntry(entry);
		} catch (IOException ex) {
			IOTools.ignorantClose(imageFile);
		}
		IOTools.copyStream(imageFile, zip, false);
		zip.closeEntry();
	}
	@Override public void noImage(Cursor cursor) throws Throwable {
		// nop
	}
	@Override public void finishImages(Cursor cursor) throws Throwable {
		// nop
	}
}
