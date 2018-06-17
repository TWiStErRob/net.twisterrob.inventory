package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.content.contract.InventoryLoader;
import net.twisterrob.inventory.android.content.contract.InventoryLoader.LoadersCallbacksAdapter;

public abstract class RecyclerViewLoadersController
		extends RecyclerViewLoaderController<CursorRecyclerAdapter<?>, Cursor> {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewLoadersController.class);

	private final InventoryLoader loader;

	public RecyclerViewLoadersController(@NonNull Fragment fragment, @NonNull InventoryLoader loader) {
		super(fragment);
		this.loader = loader;
	}

	public @NonNull InventoryLoader getLoader() {
		return loader;
	}

	@Override protected void setData(@NonNull CursorRecyclerAdapter<?> adapter, @Nullable Cursor data) {
		adapter.swapCursor(data);
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

	public void startLoad(@Nullable Bundle args) {
		getLoaderManager().initLoader(loader.id(), args, createLoaderCallbacks());
	}

	public void refresh() {
		getLoaderManager().getLoader(loader.id()).onContentChanged();
	}
}
