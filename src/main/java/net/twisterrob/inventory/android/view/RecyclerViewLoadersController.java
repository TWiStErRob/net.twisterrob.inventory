package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.Loaders.LoadersCallbacks;

public abstract class RecyclerViewLoadersController extends RecyclerViewCursorLoaderController {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewLoadersController.class);

	private final Context context;
	private final LoaderManagerProvider manager;
	private final Loaders loader;

	public RecyclerViewLoadersController(FragmentActivity activity, Loaders loader) {
		this(activity, new ActivityLoaderManagerProvider(activity), loader);
	}
	public RecyclerViewLoadersController(Fragment fragment, Loaders loader) {
		this(fragment.getActivity(), new FragmentLoaderManagerProvider(fragment), loader);
	}
	public RecyclerViewLoadersController(Context context, LoaderManagerProvider manager, Loaders loader) {
		this.context = context;
		this.manager = manager;
		this.loader = loader;
	}

	public Loaders getLoader() {
		return loader;
	}

	@Override public LoaderCallbacks<Cursor> createLoaderCallbacks() {
		return new LoadersCallbacks(context) {
			@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				startLoading();
				return super.onCreateLoader(id, args);
			}
			@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				updateAdapter(data);
				super.onLoadFinished(loader, data);
			}
			@Override public void onLoaderReset(Loader<Cursor> loader) {
				updateAdapter(null);
				super.onLoaderReset(loader);
			}
		};
	}

	public void startLoad(Bundle args) {
		manager.get().initLoader(loader.id(), args, createLoaderCallbacks());
	}

	public void refresh() {
		manager.get().getLoader(loader.id()).onContentChanged();
	}

	private interface LoaderManagerProvider {
		LoaderManager get();
	}

	private static class ActivityLoaderManagerProvider implements LoaderManagerProvider {
		private final FragmentActivity activity;

		public ActivityLoaderManagerProvider(FragmentActivity activity) {
			this.activity = activity;
		}

		@Override public LoaderManager get() {
			return activity.getSupportLoaderManager();
		}
	}

	private static class FragmentLoaderManagerProvider implements LoaderManagerProvider {
		private final Fragment fragment;

		public FragmentLoaderManagerProvider(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override public LoaderManager get() {
			return fragment.getLoaderManager();
		}
	}
}
