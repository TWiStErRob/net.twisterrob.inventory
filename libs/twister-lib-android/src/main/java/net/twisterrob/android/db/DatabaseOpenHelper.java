package net.twisterrob.android.db;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build.*;
import android.os.Environment;

import static android.Manifest.permission.*;

import net.twisterrob.android.BuildConfig;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.java.annotations.DebugHelper;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	public static final int CURSOR_NO_COLUMN = -1;

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseOpenHelper.class);
	private static final String DB_SCHEMA_FILE = "%s.schema.sql";
	private static final String DB_UPGRADE_FILE = "%s.upgrade.%d.sql";
	private static final String DB_DATA_FILE = "%s.data.sql";
	private static final String DB_CLEAN_FILE = "%s.clean.sql";
	private static final String DB_TEST_FILE = "%s.test.sql";
	private static final String DB_DEVELOPMENT_FILE = "%s.development.sql";
	private static final CursorFactory s_factory = VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT
			? (BuildConfig.DEBUG? new LoggingCursorFactory() : null)
			: null;

	protected final AssetManager assets;
	private final boolean hasWriteExternalPermission;
	private final String dbName;
	private boolean devMode;
	private boolean testMode;
	private boolean dumpOnOpen;

	public DatabaseOpenHelper(Context context, String dbName, int dbVersion) {
		super(context, dbName, s_factory, dbVersion);
		this.assets = context.getAssets();
		this.dbName = dbName;
		this.hasWriteExternalPermission = AndroidTools.hasPermission(context, WRITE_EXTERNAL_STORAGE);
	}

	/** Polyfill for pre-ICE_CREAM_SANDWICH. */
	@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override public String getDatabaseName() {
		return dbName;
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	public boolean isDevMode() {
		return devMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}
	public boolean isTestMode() {
		return testMode;
	}

	public void setDumpOnOpen(boolean dumpOnOpen) {
		this.dumpOnOpen = dumpOnOpen;
	}
	public boolean isDumpOnOpen() {
		return dumpOnOpen;
	}

	protected String[] getDataFiles() {
		return new String[] {String.format(DB_DATA_FILE, dbName)};
	}
	protected String[] getSchemaFiles() {
		return new String[] {String.format(DB_SCHEMA_FILE, dbName)};
	}
	protected String[] getCleanFiles() {
		return new String[] {String.format(DB_CLEAN_FILE, dbName)};
	}
	protected String[] getTestFiles() {
		return new String[] {String.format(DB_TEST_FILE, dbName)};
	}
	protected String[] getDevelopmentFiles() {
		return new String[] {String.format(DB_DEVELOPMENT_FILE, dbName)};
	}
	protected String[] getUpgradeFiles(int oldVersion, int newVersion) {
		return new String[] {String.format(DB_UPGRADE_FILE, dbName, newVersion)};
	}

	@Override public void onCreate(SQLiteDatabase db) {
		if (devMode) {
			backupDB(db, "onCreate");
		}
		onConfigureCompat(db);
		LOG.debug("Creating database: {}", dbToString(db));
		execFiles(db, getSchemaFiles());
		execFiles(db, getDataFiles());
		LOG.info("Created database: {}", dbToString(db));
	}

	public void onDestroy(SQLiteDatabase db) {
		if (devMode) {
			backupDB(db, "onDestroy");
		}
		onConfigureCompat(db);
		LOG.debug("Destroying database: {}", dbToString(db));
		execFiles(db, getCleanFiles());
		LOG.info("Destroyed database: {}", dbToString(db));
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onConfigureCompat(db);
		for (int version = oldVersion + 1; version <= newVersion; ++version) {
			if (devMode) {
				backupDB(db, "onUpgrade_" + oldVersion + "-" + newVersion);
			}
			execFiles(db, getUpgradeFiles(version - 1, version));
		}
	}

	@Override public void onOpen(SQLiteDatabase db) {
		LOG.debug("Opening database: {}", dbToString(db));
		onConfigureCompat(db);
		super.onOpen(db);
		if (testMode) {
			// for DB development, always clear and initialize
			onDestroy(db);
			onCreate(db);
			execFiles(db, getTestFiles());
		}
		if (devMode) {
			if (dumpOnOpen) {
				backupDB(db, "onOpen_backup");
			}
			execFiles(db, getDevelopmentFiles());
		}
		LOG.info("Opened database: {}", dbToString(db));
		if (dumpOnOpen) {
			backupDB(db, "onOpen");
		}
	}

	@Override public void onConfigure(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys=ON;"); // db.setForeignKeyConstraintsEnabled(true);
		}
	}

	@DebugHelper
	public void restore(File from) throws IOException {
		@SuppressWarnings("resource")
		File target = new File(getReadableDatabase().getPath());
		close();
		if (!target.delete()) {
			throw new IOException("Couldn't delete current DB file: " + target.getAbsolutePath());
		}
		IOTools.copyFile(from, target);
	}

	private void onConfigureCompat(SQLiteDatabase db) {
		if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
			onConfigure(db);
		} // otherwise onConfigure was already called by super
	}

	private void execFiles(SQLiteDatabase db, String... dbFiles) {
		for (String dbFile : dbFiles) {
			execFile(db, dbFile);
		}
	}
	private void execFile(SQLiteDatabase db, String dbFile) {
		LOG.debug("Executing file {} into database: {}", dbFile, dbToString(db));
		long time = System.nanoTime();

		realExecuteFile(db, dbFile);

		long end = System.nanoTime();
		long executionTime = (end - time) / 1000 / 1000;
		LOG.debug("Finished ({} ms) executed file {} into database: {}", executionTime, dbFile, dbToString(db));
	}

	private void realExecuteFile(SQLiteDatabase db, String dbSchemaFile) {
		InputStream s = null;
		String statement = null;
		BufferedReader reader = null;
		try {
			//noinspection resource closed in finally
			s = assets.open(dbSchemaFile);
			reader = new BufferedReader(new InputStreamReader(s, IOTools.ENCODING));
			while ((statement = DatabaseOpenHelper.getNextStatement(reader)) != null) {
				if (statement.trim().isEmpty()) {
					continue;
				}
				db.execSQL(statement);
			}
		} catch (SQLException ex) {
			String message = String.format("Error executing database file: %s while executing\n%s",
					dbSchemaFile, statement);
			LOG.error(message); // to have the SQL statement, but need additional log line to display stack trace
			LOG.error("Error executing database file: {}", dbSchemaFile, ex);
			throw new IllegalStateException(message, ex);
		} catch (IOException ex) {
			String message = String.format("Error executing database file: %s", dbSchemaFile);
			LOG.error(message, ex);
			throw new IllegalStateException(message, ex);
		} finally {
			IOTools.ignorantClose(s, reader);
		}
	}

	private static String getNextStatement(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.matches("^\\s*$")) {
				continue; // empty lines
			}
			if (line.matches("^\\s*--.*")) {
				continue; // comment lines
			}
			sb.append(line);
			sb.append(IOTools.LINE_SEPARATOR);
			if (line.matches(".*;\\s*(?!--NOTEOS)(--.*)?$")) {
				return sb.toString(); // ends in a semicolon -> end of statement
			}
		}
		return null;
	}

	private void backupDB(SQLiteDatabase db, String when) {
		if (hasWriteExternalPermission) {
			String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
			String fileName = dbName + "." + date + "." + when + ".sqlite";
			try {
				String target = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName;
				IOTools.copyFile(db.getPath(), target);
				LOG.info("DB backed up to {}", target);
			} catch (IOException ex) {
				LOG.error("Cannot back up DB on open", ex);
			}
		} else {
			LOG.warn("No {} permission to back up DB at {}", WRITE_EXTERNAL_STORAGE, when);
		}
	}
}
