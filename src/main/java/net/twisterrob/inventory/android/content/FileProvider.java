package net.twisterrob.inventory.android.content;

import java.io.FileNotFoundException;

import org.slf4j.*;

import android.content.*;
import android.content.pm.ProviderInfo;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.os.StrictMode.ThreadPolicy;

import net.twisterrob.android.utils.tools.DatabaseTools;

public class FileProvider extends android.support.v4.content.FileProvider {
	private static final Logger LOG = LoggerFactory.getLogger(FileProvider.class);

	@Override public boolean onCreate() {
		try {
			boolean result = super.onCreate();
			LOG.trace("{}.onCreate(): {}", this, result);
			return result;
		} catch (RuntimeException ex) {
			LOG.trace("{}.onCreate(): thrown {}", this, ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}
	@Override public void attachInfo(Context context, ProviderInfo info) {
		try {
			LOG.trace("{}.attachInfo({}, {})", this, context, info);
			ThreadPolicy policy = StrictMode.allowThreadDiskReads();
			try {
				super.attachInfo(context, info);
			} finally {
				StrictMode.setThreadPolicy(policy);
			}
		} catch (RuntimeException ex) {
			LOG.trace("{}.attachInfo({}, {}): thrown {}", this, context, info, ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}
	@Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		try {
			Cursor result = super.query(uri, projection, selection, selectionArgs, sortOrder);
			result = fix(result, projection);
			LOG.trace("{}.query({}, {}, {}, {}, {}): {}",
					this, uri, projection, selection, selectionArgs, sortOrder,
					DatabaseTools.dumpCursorToString(result));
			return result;
		} catch (RuntimeException ex) {
			LOG.trace("{}.query({}, {}, {}, {}, {}): thrown {}", this, uri, ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}

	/**
	 * Google+ Photos displays the image acquired from openFile and then for some reason also tries to query for _data,
	 * but {@link android.support.v4.content.FileProvider} returns a one line zero column cursor.
	 * @return original cursor or a 1x1 cursor with a NULL _data column.
	 */
	private Cursor fix(Cursor result, String... projection) {
		if (projection.length == 1 && "_data".equals(projection[0])
				&& result.getCount() == 1 && result.getColumnCount() == 0) {
			//noinspection resource the value is returned, the receiver will close it
			MatrixCursor newCursor = new MatrixCursor(new String[] {"_data"});
			newCursor.addRow(new Object[] {null});
			result.close();
			result = newCursor;
		}
		return result;
	}

	@Override public String getType(Uri uri) {
		try {
			String result = super.getType(uri);
			LOG.trace("{}.getType({}): {}", this, uri, result);
			return result;
		} catch (RuntimeException ex) {
			LOG.trace("{}.getType({}): thrown {}", this, uri, ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}
	@Override public Uri insert(Uri uri, ContentValues values) {
		try {
			Uri result = super.insert(uri, values);
			LOG.trace("{}.insert({}, {}): {}", this, uri, values, result);
			return result;
		} catch (RuntimeException ex) {
			LOG.trace("{}.insert({}, {}): thrown {}", this, uri, values, ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}
	@Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		try {
			int result = super.update(uri, values, selection, selectionArgs);
			LOG.trace("{}.update({}, {}, {}, {}): {}", this, uri, values, selection, selectionArgs, result);
			return result;
		} catch (RuntimeException ex) {
			LOG.trace("{}.update({}, {}, {}, {}): thrown {}", this, uri, values, selection, selectionArgs,
					ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}
	@Override public int delete(Uri uri, String selection, String[] selectionArgs) {
		try {
			int result = super.delete(uri, selection, selectionArgs);
			LOG.trace("{}.delete({}, {}, {}): {}", this, uri, selection, selectionArgs, result);
			return result;
		} catch (RuntimeException ex) {
			LOG.trace("{}.delete({}, {}, {}): thrown {}", this, uri, selection, selectionArgs,
					ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}
	@Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		try {
			ParcelFileDescriptor result = super.openFile(uri, mode);
			LOG.trace("{}.openFile({}, {}): {}", this, uri, mode, result);
			return result;
		} catch (RuntimeException | FileNotFoundException ex) {
			LOG.trace("{}.openFile({}, {}): thrown {}", this, uri, mode, ex.getClass().getSimpleName(), ex);
			throw ex;
		}
	}

	@Override public String toString() {
		return getClass().getSuperclass().getName() + "@" + Integer.toHexString(System.identityHashCode(this));
	}
}
