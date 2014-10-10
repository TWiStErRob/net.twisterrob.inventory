package net.twisterrob.android.db;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public final class LoggingCursorFactory implements CursorFactory {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingCursorFactory.class);

	private final boolean m_debugQueries;

	public LoggingCursorFactory() {
		this(false);
	}

	public LoggingCursorFactory(boolean debugQueries) {
		m_debugQueries = debugQueries;
	}

	public Cursor newCursor(final SQLiteDatabase db, final SQLiteCursorDriver masterQuery, final String editTable,
			final SQLiteQuery query) {
		if (m_debugQueries) {
			LoggingCursorFactory.LOG.trace("{}", query);
		}
		return new SQLiteCursor(masterQuery, editTable, query);
	}
}
