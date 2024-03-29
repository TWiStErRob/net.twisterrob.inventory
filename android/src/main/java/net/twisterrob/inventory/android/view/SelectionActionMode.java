package net.twisterrob.inventory.android.view;

import java.util.*;

import org.slf4j.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import net.twisterrob.android.view.*;
import net.twisterrob.inventory.android.PreconditionsKt;
import net.twisterrob.inventory.android.R;
import net.twisterrob.java.exceptions.StackTrace;

public abstract class SelectionActionMode implements ActionMode.Callback {
	private static final Logger LOG = LoggerFactory.getLogger(SelectionActionMode.class);
	private static final String KEY_SELECTION = "selection";

	private final @NonNull AppCompatActivity activity;
	private final @NonNull SelectionAdapter<?> adapter;

	private final ActionMode.Callback callback = this;
	private ActionMode actionMode;

	public SelectionActionMode(@NonNull FragmentActivity activity, @NonNull SelectionAdapter<?> adapter) {
		this.activity = (AppCompatActivity)PreconditionsKt.checkNotNull(activity);
		this.adapter = PreconditionsKt.checkNotNull(adapter);
	}

	public @NonNull AppCompatActivity getActivity() {
		return activity;
	}

	@SuppressWarnings("unchecked")
	public @NonNull <T extends ViewHolder> SelectionAdapter<T> getAdapter() {
		return (SelectionAdapter<T>)adapter;
	}

	public void start(@NonNull Collection<Integer> initialSelection) {
		adapter.setSelectedItems(initialSelection);
		start();
	}
	public void start() {
		if (!isRunning()) {
			actionMode = activity.startSupportActionMode(callback);
		} else {
			actionMode.invalidate();
			LOG.warn("Cannot start because it is already running.", new StackTrace());
		}
	}

	public boolean isRunning() {
		return actionMode != null;
	}

	public void toggle(int position) {
		if (isRunning()) {
			adapter.toggleSelection(position);
			invalidateOrFinish();
		} else {
			LOG.warn("Cannot toggle position #{} because action mode is not running.", position, new StackTrace());
		}
	}

	private void invalidateOrFinish() {
		if (adapter.getSelectedItemCount() == 0) {
			finish();
		} else {
			actionMode.invalidate();
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
			outState.putLongArray(KEY_SELECTION, getSelectedIDs());
		}
	}
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			long[] savedSelection = savedInstanceState.getLongArray(KEY_SELECTION);
			if (savedSelection != null) {
				adapter.setSelectedIds(savedSelection);
				start();
			}
		}
	}

	public long[] getSelectedIDs() {
		return adapter.getSelectedIds();
	}

	@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		assert actionMode == null;
		mode.getMenuInflater().inflate(R.menu.selection, menu);
		return true;
	}

	@Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		int count = adapter.getSelectedItemCount();
		mode.setTitle(activity.getResources().getQuantityString(R.plurals.selection_count, count, count));
		return true;
	}

	@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		assert actionMode == mode;
		int id = item.getItemId();
		if (id == R.id.action_select_all) {
			adapter.selectRange(0, adapter.getItemCount());
			invalidateOrFinish();
			return true;
		} else if (id == R.id.action_select_invert) {
			long[] selection = adapter.getSelectedIds();
			Arrays.sort(selection);
			adapter.clearSelections();
			for (int i = 0, end = adapter.getItemCount(); i < end; i++) {
				boolean wasSelected = Arrays.binarySearch(selection, adapter.getItemId(i)) >= 0;
				adapter.setSelected(i, !wasSelected);
			}
			invalidateOrFinish();
			return true;
		} else {
			return false;
		}
	}

	@Override public void onDestroyActionMode(ActionMode mode) {
		assert actionMode == mode;
		actionMode = null;
		adapter.clearSelections();
	}

	public abstract boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

	public static class NoOp extends SelectionActionMode {
		public NoOp(FragmentActivity activity) {
			super(activity, new SelectionAdapter<>(new EmptyAdapter<>()));
		}
		@Override public void start() {
			// no op
		}
		@Override public boolean isRunning() {
			return false;
		}
		@Override public void finish() {
			// no op
		}
		@Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
			return false;
		}
	}
}
