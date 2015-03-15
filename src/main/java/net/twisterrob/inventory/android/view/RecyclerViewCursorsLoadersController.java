package net.twisterrob.inventory.android.view;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.content.Loaders;

public abstract class RecyclerViewCursorsLoadersController extends RecyclerViewController {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewCursorsLoadersController.class);

	private final Context context;
	private final LoaderManager manager;
	private final Map<Loaders, CursorRecyclerAdapter> requestedData = new ConcurrentHashMap<>();
	private final Map<CursorRecyclerAdapter, Cursor> pendingData = new HashMap<>();
	private static final CursorRecyclerAdapter NULL = new CursorRecyclerAdapter(null) {
		@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return null;
		}
		@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		}
	};

	public RecyclerViewCursorsLoadersController(Context context, LoaderManager manager, Loaders... loaders) {
		this.context = context;
		this.manager = manager;
		for (Loaders loader : loaders) {
			this.requestedData.put(loader, NULL);
		}
	}

	@Override protected void onViewSet() {
		super.onViewSet();
		setupList();
		synchronized (pendingData) {
			Iterator<Map.Entry<CursorRecyclerAdapter, Cursor>> it;
			for (it = pendingData.entrySet().iterator(); it.hasNext(); ) {
				Entry<CursorRecyclerAdapter, Cursor> pending = it.next();
				pending.getKey().swapCursor(pending.getValue());
				it.remove();
			}
		}
	}

	protected abstract void setupList();

	protected void add(Loaders loader, CursorRecyclerAdapter adapter) {
		if (!requestedData.containsKey(loader)) {
			throw new IllegalArgumentException("Can't register multiple adapters per Loader");
		}
		requestedData.put(loader, adapter);
	}

	public void startLoad(Loaders loader, Bundle args) {
		if (!requestedData.containsKey(loader)) {
			throw new IllegalArgumentException("Please add the loader/adapter mapping first during setupList().");
		}
		manager.initLoader(loader.id(), args, callbacks);
	}

	public void refresh(Loaders loader) {
		// callbacks.finished--;
		manager.getLoader(loader.id()).onContentChanged();
	}
	public void refresh() {
		for (Loaders loader : requestedData.keySet()) {
			refresh(loader);
		}
	}

	LoaderCallbacks<Cursor> callbacks = new LoaderCallbacks<Cursor>() {
		int finished = 0;
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			if (finished == 0) {
				startLoading();
			}
			return Loaders.fromID(id).createLoader(context, args);
		}

		public void onLoaderReset(Loader<Cursor> loader) {
			updateAdapter(Loaders.fromID(loader.getId()), null);
		}

		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			updateAdapter(Loaders.fromID(loader.getId()), data);
		}

		protected void updateAdapter(Loaders loader, Cursor data) {
			CursorRecyclerAdapter adapter = requestedData.get(loader);
			if (adapter == NULL) {
				synchronized (pendingData) {
					pendingData.put(adapter, data);
				}
			} else {
				adapter.swapCursor(data);
				if (++finished == requestedData.size()) {
					finishLoading();
				}
			}
		}
	};
}
