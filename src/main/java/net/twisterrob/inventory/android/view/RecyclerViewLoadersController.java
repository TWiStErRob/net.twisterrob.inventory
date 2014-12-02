package net.twisterrob.inventory.android.view;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;

import net.twisterrob.inventory.android.content.Loaders;

public abstract class RecyclerViewLoadersController extends RecyclerViewCursorLoaderController {
	private final LoaderManager manager;
	private final Loaders loader;

	public RecyclerViewLoadersController(LoaderManager manager, View view, Loaders loader) {
		super(view);
		this.manager = manager;
		this.loader = loader;
	}

	@Override protected Loader<Cursor> createLoader(int id, Bundle args) {
		return Loaders.fromID(id).createLoader(list.getContext(), args);
	}

	public void startLoad(Bundle args) {
		manager.initLoader(loader.ordinal(), args, createLoaderCallbacks());
	}

	public void refresh() {
		manager.getLoader(loader.ordinal()).forceLoad();
	}
}
