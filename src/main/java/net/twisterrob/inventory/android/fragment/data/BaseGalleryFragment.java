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
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.GalleryAdapter.GalleryItemEvents;

public abstract class BaseGalleryFragment<T> extends BaseFragment<T> implements GalleryItemEvents {
	private static final Logger LOG = LoggerFactory.getLogger(BaseGalleryFragment.class);

	private HeaderManager header = null;
	protected RecyclerViewLoadersController listController;
	protected SelectionActionMode selectionMode;

	public void setHeader(BaseFragment headerFragment) {
		this.header = headerFragment != null? new HeaderManager(this, headerFragment) : null;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (header != null) {
			// TODO workaround for https://code.google.com/p/android/issues/detail?id=40537
			header.getHeader().onActivityResult(requestCode, resultCode, data);
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
		if (header != null) {
			header.refresh();
		}
		listController.refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (header == null) {
			setHeader(HeaderManager.tryRestore(this));
		}
		return inflater.inflate(R.layout.generic_list, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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

	@Override public final void onItemClick(ViewHolder holder) {
		if (selectionMode.isRunning()) {
			selectionMode.toggle(holder.getPosition());
		} else {
			onListItemClick(holder);
		}
	}
	protected abstract void onListItemClick(ViewHolder holder);

	@Override public final boolean onItemLongClick(ViewHolder holder) {
		if (!selectionMode.isRunning()) {
			selectionMode.start();
		}
		if (selectionMode.isRunning()) {
			selectionMode.toggle(holder.getPosition());
		} else {
			onListItemLongClick(holder);
		}
		return true;
	}
	protected abstract void onListItemLongClick(ViewHolder holder);

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
		//StaggeredGridLayoutManager layout = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
		//LinearLayoutManager layout = new LinearLayoutManager(getContext());
		GridLayoutManager layout = new GridLayoutManager(getContext(), columns);
		if (header != null) {
			layout.setSpanSizeLookup(new SpanSizeLookup() {
				@Override public int getSpanSize(int position) {
					return position == 0? columns : 1;
				}
			});
		}
		list.setLayoutManager(layout);
		list.addItemDecoration(new ItemDecoration() {
			private final int margin = getContext().getResources().getDimensionPixelSize(R.dimen.margin);
			@Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
				if (header == null || parent.getChildPosition(view) != 0) {
					outRect.set(margin, margin, margin, margin);
				}
			}
		});
		GalleryAdapter cursorAdapter = new GalleryAdapter(null, this);
		RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter = cursorAdapter;
		if (header != null) {
			adapter = header.wrap(adapter);
		}
		SelectionAdapter<? extends ViewHolder> selectionAdapter = new SelectionAdapter<>(adapter);
		if (header != null) {
			selectionAdapter.setSelectable(0, false);
		}
		selectionMode = onPrepareSelectionMode(getActivity(), selectionAdapter);
		list.setAdapter(selectionAdapter);
		return cursorAdapter;
	}

	protected SelectionActionMode onPrepareSelectionMode(Activity activity, SelectionAdapter<?> adapter) {
		return new SelectionActionMode(activity, adapter);
	}
}
