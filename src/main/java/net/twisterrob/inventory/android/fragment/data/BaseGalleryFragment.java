package net.twisterrob.inventory.android.fragment.data;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.*;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.*;
import android.view.View;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.GalleryAdapter.GalleryItemEvents;

public abstract class BaseGalleryFragment<T> extends BaseRecyclerFragment<T>
		implements GalleryItemEvents {
	private HeaderManager header = null;

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

	@Override protected void onRefresh() {
		super.onRefresh();
		if (header != null) {
			header.refresh();
		}
	}

	@Override protected CursorRecyclerAdapter setupList() {
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
		list.setAdapter(adapter);
		return cursorAdapter;
	}

	protected abstract boolean canCreateNew();
	protected abstract void onCreateNew();
	protected abstract void onListItemClick(ViewHolder holder);
	protected abstract void onListItemLongClick(ViewHolder holder);

	@Override public final void onItemClick(ViewHolder holder) {
		onListItemClick(holder);
	}

	@Override public final boolean onItemLongClick(ViewHolder holder) {
		onListItemLongClick(holder);
		return true;
	}
}
