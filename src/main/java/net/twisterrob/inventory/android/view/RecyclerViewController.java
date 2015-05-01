package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.support.annotation.*;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.view.*;
import android.view.View.OnClickListener;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;

public abstract class RecyclerViewController<A extends Adapter, D> {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewController.class);

	private SwipeRefreshLayout progress;
	protected RecyclerView list;
	private View fab;
	private View empty;

	private A adapter;
	private D pendingData;

	private Runnable finishLoading = new Runnable() {
		@Override public void run() {
			progress.setRefreshing(false);
			updateEmpty();
			updateFAB();
		}
	};

	private Runnable startLoading = new Runnable() {
		@Override public void run() {
			progress.setRefreshing(true);
			updateEmpty();
			updateFAB();
		}
	};

	private OnClickListener createNew = new OnClickListener() {
		@Override public void onClick(View v) {
			onCreateNew();
		}
	};

	private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
		@Override public void onChanged() {
			updateEmpty();
		}
		@Override public void onItemRangeChanged(int positionStart, int itemCount) {
			//updateEmpty();
		}
		@Override public void onItemRangeInserted(int positionStart, int itemCount) {
			updateEmpty();
		}
		@Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			//updateEmpty();
		}
		@Override public void onItemRangeRemoved(int positionStart, int itemCount) {
			updateEmpty();
		}
	};

	public RecyclerView getView() {
		return this.list;
	}

	@SuppressWarnings("unchecked")
	public <T extends View> T getFAB() {
		return (T)this.fab;
	}

	@SuppressWarnings("unchecked")
	public <T extends View> T getEmpty() {
		return (T)this.empty;
	}

	public void setView(@NonNull RecyclerView list) {
		this.list = list;
		ViewParent parent = list.getParent();
		if (parent instanceof SwipeRefreshLayout) {
			this.progress = (SwipeRefreshLayout)parent;
		}
		fab = AndroidTools.findClosest(list, R.id.fab);
		empty = AndroidTools.findClosest(list, android.R.id.empty);
		onViewSet();
	}

	protected void onViewSet() {
		adapter = setupList();
		if (pendingData != null) {
			setData(adapter, pendingData);
			finishLoading(); // we didn't have a view to start with, but do it just to have the UI in a consistent state
			pendingData = null;
		}
		if (empty != null) {
			adapter.registerAdapterDataObserver(emptyObserver);
		}
		updateFAB();
	}

	protected abstract A setupList(); // TODO add @NonNull

	/** You have to call {@link #setView(RecyclerView)} first in order for the adapter to be created. */
	public @Nullable A getAdapter() {
		return adapter;
	}

	public void updateAdapter(D data) {
		if (adapter == null) {
			pendingData = data;
		} else {
			setData(adapter, data);
			finishLoading();
		}
	}
	protected abstract void setData(A adapter, D data);

	protected void updateFAB() {
		if (fab == null) {
			return;
		}
		if (isLoading()) {
			fab.setVisibility(View.GONE);
		} else {
			if (canCreateNew()) {
				fab.setVisibility(View.VISIBLE);
				fab.setOnClickListener(createNew);
			} else {
				fab.setVisibility(View.INVISIBLE);
			}
		}
	}

	protected void updateEmpty() {
		if (empty == null) { // no view -> nothing to update
			return;
		}
		// loading or adapter being null means emptiness is undetermined, better hide it then so progress can shine
		boolean isEmpty = !isLoading() && adapter != null && isEmpty(adapter);
		empty.setVisibility(isEmpty? View.VISIBLE : View.GONE);
	}

	protected boolean isEmpty(A adapter) {
		return adapter.getItemCount() == 0;
	}

	public SwipeRefreshLayout getProgress() {
		return progress;
	}

	protected void startLoading() {
		if (progress != null) {
			progress.post(startLoading); // progress.setRefreshing(true);
		}
	}

	protected void finishLoading() {
		if (progress != null) {
			progress.post(finishLoading); // progress.setRefreshing(false);
		}
	}

	protected boolean isLoading() {
		return progress != null && progress.isRefreshing();
	}

	public final void createNew() {
		onCreateNew();
	}
	public boolean canCreateNew() {
		return false;
	}
	protected void onCreateNew() {
		throw new UnsupportedOperationException("Please override onCreateNew() if canCreateNew() returns true!");
	}
}
