package net.twisterrob.inventory.android.view;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;

public abstract class RecyclerViewCursorLoaderController extends RecyclerViewController {
	private CursorRecyclerAdapter adapter;

	public RecyclerViewCursorLoaderController(View view) {
		super(view);
		adapter = setupList();
	}

	public LoaderCallbacks<Cursor> createLoaderCallbacks() {
		startLoading();
		return new LoaderCallbacks<Cursor>() {
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return createLoader(id, args);
			}

			public void onLoaderReset(Loader<Cursor> loader) {
				updateAdapter(null);
			}

			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				updateAdapter(data);
			}

			protected void updateAdapter(Cursor data) {
				adapter.swapCursor(data);
				finishLoading();
			}
		};
	}
	protected abstract Loader<Cursor> createLoader(int id, Bundle args);
	protected abstract CursorRecyclerAdapter setupList();
}
