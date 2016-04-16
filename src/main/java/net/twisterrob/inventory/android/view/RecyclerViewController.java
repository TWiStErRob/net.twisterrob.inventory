package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.database.Cursor;
import android.support.annotation.*;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.*;
import android.view.View.OnClickListener;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;

public abstract class RecyclerViewController<A extends RecyclerView.Adapter<?>, D> {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewController.class);

	private SwipeRefreshLayout progress;
	private boolean lastScrollUp = false;
	protected RecyclerView list;
	private View fab;
	private View empty;

	private A adapter;
	private D pendingData;

	private final Runnable finishLoading = new Runnable() {
		@Override public void run() {
			progress.setRefreshing(false);
			updateEmpty();
			updateFAB();
		}
	};

	private final Runnable startLoading = new Runnable() {
		@Override public void run() {
			progress.setRefreshing(true);
			updateEmpty();
			updateFAB();
		}
	};

	private final OnClickListener createNew = new OnClickListener() {
		@Override public void onClick(View v) {
			onCreateNew();
		}
	};

	private final OnScrollListener hideOnUp = new OnScrollListener() {
		@Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			// no op
		}
		@Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			lastScrollUp = dx > 0 || dy > 0;
			updateFAB();
		}
	};
	private final RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
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
		if (fab != null) {
			fab.setVisibility(View.INVISIBLE); // force a layout, but don't show
			fab.setOnClickListener(createNew);
		}
		list.addOnScrollListener(hideOnUp);
	}

	protected abstract @NonNull A setupList();

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

	/**
	 * If {@link D data's type} is a {@link Cursor}, and it is coming from a {@link Loader}
	 * use {@link CursorRecyclerAdapter#swapCursor(Cursor)} (keeps it open)
	 * instead of {@link CursorRecyclerAdapter#changeCursor(Cursor)} (closes it).
	 * The {@link LoaderManager} will take care of disposing the data.
	 */
	protected abstract void setData(A adapter, D data);

	protected void updateFAB() {
		if (fab == null) {
			return;
		}
		if (isLoading() || !canCreateNew() || lastScrollUp) {
			if (fab instanceof FloatingActionButton) {
				((FloatingActionButton)fab).hide();
			} else {
				fab.setVisibility(View.INVISIBLE);
			}
		} else {
			if (fab instanceof FloatingActionButton) {
				((FloatingActionButton)fab).show();
			} else {
				fab.setVisibility(View.VISIBLE);
			}
		}
	}

	protected void updateEmpty() {
		if (empty == null) { // no view -> nothing to update
			return;
		}
		// loading or adapter being null means emptiness is undetermined, better hide it then so progress can shine
		AndroidTools.displayedIf(empty, !isLoading() && adapter != null && isEmpty(adapter));
	}

	protected boolean isEmpty(@NonNull A adapter) {
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
