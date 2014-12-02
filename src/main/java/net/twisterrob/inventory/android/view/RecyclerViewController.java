package net.twisterrob.inventory.android.view;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;

import net.twisterrob.inventory.android.R;

public abstract class RecyclerViewController {
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

	public void setView(View view) {
		//LayoutInflater inflater = (LayoutInflater)view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = view; //inflater.inflate(R.layout.generic_list, view, true);

		fab = root.findViewById(R.id.fab);
		progress = (SwipeRefreshLayout)root.findViewById(android.R.id.progress);
		list = (RecyclerView)root.findViewById(android.R.id.list);

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
