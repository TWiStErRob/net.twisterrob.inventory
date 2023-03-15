package net.twisterrob.inventory.android.view;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.content.contract.InventoryLoader;
import net.twisterrob.inventory.android.content.contract.InventoryLoader.LoadersCallbacksAdapter;

public abstract class RecyclerViewLoadersController
		extends RecyclerViewLoaderController<CursorRecyclerAdapter<?>, Cursor> {
	private final InventoryLoader loader;

	public RecyclerViewLoadersController(@NonNull Fragment fragment, @NonNull InventoryLoader loader) {
		super(fragment);
		this.loader = loader;
	}

	public @NonNull InventoryLoader getLoader() {
		return loader;
	}

	@Override protected void setData(@NonNull CursorRecyclerAdapter<?> adapter, @Nullable Cursor data) {
		Cursor oldCursor = adapter.swapCursor(data);
		if (data == null && oldCursor != null) {
			// This is meant to close the Cursor that may be created by a subclass of the adapter.
			// swapCursor might actually set a cursor inside the adapter, that's not the same as data.
			// When data is null, it means we want to forget what's in the adapter, so we close the original.
			// If we close it always, i.e. adapter.changeCursor(data), then we might close used cursors.
			// This happens because some Cursors might be shared between different screens via the Loaders.
			oldCursor.close();
		}
	}

	private @NonNull LoaderCallbacks<Cursor> createLoaderCallbacks() {
		return loader.createCallbacks(getContext(), new LoadersCallbacksAdapter() {
			@Override public void preOnCreateLoader(int id, Bundle args) {
				startLoading();
			}
			@Override public void preOnLoadFinished(Loader<Cursor> loader, @Nullable Cursor data) {
				updateAdapter(data);
			}
			@Override public void preOnLoaderReset(Loader<Cursor> loader) {
				updateAdapter(null);
			}
		});
	}

	@Override public void startLoad(@Nullable Bundle args) {
		getLoaderManager().initLoader(loader.id(), args, createLoaderCallbacks());
	}

	@Override public void refresh() {
		getLoaderManager().getLoader(loader.id()).onContentChanged();
	}
}
