package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.*;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.view.SynchronizedScrollListener;
import net.twisterrob.android.view.ViewProvider.StaticViewProvider;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.GalleryAdapter.GalleryItemEvents;

public abstract class BaseGalleryFragment<T> extends BaseFragment<T> implements GalleryItemEvents {
	private static final Logger LOG = LoggerFactory.getLogger(BaseGalleryFragment.class);

	private BaseFragment header;
	protected RecyclerViewLoadersController listController;
	protected SelectionActionMode selectionMode;

	public void setHeader(BaseFragment headerFragment) {
		this.header = headerFragment;
	}
	public BaseFragment getHeader() {
		return header;
	}
	private boolean hasHeader() {
		return header != null;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (hasHeader()) {
			// TODO this is workaround for https://code.google.com/p/android/issues/detail?id=40537
			header.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		listController.startLoad(createLoadArgs());
	}

	protected Bundle createLoadArgs() {
		return null;
	}

	@Override protected void onRefresh() {
		super.onRefresh();
		if (hasHeader()) {
			header.refresh();
		}
		listController.refresh();
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null && hasHeader()) {
			getChildFragmentManager()
					.beginTransaction()
					.add(R.id.header, header)
					.commit()
			;
			// header is scheduled to be added, will be available: whenever,
			// any post() will have header.getView() available, don't executePendingTransactions
			// because we're (parent) not activated yet!
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.generic_list_with_header, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (header == null) {
			header = (BaseFragment)getChildFragmentManager().findFragmentById(R.id.header); // restore on rotation
		}

		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		selectionMode.onSaveInstanceState(outState);
	}

	@Override public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		selectionMode.onRestoreInstanceState(savedInstanceState);
	}

	@Override public final void onItemClick(int position, long recyclerViewItemID) {
		if (selectionMode.isRunning()) {
			selectionMode.toggle(position);
		} else {
			onListItemClick(position, recyclerViewItemID);
		}
	}
	protected abstract void onListItemClick(int position, long recyclerViewItemID);

	@Override public final boolean onItemLongClick(int position, long recyclerViewItemID) {
		if (!selectionMode.isRunning()) {
			selectionMode.start();
		}
		if (selectionMode.isRunning()) {
			selectionMode.toggle(position);
		} else {
			onListItemLongClick(position, recyclerViewItemID);
		}
		return true;
	}
	protected abstract void onListItemLongClick(int position, long recyclerViewItemID);

	/**
	 * Called through:
	 * <ul>
	 * <li>{ChildOf}BaseGalleryFragment$RecyclerViewCursorLoaderController{Anon}.setupList</li>
	 * <li>RecyclerViewCursorLoaderController.setupList</li>
	 * <li>RecyclerViewCursorLoaderController.setView</li>
	 * <li>RecyclerViewController.setView</li>
	 * <li>BaseGalleryFragment.onViewCreated</li>
	 * </ul>
	 */
	protected CursorRecyclerAdapter setupList(RecyclerView list) {
		final int columns = getResources().getInteger(R.integer.gallery_columns);

		final GalleryAdapter cursorAdapter = new GalleryAdapter(null, this);
		SelectionAdapter<? extends ViewHolder> selectionAdapter = new SelectionAdapter<>(cursorAdapter);
		selectionMode = onPrepareSelectionMode(getActivity(), selectionAdapter);

		if (hasHeader() && getView() != null) {
			View headerContainer = getView().findViewById(R.id.header);
			cursorAdapter.setHeader(headerContainer);
			list.setOnScrollListener(new SynchronizedScrollListener(0, list, new StaticViewProvider(headerContainer)));
			selectionAdapter.setSelectable(0, false);
		}

		GridLayoutManager layout = new GridLayoutManager(getContext(), columns);
		// TODO v21.0.3: doesn't work, false -> ladder jumpy, true -> chaotic jumpy
		//layout.setSmoothScrollbarEnabled(true);
		layout.setSpanSizeLookup(new SpanSizeLookup() {
			@Override public int getSpanSize(int position) {
				return (hasHeader() && position == 0) || cursorAdapter.isGroup(position)? columns : 1;
			}
		});
		list.setLayoutManager(layout);
		list.addItemDecoration(new ItemDecoration() {
			private final int margin = getContext().getResources().getDimensionPixelSize(R.dimen.margin);
			@Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
				if (header == null || parent.getChildPosition(view) != 0) {
					outRect.set(margin, margin, margin, margin);
				}
			}
		});
		list.setAdapter(selectionAdapter);

		return cursorAdapter;
	}

	protected SelectionActionMode onPrepareSelectionMode(Activity activity, SelectionAdapter<?> adapter) {
		return new SelectionActionMode(activity, adapter);
	}
}
