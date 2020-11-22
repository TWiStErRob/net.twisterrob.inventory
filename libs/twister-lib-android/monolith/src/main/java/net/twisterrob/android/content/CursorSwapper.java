package net.twisterrob.android.content;

import android.database.Cursor;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

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
