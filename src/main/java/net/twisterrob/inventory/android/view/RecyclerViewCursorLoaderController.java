package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.database.Cursor;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;

public abstract class RecyclerViewCursorLoaderController extends RecyclerViewController {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewCursorLoaderController.class);

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

	protected void updateAdapter(Cursor data) {
		if (adapter == null) {
			pendingData = data;
		} else {
			adapter.swapCursor(data);
			finishLoading();
		}
	}

	public abstract LoaderCallbacks<Cursor> createLoaderCallbacks();
	protected abstract CursorRecyclerAdapter setupList();
}
