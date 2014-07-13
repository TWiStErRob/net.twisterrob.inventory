package net.twisterrob.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

class CursorSwapper implements LoaderCallbacks<Cursor> {
	private final Context context;
	private final CursorAdapter adapter;

	public CursorSwapper(Context context, CursorAdapter adapter) {
		this.context = context;
		this.adapter = adapter;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return Loaders.fromID(id).createLoader(context);
	}
	public void onLoaderReset(Loader<Cursor> loader) {
		updateAdapter(null);
	}
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		updateAdapter(data);
	}

	protected void updateAdapter(Cursor data) {
		//		if (data != null) {
		//			@SuppressWarnings("resource")
		//			MatrixCursor extras = new MatrixCursor(new String[]{"_id", "name"});
		//			extras.addRow(new Object[]{-1, null});
		//			data = new MergeCursor(new Cursor[]{data, extras});
		//		}
		adapter.swapCursor(data);
	}
}