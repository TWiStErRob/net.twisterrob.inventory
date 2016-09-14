package net.twisterrob.android.db;

import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build.*;
import android.support.annotation.*;

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
public class SQLiteOpenHelperCompat extends SQLiteOpenHelper {
	public static final int CURSOR_NO_COLUMN = -1;

	protected final Context context;
	private final String name;
	private boolean needsConfigure;

	public SQLiteOpenHelperCompat(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
		this.name = name;
	}

	/** Polyfill for pre-ICE_CREAM_SANDWICH. */
	@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override public String getDatabaseName() {
		return name;
	}

	public File getDatabaseFile() {
		return context.getDatabasePath(getDatabaseName());
	}

	@Override public synchronized SQLiteDatabase getWritableDatabase() {
		try {
			needsConfigure = true;
			return super.getWritableDatabase();
		} finally {
			needsConfigure = false;
		}
	}
	@Override public synchronized SQLiteDatabase getReadableDatabase() {
		try {
			needsConfigure = true;
			return super.getReadableDatabase();
		} finally {
			needsConfigure = false;
		}
	}

	@CallSuper
	@Override public void onCreate(SQLiteDatabase db) {
		onConfigureCompat(db);
	}

	/** Not a real lifecycle method, exists solely for testing and debugging. */
	// CONSIDER getting rid of this or renaming to something better
	@CallSuper
	public void onDestroy(SQLiteDatabase db) {
		onConfigureCompat(db);
	}

	@CallSuper
	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onConfigureCompat(db);
	}

	@CallSuper
	@Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onConfigureCompat(db);
	}

	@CallSuper
	@Override public void onOpen(SQLiteDatabase db) {
		onConfigureCompat(db);
	}

	/** This is the first interaction with the DB whenever it's being opened. */
	@TargetApi(VERSION_CODES.JELLY_BEAN)
	@Override public void onConfigure(SQLiteDatabase db) {
		// optional override
	}

	/**
	 * Protected to be callable in case the lifecycle methods are overridden without calling super.
	 */
	protected void onConfigureCompat(SQLiteDatabase db) {
		if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN && needsConfigure) {
			boolean transactionWorkaround = db.inTransaction();
			if (transactionWorkaround) {
				// close & reopen transaction opened in super
				// because onConfigure may set pragmas that need to be outside a transaction
				db.setTransactionSuccessful();
				db.endTransaction();
			}
			onConfigure(db);
			needsConfigure = false;
			if (transactionWorkaround) {
				db.beginTransaction();
			}
		} // otherwise onConfigure was already called by super
	}
}
