package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;

public abstract class RecyclerViewCursorLoaderController extends RecyclerViewController {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewCursorLoaderController.class);

	private CursorRecyclerAdapter adapter;
	private Cursor pendingData;

	public void setView(View view) {
		super.setView(view);
		adapter = setupList();
		if (pendingData != null) {
			adapter.swapCursor(pendingData);
			pendingData = null;
		}
	}

	public LoaderCallbacks<Cursor> createLoaderCallbacks() {
		return new LoaderCallbacks<Cursor>() {
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				startLoading();
				return createLoader(id, args);
			}

			public void onLoaderReset(Loader<Cursor> loader) {
				updateAdapter(null);
			}

			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				updateAdapter(data);
			}

			protected void updateAdapter(Cursor data) {
				if (adapter == null) {
					pendingData = data;
				} else {
					adapter.swapCursor(data);
					finishLoading();
				}
			}
		};
	}
	protected abstract Loader<Cursor> createLoader(int id, Bundle args);
	protected abstract CursorRecyclerAdapter setupList();
}
