package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.database.Cursor;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;

public abstract class RecyclerViewCursorController extends RecyclerViewController {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewCursorController.class);

	private CursorRecyclerAdapter adapter;
	private Cursor pendingData;

	@Override protected void onViewSet() {
		super.onViewSet();
		adapter = setupList();
		if (pendingData != null) {
			adapter.swapCursor(pendingData);
			// finishLoading(); // no need to finish, because we didn't have a view to start with
			pendingData = null;
		}
	}

	public void updateAdapter(Cursor data) {
		if (adapter == null) {
			pendingData = data;
		} else {
			adapter.swapCursor(data);
			finishLoading();
		}
	}

	protected abstract CursorRecyclerAdapter setupList();
}
