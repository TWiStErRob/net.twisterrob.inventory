package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.content.Loader;

import net.twisterrob.inventory.android.content.Loaders;

public abstract class RecyclerViewLoadersController extends RecyclerViewCursorLoaderController {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewLoadersController.class);

	private final Context context;
	private final LoaderManager manager;
	private final Loaders loader;

	public RecyclerViewLoadersController(FragmentActivity activity, Loaders loader) {
		this(activity, activity.getSupportLoaderManager(), loader);
	}
	public RecyclerViewLoadersController(Fragment fragment, Loaders loader) {
		this(fragment.getActivity(), fragment.getLoaderManager(), loader);
	}
	public RecyclerViewLoadersController(Context context, LoaderManager manager, Loaders loader) {
		this.context = context;
		this.manager = manager;
		this.loader = loader;
	}

	public Loaders getLoader() {
		return loader;
	}

	@Override protected Loader<Cursor> createLoader(int id, Bundle args) {
		return Loaders.fromID(id).createLoader(context, args);
	}

	public void startLoad(Bundle args) {
		manager.initLoader(loader.ordinal(), args, createLoaderCallbacks());
	}

	public void refresh() {
		manager.getLoader(loader.ordinal()).onContentChanged();
	}
}
