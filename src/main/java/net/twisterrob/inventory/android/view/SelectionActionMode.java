package net.twisterrob.inventory.android.view;

import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.*;

import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.java.exceptions.StackTrace;

public abstract class SelectionActionMode implements ActionMode.Callback {
	private static final Logger LOG = LoggerFactory.getLogger(SelectionActionMode.class);
	private static final String KEY_SELECTION = "selection";

	private final Activity activity;
	private final SelectionAdapter<?> adapter;

	private final ActionMode.Callback callback = this;
	private ActionMode actionMode;

	public SelectionActionMode(Activity activity, SelectionAdapter<?> adapter) {
		this.activity = activity;
		this.adapter = adapter;
	}

	public Activity getActivity() {
		return activity;
	}

	@SuppressWarnings("unchecked")
	public <T extends ViewHolder> SelectionAdapter<T> getAdapter() {
		return (SelectionAdapter<T>)adapter;
	}

	public void start() {
		if (!isRunning()) {
			if (activity instanceof AppCompatActivity) {
				actionMode = ((AppCompatActivity)activity).startSupportActionMode(callback);
			} else {
				LOG.warn("Cannot start because activity doesn't support supportActionMode.", new StackTrace());
			}
		} else {
			LOG.warn("Cannot start because it is already running.", new StackTrace());
		}
	}

	public boolean isRunning() {
		return actionMode != null;
	}

	public void toggle(int position) {
		if (isRunning()) {
			adapter.toggleSelection(position);
			actionMode.invalidate();
		} else {
			LOG.warn("Cannot toggle position #{} because action mode is not running.", position, new StackTrace());
		}
	}

	public void finish() {
		if (isRunning()) {
			actionMode.finish();
		} else {
			LOG.warn("Cannot finish because action mode is not running.", new StackTrace());
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		if (isRunning()) {
			outState.putIntegerArrayList(KEY_SELECTION, new ArrayList<>(adapter.getSelectedPositions()));
		}
	}
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			ArrayList<Integer> selection = savedInstanceState.getIntegerArrayList(KEY_SELECTION);
			if (selection != null) {
				start();
				adapter.setSelectedItems(selection);
				actionMode.invalidate();
			}
		}
	}

	public long[] getSelectedIDs() {
		Collection<Integer> positions = adapter.getSelectedPositions();
		long[] IDs = new long[positions.size()];
		int i = 0;
		for (int position : positions) {
			IDs[i++] = adapter.getItemId(position);
		}
		return IDs;
	}

	@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		assert actionMode == null;
		mode.getMenuInflater().inflate(R.menu.selection, menu);
		return true;
	}

	@Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		int count = adapter.getSelectedItemCount();
		if (count == 0) {
			mode.finish();
		} else {
			mode.setTitle(activity.getResources().getQuantityString(R.plurals.selection_count, count, count));
		}
		return false;
	}

	@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		assert actionMode == mode;
		switch (item.getItemId()) {
			case R.id.action_select_all:
				adapter.selectRange(0, adapter.getItemCount());
				mode.invalidate();
				return true;
			case R.id.action_select_invert:
				Set<Integer> selection = new TreeSet<>(adapter.getSelectedPositions());
				adapter.clearSelections();
				for (int i = 0, end = adapter.getItemCount(); i < end; i++) {
					adapter.setSelected(i, !selection.contains(i));
				}
				mode.invalidate();
				return true;
			default:
				return false;
		}
	}

	@Override public void onDestroyActionMode(ActionMode mode) {
		assert actionMode == mode;
		actionMode = null;
		adapter.clearSelections();
	}

	public abstract boolean onActivityResult(int requestCode, int resultCode, Intent data);
}
