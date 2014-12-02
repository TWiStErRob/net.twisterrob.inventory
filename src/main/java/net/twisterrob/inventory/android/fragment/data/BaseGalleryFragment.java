package net.twisterrob.inventory.android.fragment.data;

import java.util.*;

import org.slf4j.*;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.*;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.*;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.GalleryAdapter.GalleryItemEvents;

public abstract class BaseGalleryFragment<T> extends BaseFragment<T>
		implements GalleryItemEvents, ActionMode.Callback {
	private static final Logger LOG = LoggerFactory.getLogger(BaseGalleryFragment.class);

	private HeaderManager header = null;
	private SelectionAdapter<? extends ViewHolder> selectionAdapter;
	protected RecyclerViewLoadersController listController;

	public void setHeader(BaseFragment headerFragment) {
		this.header = headerFragment != null? new HeaderManager(this, headerFragment) : null;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (header != null) {
			// TODO workaround for https://code.google.com/p/android/issues/detail?id=40537
			header.getHeader().onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		listController.startLoad(createLoadArgs());
	}

	protected Bundle createLoadArgs() {
		return null;
	}

	@Override protected void onRefresh() {
		super.onRefresh();
		if (header != null) {
			header.refresh();
		}
		listController.refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.generic_list, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView(view);
	}

	protected CursorRecyclerAdapter setupList(RecyclerView list) {
		final int columns = getResources().getInteger(R.integer.gallery_columns);
		//StaggeredGridLayoutManager layout = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
		//LinearLayoutManager layout = new LinearLayoutManager(getContext());
		GridLayoutManager layout = new GridLayoutManager(getContext(), columns);
		if (header != null) {
			layout.setSpanSizeLookup(new SpanSizeLookup() {
				@Override public int getSpanSize(int position) {
					return position == 0? columns : 1;
				}
			});
		}
		list.setLayoutManager(layout);
		list.addItemDecoration(new ItemDecoration() {
			private final int margin = getContext().getResources().getDimensionPixelSize(R.dimen.margin);
			@Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
				if (header == null || parent.getChildPosition(view) != 0) {
					outRect.set(margin, margin, margin, margin);
				}
			}
		});
		GalleryAdapter cursorAdapter = new GalleryAdapter(null, this);
		RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter = cursorAdapter;
		if (header != null) {
			adapter = header.wrap(adapter);
		}
		adapter = selectionAdapter = new SelectionAdapter<>(adapter);
		if (header != null) {
			selectionAdapter.setSelectable(0, false);
		}
		list.setAdapter(adapter);
		return cursorAdapter;
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (actionMode != null) {
			outState.putIntegerArrayList("selection", (ArrayList<Integer>)selectionAdapter.getSelectedPositions());
		}
	}

	@Override public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			ArrayList<Integer> selection = savedInstanceState.getIntegerArrayList("selection");
			if (selection != null) {
				tryStartSelectionMode();
				selectionAdapter.setSelectedItems(selection);
				actionMode.invalidate();
			}
		}
	}

	protected abstract void onListItemClick(ViewHolder holder);
	protected abstract void onListItemLongClick(ViewHolder holder);

	private ActionMode actionMode;
	protected boolean wantSelection() {
		return true;
	}
	protected long[] getSelectedIDs() {
		List<Integer> positions = selectionAdapter.getSelectedPositions();
		long[] IDs = new long[positions.size()];
		for (int i = 0; i < positions.size(); i++) {
			IDs[i] = selectionAdapter.getItemId(positions.get(i));
		}
		return IDs;
	}
	protected void finishActionMode() {
		actionMode.finish();
	}
	@Override public final void onItemClick(ViewHolder holder) {
		if (actionMode != null) {
			selectionAdapter.toggleSelection(holder.getPosition());
			actionMode.invalidate();
		} else {
			onListItemClick(holder);
		}
	}

	@Override public final boolean onItemLongClick(ViewHolder holder) {
		if (!tryStartSelectionMode()) {
			return false;
		}
		if (actionMode != null) {
			selectionAdapter.toggleSelection(holder.getPosition());
			actionMode.invalidate();
		} else {
			onListItemLongClick(holder);
		}
		return true;
	}
	private boolean tryStartSelectionMode() {
		if (actionMode == null && wantSelection()) {
			FragmentActivity activity = getActivity();
			if (!(activity instanceof ActionBarActivity)) {
				LOG.debug("Action Mode is not available if Activity is not an ActionBar one: {}", activity);
				return false;
			}
			actionMode = ((ActionBarActivity)activity).startSupportActionMode(this);
			return true;
		}
		return true;
	}

	@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.selection, menu);
		return true;
	}

	@Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		int count = selectionAdapter.getSelectedItemCount();
		if (count == 0) {
			mode.finish();
		} else {
			mode.setTitle(getString(R.string.selection_count, count));
		}
		return false;
	}

	@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_select_all:
				selectionAdapter.selectRange(0, selectionAdapter.getItemCount());
				mode.invalidate();
				return true;
			case R.id.action_select_invert:
				Set<Integer> selection = new TreeSet<>(selectionAdapter.getSelectedPositions());
				selectionAdapter.clearSelections();
				for (int i = 0; i < selectionAdapter.getItemCount(); i++) {
					selectionAdapter.setSelected(i, !selection.contains(i));
				}
				mode.invalidate();
				return true;
			default:
				return false;
		}
	}

	@Override public void onDestroyActionMode(ActionMode mode) {
		actionMode = null;
		selectionAdapter.clearSelections();
	}
}
