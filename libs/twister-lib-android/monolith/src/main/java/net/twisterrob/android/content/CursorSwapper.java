package net.twisterrob.android.content;

import android.database.Cursor;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

public abstract class CursorSwapper implements LoaderCallbacks<Cursor> {
	protected final CursorAdapter adapter;

	public CursorSwapper(CursorAdapter adapter) {
		if (adapter == null) {
			throw new IllegalArgumentException("Adapter cannot be null");
		}
		this.adapter = adapter;
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		updateAdapter(null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		updateAdapter(data);
	}

	protected void updateAdapter(Cursor data) {
		adapter.swapCursor(data);
	}
}
