package net.twisterrob.inventory.android.content.contract;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public interface InventoryLoader {
	int id();

	LoaderCallbacks<Cursor> createCallbacks(Context context, LoadersCallbacksListener listener);

	interface LoadersCallbacksListener {
		void preOnCreateLoader(int id, Bundle args);
		void postOnCreateLoader(Loader<Cursor> id, Bundle args);
		void preOnLoadFinished(Loader<Cursor> loader, Cursor data);
		void postOnLoadFinished(Loader<Cursor> loader, Cursor data);
		void preOnLoaderReset(Loader<Cursor> loader);
		void postOnLoaderReset(Loader<Cursor> loader);
	}

	class LoadersCallbacksAdapter implements LoadersCallbacksListener {

		//@formatter:off
		@Override public void preOnCreateLoader(int id, Bundle args) {}
		@Override public void postOnCreateLoader(Loader<Cursor> id, Bundle args) {}
		@Override public void preOnLoadFinished(Loader<Cursor> loader, Cursor data) {}
		@Override public void postOnLoadFinished(Loader<Cursor> loader, Cursor data) {}
		@Override public void preOnLoaderReset(Loader<Cursor> loader) {}
		@Override public void postOnLoaderReset(Loader<Cursor> loader) {}
		//@formatter:on
	}
}
