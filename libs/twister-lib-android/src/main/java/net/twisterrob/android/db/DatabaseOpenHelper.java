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
import android.support.annotation.WorkerThread;

import static android.Manifest.permission.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.java.annotations.DebugHelper;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	public static final int CURSOR_NO_COLUMN = -1;

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseOpenHelper.class);
	private static final String DB_SCHEMA_FILE = "%s.schema.sql";
	private static final String DB_UPGRADE_FILE = "%s.upgrade.%d.sql";
	private static final String DB_DATA_FILE = "%s.data.sql";
	private static final String DB_INIT_FILE = "%s.init.sql";
	private static final String DB_CLEAN_FILE = "%s.clean.sql";
	private static final String DB_TEST_FILE = "%s.test.sql";
	private static final String DB_DEVELOPMENT_FILE = "%s.development.sql";

	protected final AssetManager assets;
	private final boolean hasWriteExternalPermission;
	private final String dbName;
	private boolean devMode;
	private boolean testMode;
	private boolean dumpOnOpen;
	private boolean allowDump;

	public DatabaseOpenHelper(Context context, String dbName, int dbVersion, boolean isDebugBuild) {
		super(context, dbName, createCursorFactory(isDebugBuild), dbVersion);
		this.assets = context.getAssets();
		this.dbName = dbName;
		this.hasWriteExternalPermission = AndroidTools.hasPermission(context, WRITE_EXTERNAL_STORAGE);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private static CursorFactory createCursorFactory(boolean isDebugBuild) {
		if (isDebugBuild && VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			return new LoggingCursorFactory();
		}
		return null;
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

	public boolean isAllowDump() {
		return allowDump;
	}
	public void setAllowDump(boolean allowDump) {
		this.allowDump = allowDump;
	}

	protected String[] getDataFiles() {
		return new String[] {String.format(Locale.ROOT, DB_DATA_FILE, dbName)};
	}
	protected String[] getSchemaFiles() {
		return new String[] {String.format(Locale.ROOT, DB_SCHEMA_FILE, dbName)};
	}
	protected String[] getScriptFiles() {
		return new String[] {String.format(Locale.ROOT, DB_INIT_FILE, dbName)};
	}
	protected String[] getCleanFiles() {
		return new String[] {String.format(Locale.ROOT, DB_CLEAN_FILE, dbName)};
	}
	protected String[] getTestFiles() {
		return new String[] {String.format(Locale.ROOT, DB_TEST_FILE, dbName)};
	}
	protected String[] getDevelopmentFiles() {
		return new String[] {String.format(Locale.ROOT, DB_DEVELOPMENT_FILE, dbName)};
	}
	protected String[] getUpgradeFiles(int oldVersion, int newVersion) {
		return new String[] {String.format(Locale.ROOT, DB_UPGRADE_FILE, dbName, newVersion)};
	}

	@Override public void onCreate(SQLiteDatabase db) {
		if (devMode) {
			backupDB(db, "onCreate");
		}
		onConfigureCompat(db);
		LOG.debug("Creating database: {}", dbToString(db));
		execFiles(db, getSchemaFiles());
		execFiles(db, getDataFiles());
		execFiles(db, getScriptFiles());
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
	@WorkerThread
	public void execFile(String assetPath) throws IOException, SQLException {
		realExecuteFile(getWritableDatabase(), assetPath);
	}
	private void realExecuteFile(SQLiteDatabase db, String assetPath) {
		try {
			realExecuteFile(db, new InputStreamReader(assets.open(assetPath), IOTools.ENCODING));
		} catch (SQLException | IOException ex) {
			String message = String.format(Locale.ROOT, "Error executing database file: %s", assetPath);
			LOG.error(message, ex);
			throw new IllegalStateException(message, ex);
		}
	}
	private static void realExecuteFile(SQLiteDatabase db, Reader reader) throws IOException, SQLException {
		String statement = null;
		BufferedReader buffered = null;
		try {
			buffered = new BufferedReader(reader);
			while ((statement = getNextStatement(buffered)) != null) {
				if (statement.trim().isEmpty()) {
					continue;
				}
				db.execSQL(statement);
			}
		} catch (SQLException ex) {
			String message = String.format(Locale.ROOT, "Error while executing\n%s", statement);
			LOG.error(message);
			SQLException decorated = new SQLException(message);
			//noinspection UnnecessaryInitCause SQLException(String,ex) was introduced in API 16
			decorated.initCause(ex);
			throw decorated;
		} finally {
			IOTools.ignorantClose(reader, buffered);
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
		if (allowDump && hasWriteExternalPermission) {
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
			if (hasWriteExternalPermission) {
				LOG.warn("No {} permission to back up DB at {}", WRITE_EXTERNAL_STORAGE, when);
			}
		}
	}
}
