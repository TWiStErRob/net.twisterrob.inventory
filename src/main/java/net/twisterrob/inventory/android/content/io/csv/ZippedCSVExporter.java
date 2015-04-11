package net.twisterrob.inventory.android.content.io.csv;

import java.io.*;

import org.apache.commons.csv.CSVPrinter;
import org.slf4j.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.io.ZippedExporter;

public class ZippedCSVExporter extends ZippedExporter<CSVPrinter> {
	private static final Logger LOG = LoggerFactory.getLogger(ZippedCSVExporter.class);
	private Object[] values;

	public ZippedCSVExporter() {
		super(Paths.BACKUP_CSV_FILENAME);
	}

	@Override protected CSVPrinter initData(OutputStream dataStream, Cursor cursor) throws IOException {
		values = new Object[CSVConstants.FORMAT.getHeader().length];
		return CSVConstants.FORMAT.print(new PrintStream(dataStream, false, CSVConstants.ENCODING));
	}

	@Override protected void writeData(CSVPrinter output, Cursor cursor) throws IOException {
		for (int i = 0; i < values.length; ++i) {
			String column = CSVConstants.COLUMNS[i];
			if (column != null) {
				values[i] = cursor.getString(cursor.getColumnIndexOrThrow(column));
			} else {
				values[i] = null;
			}
		}
		output.printRecord(values);
	}

	@Override protected void finishData(CSVPrinter output, Cursor cursor) throws IOException {
		output.flush();
	}
}
