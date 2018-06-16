package net.twisterrob.android.db;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.*;

import android.annotation.*;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build.*;
import android.os.Environment;
import android.support.annotation.*;

import static android.Manifest.permission.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.java.annotations.DebugHelper;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;

@RequiresApi(VERSION_CODES.GINGERBREAD_MR1)
@SuppressLint("ObsoleteSdkInt")
public class DatabaseOpenHelper extends SQLiteOpenHelperCompat {
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
		LOG.debug("Creating database: {}", dbToString(db));
		super.onCreate(db);
		if (devMode) {
			backupDB(db, "onCreate");
		}
		execFiles(db, getSchemaFiles());
		execFiles(db, getDataFiles());
		execFiles(db, getScriptFiles());
		LOG.info("Created database: {}", dbToString(db));
	}

	public void onDestroy(SQLiteDatabase db) {
		LOG.debug("Destroying database: {}", dbToString(db));
		super.onDestroy(db);
		if (devMode) {
			backupDB(db, "onDestroy");
		}
		execFiles(db, getCleanFiles());
		LOG.info("Destroyed database: {}", dbToString(db));
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onUpgrade(db, oldVersion, newVersion);
		for (int version = oldVersion + 1; version <= newVersion; ++version) {
			if (devMode) {
				backupDB(db, "onUpgrade_" + oldVersion + "-" + newVersion);
			}

			LOG.trace("Upgrading database v{} to v{}, step {} to {}: {}",
					oldVersion, newVersion, version - 1, version, dbToString(db));
			long time = System.nanoTime();

			onUpgradeStep(db, version - 1, version);

			long end = System.nanoTime();
			long executionTime = (end - time) / 1000 / 1000;
			LOG.debug("Upgraded ({} ms) database v{} to v{}, step {} to {}: {}",
					executionTime, oldVersion, newVersion, version - 1, version, dbToString(db));
		}
	}
	/**
	 * Executed one-by-one, the difference between {@code oldVersion} and {@code newVersion} will be always {@code 1}.
	 */
	protected void onUpgradeStep(SQLiteDatabase db, int oldVersion, int newVersion) {
		execFiles(db, getUpgradeFiles(oldVersion, newVersion));
	}

	@Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (devMode) {
			super.onConfigureCompat(db);
			for (int version = oldVersion; version > newVersion; --version) {
				LOG.trace("Downgrading database v{} to v{}, step {} to {}: {}",
						oldVersion, newVersion, version, version - 1, dbToString(db));
			}
		} else {
			super.onDowngrade(db, oldVersion, newVersion);
		}
	}

	@Override public void onOpen(SQLiteDatabase db) {
		LOG.debug("Opening database: {}", dbToString(db));
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
		if (dumpOnOpen) {
			backupDB(db, "onOpen");
		}
		LOG.info("Opened database: {}", dbToString(db));
	}

	/** This is the first interaction with the DB whenever it's being opened. */
	@Override public void onConfigure(SQLiteDatabase db) {
		LOG.trace("Initializing database: {}", dbToString(db));
		super.onConfigure(db);
		if (!db.isReadOnly()) {
			enableForeignKeys(db);
			enableRecursiveTriggers(db);
		} else {
			throw new IllegalStateException("Database is read only");
		}
	}

	@RequiresApi(VERSION_CODES.FROYO)
	protected void enableRecursiveTriggers(SQLiteDatabase db) {
		// SQLite 3.6.18 (2009-09-11)
		// Recursive triggers can be enabled using the PRAGMA recursive_triggers statement.
		if (VERSION.SDK_INT < VERSION_CODES.FROYO) {
			throw new IllegalStateException("Cannot enable foreign keys, SQLite version doesn't support it.");
		}
		DatabaseTools.setPragma(db, Pragma.RECURSIVE_TRIGGERS, true);
		if (!DatabaseTools.isPragma(db, Pragma.RECURSIVE_TRIGGERS)) {
			throw new IllegalStateException("Cannot enable " + Pragma.RECURSIVE_TRIGGERS);
		}
	}

	@RequiresApi(VERSION_CODES.FROYO)
	@TargetApi(VERSION_CODES.JELLY_BEAN)
	protected void enableForeignKeys(SQLiteDatabase db) {
		// SQLite 3.6.19 (2009-10-14)
		// Added support for foreign key constraints. Foreign key constraints are disabled by default.
		if (VERSION.SDK_INT < VERSION_CODES.FROYO) {
			throw new IllegalStateException("Cannot enable foreign keys, SQLite version doesn't support it.");
		}
		if (VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
			db.setForeignKeyConstraintsEnabled(true);
		} else {
			DatabaseTools.setPragma(db, Pragma.FOREIGN_KEYS, true);
		}
		if (!DatabaseTools.isPragma(db, Pragma.FOREIGN_KEYS)) {
			throw new IllegalStateException("Cannot enable " + Pragma.FOREIGN_KEYS);
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

	private void execFiles(SQLiteDatabase db, String... dbFiles) {
		for (String dbFile : dbFiles) {
			execFile(db, dbFile);
		}
	}
	private void execFile(SQLiteDatabase db, String dbFile) {
		LOG.trace("Executing file {} into database: {}", dbFile, dbToString(db));
		long time = System.nanoTime();

		realExecuteFile(db, dbFile);

		long end = System.nanoTime();
		long executionTime = (end - time) / 1000 / 1000;
		LOG.debug("Finished ({} ms) executed file {} into database: {}", executionTime, dbFile, dbToString(db));
	}

	/**
	 * @throws IOException never, but in the future may throw if asset access fails
	 * @throws SQLException if the loaded file is not valid SQL
	 */
	@SuppressWarnings("RedundantThrows")
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
		SQLStatementParser parser = null;
		try {
			parser = new SQLStatementParser(reader);
			while ((statement = parser.getNextStatement()) != null) {
				if (statement.isEmpty()) {
					continue;
				}
				if (parser.isPragma(statement)) {
					// For some reason some PRAGMAs need this, even when they're setters or function calls.
					// For example, db.execSQL("PRAGMA secure_delete = FALSE;") throws this:
					// "Caused by: android.database.sqlite.SQLiteException: unknown error (code 0):
					// Queries can be performed using SQLiteDatabase query or rawQuery methods only."
					DatabaseTools.consume(db.rawQuery(statement, NO_ARGS));
				} else {
					db.execSQL(statement);
				}
			}
		} catch (SQLException ex) {
			String message = String.format(Locale.ROOT, "Error while executing\n%s", statement);
			LOG.error(message);
			SQLException decorated = new SQLException(message);
			//noinspection UnnecessaryInitCause SQLException(String,ex) was introduced in API 16
			decorated.initCause(ex);
			throw decorated;
		} finally {
			IOTools.ignorantClose(reader, parser);
		}
	}

	private static class SQLStatementParser implements Closeable {
		private static final String LINE_SEPARATOR = System.getProperty("line.separator");
		private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*$");
		private static final Pattern COMMENTED_LINE = Pattern.compile("^\\s*--.*");
		private static final Pattern END_OF_STATEMENT = Pattern.compile(".*;\\s*(?!--NOTEOS)(--.*)?$");
		/**
		 * Matches the usual pragma commands (with or without schemas):
		 * <ul>
		 * <li>PRAGMA pragma_name;</li>
		 * <li>PRAGMA pragma_name = value;</li>
		 * <li>PRAGMA pragma_name(value);</li>
		 * </ul>
		 * Skips parsing the end of the statement, only matches until it's clear that it's read/write/function style.
		 * The {@code (schema.)?name} part of the regex is reversed to {@code schemaOrName(.surelyName)?},
		 * because it's more likely that the schema will be missing.
		 * TODO proper schema support, current one is limited, I guess name can be quoted and stuff,
		 * but didn't find definition of (schema-name) from the syntax diagram.
		 */
		private static final Pattern PRAGMA = Pattern.compile("^\\s*PRAGMA\\s+[a-z_]+(?:\\s*\\.\\s*[a-z_]+)?[\\s=(].*$",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		private final BufferedReader reader;

		public SQLStatementParser(Reader reader) {
			this.reader = new BufferedReader(reader);
		}
		private String getNextStatement() throws IOException {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (EMPTY_LINE.matcher(line).matches()) {
					continue;
				}
				if (COMMENTED_LINE.matcher(line).matches()) {
					continue;
				}
				sb.append(line);
				sb.append(LINE_SEPARATOR);
				if (END_OF_STATEMENT.matcher(line).matches()) {
					return sb.toString().trim(); // ends in a semicolon -> end of statement
				}
			}
			return null;
		}
		@Override public void close() throws IOException {
			reader.close();
		}
		public boolean isPragma(String statement) {
			return PRAGMA.matcher(statement).matches();
		}
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
			if (!hasWriteExternalPermission) {
				LOG.debug("No {} permission to back up DB at {}", WRITE_EXTERNAL_STORAGE, when);
			}
		}
	}
}
