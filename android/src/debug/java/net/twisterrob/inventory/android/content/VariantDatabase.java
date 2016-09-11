package net.twisterrob.inventory.android.content;

import java.util.Arrays;

import org.slf4j.*;

import android.content.res.Resources;
import android.database.*;
import android.database.sqlite.*;
import android.support.annotation.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.java.utils.StringTools;

public class VariantDatabase {
	@SuppressWarnings("LoggerInitializedWithForeignClass") // intentionally other class
	private static final Logger LOG = LoggerFactory.getLogger(Database.class);

	private final Resources m_resources;

	public VariantDatabase(Resources resources) {
		m_resources = resources;
	}

	protected void execSQL(@NonNull SQLiteDatabase db,
			@StringRes int queryResource, @NonNull Object... params) {
		LOG.trace("execSQL({}, {})", m_resources.getResourceEntryName(queryResource), Arrays.toString(params));
		long start = System.nanoTime();
		db.execSQL(m_resources.getString(queryResource), params);
		long end = System.nanoTime();
		if (queryResource != R.string.query_category_cache_update) {
			LOG.debug("execSQL({}, {}): {}ms",
					m_resources.getResourceEntryName(queryResource), Arrays.toString(params), (end - start) / 1000000);
		}
	}

	protected @NonNull Cursor rawQuery(@NonNull SQLiteDatabase db,
			@StringRes int queryResource, @NonNull Object... params) {
		String name = m_resources.getResourceEntryName(queryResource);
		String paramString = Arrays.toString(params);
		LOG.trace("rawQuery({}, {})", name, paramString);
		try {
			long start = System.nanoTime();
			Cursor cursor = db.rawQuery(m_resources.getString(queryResource), StringTools.toStringArray(params));
			cursor.getCount(); // make sure the query runs now
			long end = System.nanoTime();
			LOG.debug("rawQuery({}, {}): {}ms", name, paramString, (end - start) / 1000000);
			return cursor;
		} catch (Exception ex) {
			throw new IllegalStateException(name + ": " + paramString, ex);
		}
	}

	@SuppressWarnings("resource")
	protected long rawInsert(@NonNull SQLiteDatabase db,
			@StringRes int insertResource, @NonNull Object... params) {
		LOG.trace("rawInsert({}, {})", m_resources.getResourceEntryName(insertResource), Arrays.toString(params));

		long start = System.nanoTime();
		SQLiteStatement insert = db.compileStatement(m_resources.getString(insertResource));
		//noinspection TryFinallyCanBeTryWithResources
		try {
			for (int i = 0; i < params.length; ++i) {
				DatabaseUtils.bindObjectToProgram(insert, i + 1, params[i]);
			}
			long rowID = insert.executeInsert();
			long end = System.nanoTime();
			LOG.debug("rawInsert({}, {}): result={} in {}ms",
					m_resources.getResourceEntryName(insertResource), Arrays.toString(params), rowID,
					(end - start) / 1000000);
			return rowID;
		} finally {
			insert.close();
		}
	}
}
