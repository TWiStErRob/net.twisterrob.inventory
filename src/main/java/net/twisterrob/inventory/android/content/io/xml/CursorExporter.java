package net.twisterrob.inventory.android.content.io.xml;

import java.io.*;

import android.database.Cursor;

public interface CursorExporter {
	void start(OutputStream dataStream, Cursor cursor) throws IOException;
	void processEntry(Cursor cursor) throws IOException;
	void finish(Cursor cursor) throws IOException;
}
