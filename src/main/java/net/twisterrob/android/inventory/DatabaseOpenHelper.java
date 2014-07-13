package net.twisterrob.android.inventory;

import java.io.*;

import org.slf4j.*;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;

import net.twisterrob.android.utils.tools.*;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseOpenHelper.class);

	private static final String DB_SCHEMA_FILE = "MagicHomeInventory.v1.schema.sql";
	private static final String[] DB_DATA_FILES = {"MagicHomeInventory.v1.data.sql"};
	private static final String DB_CLEAN_FILE = "MagicHomeInventory.v1.clean.sql";
	private static final String DB_DEVELOPMENT_FILE = "MagicHomeInventory.v1.development.sql";
	private static final String DB_NAME = "MagicHomeInventory";
	private static final int DB_VERSION = 1;
	private static final CursorFactory s_factory = null;
	//VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT
	//? new LoggingCursorFactory(BuildConfig.DEBUG)
	//: null;

	public DatabaseOpenHelper(final Context context) {
		super(context, DB_NAME, s_factory, DB_VERSION);
	}

	@Override
	public void onOpen(final SQLiteDatabase db) {
		super.onOpen(db);
		LOG.debug("Opening database: {}", DBTools.toString(db));
		backupDB(db, DB_NAME + ".onOpen_BeforeDev.sqlite");
		DatabaseOpenHelper.execFile(db, DB_DEVELOPMENT_FILE);
		backupDB(db, DB_NAME + ".onOpen_AfterDev.sqlite");
		onCreate(db); // FIXME for DB development, always clear and initialize
		LOG.info("Opened database: {}", DBTools.toString(db));
	}

	private static void backupDB(final SQLiteDatabase db, final String fileName) {
		if (BuildConfig.DEBUG) {
			try {
				String target = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName;
				IOTools.copyFile(db.getPath(), target);
				LOG.info("DB backed up to {}", target);
			} catch (IOException ex) {
				LOG.error("Cannot back up DB on open", ex);
			}
		}
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		backupDB(db, DB_NAME + ".onCreate.sqlite");
		LOG.debug("Creating database: {}", DBTools.toString(db));
		DatabaseOpenHelper.execFile(db, DB_CLEAN_FILE);
		DatabaseOpenHelper.execFile(db, DB_SCHEMA_FILE);
		for (String dataFile: DB_DATA_FILES) {
			DatabaseOpenHelper.execFile(db, dataFile);
		}
		LOG.info("Created database: {}", DBTools.toString(db));
	}

	private static void execFile(final SQLiteDatabase db, final String dbSchemaFile) {
		LOG.debug("Executing file {} into database: {}", dbSchemaFile, DBTools.toString(db));
		long time = System.nanoTime();

		DatabaseOpenHelper.realExecuteFile(db, dbSchemaFile);

		long end = System.nanoTime();
		long executionTime = (end - time) / 1000 / 1000;
		LOG.debug("Finished ({} ms) executed file {} into database: {}", executionTime, dbSchemaFile,
				DBTools.toString(db));
	}

	private static void realExecuteFile(final SQLiteDatabase db, final String dbSchemaFile) {
		InputStream s = null;
		String statement = null;
		BufferedReader reader = null;
		try {
			s = App.getInstance().getAssets().open(dbSchemaFile);
			reader = new BufferedReader(new InputStreamReader(s, IOTools.ENCODING));
			while ((statement = DatabaseOpenHelper.getNextStatement(reader)) != null) {
				if (statement.trim().isEmpty()) {
					continue;
				}
				db.execSQL(statement);
			}
		} catch (SQLException ex) {
			LOG.error("Error creating database from file: {} while executing\n{}", dbSchemaFile, statement, ex);
		} catch (IOException ex) {
			LOG.error("Error creating database from file: {}", dbSchemaFile, ex);
		} finally {
			IOTools.ignorantClose(s, reader);
		}
	}

	private static String getNextStatement(final BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.matches("^\\s*$")) {
				continue; // empty lines
			}
			if (line.matches("^\\s*--.*")) {
				continue; // comment lines
			}
			sb.append(line);
			sb.append(IOTools.LINE_SEPARATOR);
			if (line.matches(".*;\\s*(--.*)?$")) {
				return sb.toString(); // ends in a semicolon -> end of statement
			}
		}
		return null;
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		backupDB(db, DB_NAME + ".onUpgrade.sqlite");
		onCreate(db);
	}
}
