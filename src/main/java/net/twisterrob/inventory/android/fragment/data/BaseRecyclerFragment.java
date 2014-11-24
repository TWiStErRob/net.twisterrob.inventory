package net.twisterrob.inventory.android.fragment.data;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.OnClickListener;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.*;

public abstract class BaseRecyclerFragment<T> extends BaseFragment<T> implements RecyclerViewItemEvents {
	protected RecyclerView list;
	protected CursorRecyclerAdapter adapter;
	private SwipeRefreshLayout progress;

	protected LoaderCallbacks<Cursor> createListLoaderCallbacks() {
		progress.post(new Runnable() {
			@Override public void run() {
				progress.setRefreshing(true);
			}
		});
		return new LoaderCallbacks<Cursor>() {
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return Loaders.fromID(id).createLoader(getContext(), args);
			}

			public void onLoaderReset(Loader<Cursor> loader) {
				updateAdapter(null);
			}

			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				updateAdapter(data);
			}

			protected void updateAdapter(Cursor data) {
				adapter.swapCursor(data);
				progress.post(new Runnable() {
					@Override public void run() {
						progress.setRefreshing(false);
					}
				});
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.generic_list, container, false);

		if (canCreateNew()) {
			View fab = root.findViewById(R.id.fab);
			fab.setVisibility(View.VISIBLE);
			fab.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					onCreateNew();
				}
			});
		}
		progress = (SwipeRefreshLayout)root.findViewById(android.R.id.progress);

		list = (RecyclerView)root.findViewById(android.R.id.list);
		adapter = setupList();
		list.setAdapter(new HeaderViewRecyclerAdapter(adapter));
		return root;
	}

	protected abstract CursorRecyclerAdapter setupList();

	protected abstract boolean canCreateNew();
	protected abstract void onCreateNew();
}
