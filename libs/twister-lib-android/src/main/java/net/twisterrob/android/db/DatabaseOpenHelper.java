package net.twisterrob.android.db;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build.*;
import android.os.Environment;
import android.support.annotation.*;

import static android.Manifest.permission.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.exceptions.StackTrace;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;

/**
 * Database Interface for SQLite with some convenience methods for asset scripts.
 * The following SQLite version are available in the corresponding Android versions:
 * <table border="1">
 * <thead><tr><th>API</th><th>SQLite</th><th>Android</th><th>Codename</th></tr></thead>
 * <tr><td>24</td><td><a href="http://www.sqlite.org/changes.html#version_3_9_2">3.9.2</a></td><td>7.0</td><td>Nougat</td></tr>
 * <tr><td>23</td><td><a href="http://www.sqlite.org/changes.html#version_3_8_10_2">3.8.10.2</a></td><td>6.0</td><td>Marshmallow</td></tr>
 * <tr><td>22</td><td><a href="http://www.sqlite.org/changes.html#version_3_8_6">3.8.6</a></td><td>5.1.1</td><td>Lollipop MR1</td></tr>
 * <tr><td>21</td><td><a href="http://www.sqlite.org/changes.html#version_3_8_4_3">3.8.4.3</a></td><td>5.0</td><td>Lollipop</td></tr>
 * <tr><td>20</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_11">3.7.11</a></td><td>4.4W.2</td><td>Android Wear</td></tr>
 * <tr><td>19</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_11">3.7.11</a></td><td>4.4</td><td>KitKat</td></tr>
 * <tr><td>18</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_11">3.7.11</a></td><td>4.3</td><td>Jelly Bean MR2</td></tr>
 * <tr><td>17</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_11">3.7.11</a></td><td>4.2</td><td>Jelly Bean MR1</td></tr>
 * <tr><td>16</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_11">3.7.11</a></td><td>4.1</td><td>Jelly Bean</td></tr>
 * <tr><td>15</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_4">3.7.4</a></td><td>4.0.3</td><td>Ice Cream Sandwich MR1</td></tr>
 * <tr><td>14</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_4">3.7.4</a></td><td>4.0</td><td>Ice Cream Sandwich</td></tr>
 * <tr><td>13</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_4">3.7.4</a></td><td>3.2</td><td>Honeycomb MR2</td></tr>
 * <tr><td>12</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_4">3.7.4</a></td><td>3.1</td><td>Honeycomb MR1</td></tr>
 * <tr><td>11</td><td><a href="http://www.sqlite.org/changes.html#version_3_7_4">3.7.4</a></td><td>3.0</td><td>Honeycomb</td></tr>
 * <tr><td>10</td><td><a href="http://www.sqlite.org/changes.html#version_3_6_22">3.6.22</a></td><td>2.3.3</td><td>Gingerbread MR1</td></tr>
 * <tr><td> 9</td><td><a href="http://www.sqlite.org/changes.html#version_3_6_22">3.6.22</a></td><td>2.3.1</td><td>Gingerbread</td></tr>
 * <tr><td> 8</td><td><a href="http://www.sqlite.org/changes.html#version_3_6_22">3.6.22</a></td><td>2.2</td><td>Froyo</td></tr>
 * <tr><td> 7</td><td><a href="http://www.sqlite.org/changes.html#version_3_5_9">3.5.9</a></td><td>2.1</td><td>Eclair</td></tr>
 * <tr><td> 4</td><td><a href="http://www.sqlite.org/changes.html#version_3_5_9">3.5.9</a></td><td>1.6</td><td>Donut</td></tr>
 * <tr><td> 3</td><td><a href="http://www.sqlite.org/changes.html#version_3_5_9">3.5.9</a></td><td>1.5</td><td>Cupcake</td></tr>
 * <tfoot><tr><td colspan="4">
 *     <i>There are some exceptions to these, but they all seem to be in a positive direction:
 *     a newer version available than stated above.</i>
 * </td></tr></tfoot>
 * </table>
 *
 * @see <a href="http://stackoverflow.com/a/4377116/253468">Version of SQLite used in Android?</a>
 */
@RequiresApi(VERSION_CODES.GINGERBREAD_MR1)
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

	protected final Context context;
	protected final AssetManager assets;
	private final boolean hasWriteExternalPermission;
	private final String dbName;
	private boolean devMode;
	private boolean testMode;
	private boolean dumpOnOpen;
	private boolean allowDump;

	public DatabaseOpenHelper(Context context, String dbName, int dbVersion, boolean isDebugBuild) {
		super(context, dbName, createCursorFactory(isDebugBuild), dbVersion);
		this.context = context;
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
	public File getDatabaseFile() {
		return context.getDatabasePath(getDatabaseName());
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

	/** This is the first interaction with the DB whenever it's being opened. */
	@Override public void onConfigure(SQLiteDatabase db) {
		LOG.trace("Initializing database: {}", dbToString(db), new StackTrace());
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
		LOG.trace("Executing file {} into database: {}", dbFile, dbToString(db));
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
