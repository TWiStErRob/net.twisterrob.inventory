package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v7.widget.*;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.*;
import android.widget.TextView;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.view.*;
import net.twisterrob.android.view.ViewProvider.StaticViewProvider;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.adapters.*;

public abstract class BaseGalleryFragment<T> extends BaseFragment<T> implements GalleryEvents {
	private static final Logger LOG = LoggerFactory.getLogger(BaseGalleryFragment.class);

	private BaseFragment header;
	protected RecyclerViewLoaderController listController;
	protected SelectionActionMode selectionMode;

	public void setHeader(BaseFragment headerFragment) {
		this.header = headerFragment;
	}
	public BaseFragment getHeader() {
		return header;
	}
	public boolean hasHeader() {
		return header != null;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (selectionMode.onActivityResult(requestCode, resultCode, data)) {
			return;
		}
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
		if (header == null) {
			header = (BaseFragment)getChildFragmentManager().findFragmentById(R.id.header); // restore on rotation
			// FIXME save fragment UI state from savedInstanceState into a field and use that in the adapter
		}
		int layout = hasHeader()? R.layout.generic_list_with_header : R.layout.generic_list;
		return inflater.inflate(layout, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (selectionMode != null) { // Fragment may be in background, and selectionMode is created in onViewCreated
			selectionMode.onSaveInstanceState(outState);
		}
	}

	@Override public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (selectionMode != null) { // Fragment may be in background, and selectionMode is created in onViewCreated
			selectionMode.onRestoreInstanceState(savedInstanceState);
		}
	}

	@Override public void onDestroyView() {
		super.onDestroyView();
		if (selectionMode.isRunning()) {
			selectionMode.finish();
		}
		selectionMode = null;
	}
	@Override public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (header != null) {
			// FIXME needed for hierarchy view? header.setMenuVisibility(menuVisible);
		}
	}

	protected abstract SelectionActionMode onPrepareSelectionMode(SelectionAdapter<?> adapter);

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

	@Override public void onTypeClick(int position, ImagedDTO dto) {
		new ChangeTypeListener(this, dto).onClick(listController.getView());
	}

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
	protected @NonNull CursorRecyclerAdapter setupGallery(RecyclerView list) {
		final int columns = getResources().getInteger(R.integer.gallery_columns);

		final SingleHeaderAdapter adapter = createAdapter();
		@SuppressWarnings("unchecked")
		SelectionAdapter selectionAdapter = new SelectionAdapter(adapter);
		selectionMode = onPrepareSelectionMode(selectionAdapter);

		if (hasHeader() && header.hasUI() && /*ConstantConditions:*/ getView() != null) {
			View headerContainer = getView().findViewById(R.id.header);
			adapter.setHeader(headerContainer);
			list.addOnScrollListener(new SynchronizedScrollListener(0, list, new StaticViewProvider(headerContainer)));
			selectionAdapter.setSelectable(0, false);
		}

		GridLayoutManager layout = new GridLayoutManager(getContext(), columns);
		// TODO v21.0.3: doesn't work, false -> ladder jumpy, true -> chaotic jumpy
		//layout.setSmoothScrollbarEnabled(true);
		layout.setSpanSizeLookup(new SpanSizeLookup() {
			@Override public int getSpanSize(int position) {
				if (hasHeader() && header.hasUI() && position == 0) {
					return columns;
				}
				return adapter.getSpanSize(position, columns);
			}
		});
		list.setLayoutManager(layout);
		list.setAdapter(selectionAdapter);

		return adapter;
	}

	protected SingleHeaderAdapter createAdapter() {
		return new GalleryAdapter(null, this);
	}

	private static class GalleryAdapter extends SingleHeaderAdapter<ViewHolder> {
		private final GalleryEvents listener;

		public GalleryAdapter(Cursor cursor, GalleryEvents listener) {
			super(cursor);
			this.listener = listener;
		}

		@Override public int getSpanSize(int position, int columns) {
			return isGroup(position)? columns : 1;
		}

		@Override protected int getNonHeaderViewType(int position) {
			return isGroup(position)? R.layout.item_gallery_group : R.layout.item_gallery;
		}

		private boolean isGroup(int position) {
			Cursor c = getCursor();
			return c.moveToPosition(position) && DatabaseTools.getOptionalBoolean(c, "group", false);
		}

		@Override protected ViewHolder onCreateNonHeaderViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			View view = inflater.inflate(viewType, parent, false);
			switch (viewType) {
				case R.layout.item_gallery:
					return new GalleryViewHolder(view, listener);
				case R.layout.item_gallery_group:
					return new GalleryGroupViewHolder(view, listener);
				default:
					throw new IllegalArgumentException("Unsupported viewType: " + viewType);
			}
		}

		@Override protected void onBindNonHeaderViewHolder(ViewHolder holder, Cursor cursor) {
			if (holder instanceof GalleryViewHolder) {
				((GalleryViewHolder)holder).bind(cursor);
			} else if (holder instanceof GalleryGroupViewHolder) {
				((GalleryGroupViewHolder)holder).bind(cursor);
			}
		}
	}

	protected class BaseGalleryController extends RecyclerViewLoadersController {
		private final @StringRes int emptyText;

		public BaseGalleryController(Loaders loader, @StringRes int emptyText) {
			super(BaseGalleryFragment.this, loader);
			this.emptyText = emptyText;
		}

		@Override protected @NonNull CursorRecyclerAdapter setupList() {
			return setupGallery(list);
		}

		@Override protected void onViewSet() {
			super.onViewSet();
			if (emptyText != AndroidTools.INVALID_RESOURCE_ID) {
				TextView text = getEmpty();
				text.setText(emptyText);
			}
		}

		@Override protected boolean isEmpty(CursorRecyclerAdapter adapter) {
			return hasHeader()? adapter.getItemCount() == 1 : super.isEmpty(adapter);
		}
	}
}
