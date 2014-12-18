package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.OnClickListener;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;

public abstract class RecyclerViewController {
	private static final Logger LOG = LoggerFactory.getLogger(RecyclerViewController.class);

	protected RecyclerView list;
	private SwipeRefreshLayout progress;

	private Runnable finishLoading = new Runnable() {
		@Override public void run() {
			progress.setRefreshing(false);
		}
	};

	private Runnable startLoading = new Runnable() {
		@Override public void run() {
			progress.setRefreshing(true);
		}
	};

	private OnClickListener createNew = new OnClickListener() {
		@Override public void onClick(View v) {
			onCreateNew();
		}
	};
	private View fab;

	public void setView(RecyclerView list) {
		this.list = list;
		ViewParent parent = list.getParent();
		if (parent instanceof SwipeRefreshLayout) {
			this.progress = (SwipeRefreshLayout)parent;
		}
		fab = AndroidTools.findClosest(list, R.id.fab);
		onViewSet();
	}

	protected void onViewSet() {
		updateFAB();
	}

	protected void updateFAB() {
		if (fab == null) {
			return;
		}
		if (canCreateNew()) {
			fab.setVisibility(View.VISIBLE);
			fab.setOnClickListener(createNew);
		} else {
			fab.setVisibility(View.INVISIBLE);
		}
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

	public void createNew() {
		onCreateNew();
	}
	public boolean canCreateNew() {
		return false;
	}
	protected void onCreateNew() {
	}
}
