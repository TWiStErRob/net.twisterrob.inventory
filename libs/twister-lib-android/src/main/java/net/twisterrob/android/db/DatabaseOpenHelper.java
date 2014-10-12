package net.twisterrob.android.db;

import java.io.*;

import org.slf4j.*;

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

import static net.twisterrob.android.utils.tools.DBTools.*;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	public static final int CURSOR_NO_COLUMN = -1;

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseOpenHelper.class);
	private static final String DB_SCHEMA_FILE = "%s.schema.sql";
	private static final String DB_DATA_FILES = "%s.data.sql";
	private static final String DB_CLEAN_FILE = "%s.clean.sql";
	private static final String DB_DEVELOPMENT_FILE = "%s.development.sql";
	private static final CursorFactory s_factory = VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT
			? new LoggingCursorFactory(BuildConfig.DEBUG)
			: null;

	protected final AssetManager assets;
	private final boolean hasWriteExternalPermission;
	private final String dbName;
	private boolean devMode;
	private boolean dumpOnOpen;

	public DatabaseOpenHelper(Context context, String dbName, int dbVersion) {
		super(context, dbName, s_factory, dbVersion);
		this.assets = context.getAssets();
		this.dbName = dbName;
		this.hasWriteExternalPermission = AndroidTools.hasPermission(context, WRITE_EXTERNAL_STORAGE);
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	public boolean isDevMode() {
		return devMode;
	}

	public void setDumpOnOpen(boolean dumpOnOpen) {
		this.dumpOnOpen = dumpOnOpen;
	}
	public boolean isDumpOnOpen() {
		return dumpOnOpen;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (devMode) {
			backupDB(db, "onCreate");
		}
		onConfigureCompat(db);
		LOG.debug("Creating database: {}", dbToString(db));
		execFile(db, String.format(DB_CLEAN_FILE, dbName));
		execFile(db, String.format(DB_SCHEMA_FILE, dbName));
		execFile(db, String.format(DB_DATA_FILES, dbName));
		LOG.info("Created database: {}", dbToString(db));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (devMode) {
			backupDB(db, "onUpgrade_" + oldVersion + "-" + newVersion);
		}
		onConfigureCompat(db);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		LOG.debug("Opening database: {}", dbToString(db));
		onConfigureCompat(db);
		super.onOpen(db);
		if (devMode) {
			backupDB(db, "onOpen_beforeDev");
			onCreate(db); // for DB development, always clear and initialize
			execFile(db, String.format(DB_DEVELOPMENT_FILE, dbName));
			backupDB(db, "onOpen_afterDev");
		}
		LOG.info("Opened database: {}", dbToString(db));
		if (dumpOnOpen) {
			backupDB(db, "onOpen");
		}
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys=ON;"); // db.setForeignKeyConstraintsEnabled(true);
		}
	}

	private void onConfigureCompat(SQLiteDatabase db) {
		if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
			onConfigure(db);
		} // otherwise onConfigure was already called by super
	}

	private void execFile(SQLiteDatabase db, String dbSchemaFile) {
		LOG.debug("{} Executing file {} into database: {}", dbSchemaFile, dbToString(db));
		long time = System.nanoTime();

		realExecuteFile(db, dbSchemaFile);

		long end = System.nanoTime();
		long executionTime = (end - time) / 1000 / 1000;
		LOG.debug("Finished ({} ms) executed file {} into database: {}", executionTime, dbSchemaFile, dbToString(db));
	}

	@SuppressWarnings("resource")
	private void realExecuteFile(SQLiteDatabase db, String dbSchemaFile) {
		InputStream s = null;
		String statement = null;
		BufferedReader reader = null;
		try {
			s = assets.open(dbSchemaFile);
			reader = new BufferedReader(new InputStreamReader(s, IOTools.ENCODING));
			while ((statement = DatabaseOpenHelper.getNextStatement(reader)) != null) {
				if (statement.trim().isEmpty()) {
					continue;
				}
				db.execSQL(statement);
			}
		} catch (SQLException ex) {
			String message = String.format("Error creating database from file: %s while executing\n%s",
					dbSchemaFile, statement);
			LOG.error(message, ex);
			throw new IllegalStateException(message, ex);
		} catch (IOException ex) {
			String message = String.format("Error creating database from file: %s", dbSchemaFile);
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
			String fileName = dbName + "." + when + ".sqlite";
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
