package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.inventory.android.view.CursorSwapper;

public class BaseListFragment<T> extends BaseFragment<T> {
	protected static final String DYN_CursorAdapter = "cursorAdapter";
	protected static final String DYN_List = "list";

	protected AbsListView list;
	protected CursorAdapter adapter;

	protected void setAdapter(ListAdapter adapter) {
		list.setAdapter(adapter);
	}

	protected void swapEmpty(View newEmptyView) {
		View current = list.getEmptyView();
		if (current != null) {
			current.setVisibility(View.GONE);
		}
		list.setEmptyView(newEmptyView);
	}

	protected LoaderCallbacks<Cursor> createListLoaderCallbacks() {
		return new CursorSwapper(getActivity(), adapter) {
			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				super.onLoadFinished(loader, data);
				swapEmpty(getView().findViewById(android.R.id.empty));
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = super.onCreateView(inflater, container, savedInstanceState);

		this.list = (AbsListView)root.findViewById(super.<Integer> getDynamicResource(DYN_List));
		adapter = Adapters.loadCursorAdapter(getActivity(), super.<Integer> getDynamicResource(DYN_CursorAdapter),
				(Cursor)null);

		swapEmpty(root.findViewById(android.R.id.progress));
		setAdapter(adapter);

		return root;
	}
}
