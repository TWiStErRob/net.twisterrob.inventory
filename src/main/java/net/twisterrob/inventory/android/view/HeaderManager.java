package net.twisterrob.inventory.android.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public class HeaderManager {
	private final BaseFragment parent;
	private final BaseFragment header;
	private boolean headerBound = false;
	private boolean refreshPending = false;

	public HeaderManager(@NonNull BaseFragment parent, @NonNull BaseFragment fragment) {
		this.parent = parent;
		this.header = fragment;
	}

	public @NonNull RecyclerView.Adapter<? extends RecyclerView.ViewHolder> wrap(
			@NonNull RecyclerView.Adapter adapter) {
		HeaderViewRecyclerAdapter headerAdapter = new HeaderViewRecyclerAdapter(adapter) {
			@Override protected void onBindHeader(ViewHolder viewHolder, int position) {
				if (!headerBound) {
					headerBound = true;
					parent.getChildFragmentManager().beginTransaction().add(R.id.details, header).commit();
					if (refreshPending) {
						viewHolder.itemView.post(new Runnable() {
							@Override public void run() {
								refresh();
							}
						});
					}
				}
			}
		};

		FrameLayout headerContainer = new FrameLayout(parent.getActivity());
		headerContainer.setId(R.id.details);
		headerContainer.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
				(int)(parent.getActivity().getResources().getDisplayMetrics().heightPixels * 0.30)));
		headerAdapter.addHeaderView(headerContainer);
		return headerAdapter;
	}

	public BaseFragment getHeader() {
		return header;
	}

	public void refresh() {
		// when rotating the header view is not attached in onRefresh (usually =onResume),
		// so the header view is not yet bound i.e. the header fragment is not attached to the activity
		if (header.isAdded()) {
			header.refresh();
			refreshPending = false;
		} else {
			refreshPending = true;
		}
	}
}
