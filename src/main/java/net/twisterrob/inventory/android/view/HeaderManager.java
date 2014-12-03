package net.twisterrob.inventory.android.view;

import java.util.Locale;

import org.slf4j.*;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public class HeaderManager {
	private static final Logger LOG = LoggerFactory.getLogger(HeaderManager.class);

	private final BaseFragment parent;
	private final BaseFragment header;
	private boolean refreshPending = false;

	public HeaderManager(@NonNull BaseFragment parent, @NonNull BaseFragment fragment) {
		this.parent = parent;
		this.header = fragment;
	}

	public BaseFragment getHeader() {
		return header;
	}

	public @NonNull RecyclerView.Adapter<? extends RecyclerView.ViewHolder> wrap(
			@NonNull RecyclerView.Adapter adapter) {
		HeaderViewRecyclerAdapter headerAdapter = new HeaderViewRecyclerAdapter(adapter) {
			@Override protected void onBindHeader(ViewHolder viewHolder, int position) {
				if (header.getId() != R.id.details && !header.isAdded()) {
					parent.getChildFragmentManager().beginTransaction().replace(R.id.details, header).commit();
				}
				if (refreshPending) {
					viewHolder.itemView.post(new Runnable() {
						@Override public void run() {
							refresh();
						}
					});
				}
			}
		};

		headerAdapter.addHeaderView(createContainer());
		return headerAdapter;
	}

	protected ViewGroup createContainer() {
		FrameLayout headerContainer = new FrameLayout(parent.getActivity());
		headerContainer.setId(R.id.details);
		headerContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				(int)(parent.getActivity().getResources().getDisplayMetrics().heightPixels * 0.30)));
		return headerContainer;
	}

	public static BaseFragment tryRestore(BaseFragment parent) {
		Fragment oldHeader = parent.getChildFragmentManager().findFragmentById(R.id.details);
		if (oldHeader instanceof BaseFragment) {
			// should return oldHeader, but then there's no UI shown after bind, let's clone it
			return (BaseFragment)Fragment.instantiate(parent.getActivity(),
					oldHeader.getClass().getName(), oldHeader.getArguments());
		}
		return null;
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

	@Override public String toString() {
		return String.format(Locale.ROOT, "%2$s in %1$s, refresh: %3$b, add/vis/res/det: %4$b/%5$b/%6$b/%7$b",
				parent, header, refreshPending, header.isAdded(), header.isVisible(), header.isResumed(),
				header.isDetached());
	}
}
