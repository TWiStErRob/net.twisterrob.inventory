package net.twisterrob.android.db;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;

import net.twisterrob.java.annotations.DebugHelper;

@TargetApi(VERSION_CODES.HONEYCOMB)
@RequiresApi(VERSION_CODES.HONEYCOMB)
@DebugHelper
public final class LoggingCursorFactory implements CursorFactory {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingCursorFactory.class);

	@Override public Cursor newCursor(SQLiteDatabase db,
			SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
		LOG.trace("{}", query);
		return new SQLiteCursor(masterQuery, editTable, query);
	}
}
