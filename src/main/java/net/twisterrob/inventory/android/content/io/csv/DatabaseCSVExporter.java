package net.twisterrob.inventory.android.content.io.csv;

import java.io.*;

import org.apache.commons.csv.CSVPrinter;

import android.database.Cursor;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;

// TODO check http://stackoverflow.com/questions/13229294/how-do-i-create-a-google-spreadsheet-with-a-service-account-and-share-to-other-g
public class DatabaseCSVExporter implements Closeable {
	private final Cursor cursor;

	public DatabaseCSVExporter() {
		this.cursor = App.db().export();
	}

	public void export(OutputStream out) throws IOException {
		@SuppressWarnings("resource")
		CSVPrinter printer = CSVConstants.FORMAT.print(new PrintStream(out, false, CSVConstants.ENCODING));
		try {
			cursor.moveToPosition(-1);
			Object[] values = new Object[cursor.getColumnCount()];
			while (cursor.moveToNext()) {
				fillValues(values);
				printer.printRecord(values);
			}
		} finally {
			IOTools.ignorantClose(printer);
		}
	}

	private void fillValues(Object[] values) {
		for (int i = 0; i < values.length; ++i) {
			values[i] = cursor.getString(i);
		}
	}

	public void close() throws IOException {
		cursor.close();
	}
}
