package net.twisterrob.inventory.android.content;

import java.util.Arrays;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources;
import android.database.*;
import android.database.sqlite.*;

import androidx.annotation.StringRes;

import net.twisterrob.java.utils.StringTools;

public class VariantDatabase {
	private static final Logger LOG = LoggerFactory.getLogger(Database.class); // intentionally other class

	protected final Resources m_resources;

	public VariantDatabase(Resources resources) {
		m_resources = resources;
	}

	protected void execSQL(SQLiteDatabase db, @StringRes int queryResource, Object... params) {
		db.execSQL(m_resources.getString(queryResource), params);
	}

	protected Cursor rawQuery(SQLiteDatabase db, @StringRes int queryResource, Object... params) {
		try {
			Cursor cursor = db.rawQuery(m_resources.getString(queryResource), StringTools.toStringArray(params));
			cursor.getCount(); // make sure the query runs now
			return cursor;
		} catch (Exception ex) {
			throw new IllegalStateException(
					m_resources.getResourceEntryName(queryResource) + ": " + Arrays.toString(params), ex);
		}
	}

	@SuppressWarnings("resource")
	protected long rawInsert(SQLiteDatabase db, @StringRes int insertResource, Object... params) {
		SQLiteStatement insert = db.compileStatement(m_resources.getString(insertResource));
		try {
			for (int i = 0; i < params.length; ++i) {
				DatabaseUtils.bindObjectToProgram(insert, i + 1, params[i]);
			}
			return insert.executeInsert();
		} finally {
			insert.close();
		}
	}
}
