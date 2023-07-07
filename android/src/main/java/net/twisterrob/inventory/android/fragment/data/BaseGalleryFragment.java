package net.twisterrob.inventory.android.fragment.data;

import java.util.Collections;

import javax.inject.Inject;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.*;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import net.twisterrob.android.AndroidConstants;
import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.content.glide.PauseOnFling;
import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.android.view.*;
import net.twisterrob.android.view.ViewProvider.StaticViewProvider;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.categories.cache.CategoryCache;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.model.CategoryVisuals;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.adapters.*;

// Every subclass must have @AndroidEntryPoint or otherwise initialize @Inject fields.
public abstract class BaseGalleryFragment<T> extends BaseFragment<T> implements GalleryEvents {
	private static final Logger LOG = LoggerFactory.getLogger(BaseGalleryFragment.class);
	/** boolean argument, defaults to true */
	public static final String KEY_ENABLE_SELECTION = "enable_selection";

	private BaseFragment<?> header;
	protected RecyclerViewLoaderController<?, ?> listController;
	protected SelectionActionMode selectionMode;
	@Inject protected CategoryVisuals visuals;
	@Inject protected CategoryCache cache;

	public void setHeader(@Nullable BaseFragment<?> headerFragment) {
		this.header = headerFragment;
	}
	public @Nullable BaseFragment<?> getHeader() {
		return header;
	}
	public boolean hasHeader() {
		return header != null;
	}
	public boolean hasHeaderUI() {
		return header != null && header.hasUI();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (selectionMode.onActivityResult(requestCode, resultCode, data)) {
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		listController.startLoad(createLoadArgs());
	}

	protected @Nullable Bundle createLoadArgs() {
		return null;
	}

	@Override protected void onRefresh() {
		super.onRefresh();
		if (hasHeader()) {
			header.refresh();
		}
		listController.refresh();
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null && hasHeader()) {
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			if (hasHeaderUI()) {
				ft.add(R.id.header, header);
			} else {
				ft.add(header, "header");
			}
			ft.commit();
			// header is scheduled to be added, will be available: whenever,
			// any post() will have header.getView() available, don't executePendingTransactions
			// because we're (parent) not activated yet!
		}
	}
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		restoreOnRotation();

		int layout = hasHeaderUI()? R.layout.generic_list_with_header : R.layout.generic_list;
		return inflater.inflate(layout, container, false);
	}

	private void restoreOnRotation() {
		if (header == null) {
			header = (BaseFragment<?>)getChildFragmentManager().findFragmentByTag("header");
		}
		if (header == null) {
			header = (BaseFragment<?>)getChildFragmentManager().findFragmentById(R.id.header);
		}
		// FIXME save fragment UI state from savedInstanceState into a field and use that in the adapter
	}

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		if (selectionMode != null) { // Fragment may be in background, and selectionMode is created in onViewCreated
			selectionMode.onSaveInstanceState(outState);
		}
	}

	@Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (selectionMode != null) { // Fragment may be in background, and selectionMode is created in onViewCreated
			selectionMode.onRestoreInstanceState(savedInstanceState);
		}
	}

	@Override public void onDestroyView() {
		if (selectionMode.isRunning()) {
			selectionMode.finish();
		}
		selectionMode = null;
		RecyclerView view = listController.getView();
		if (view != null) {
			// FIXME replace this with proper Glide.with calls
			view.setAdapter(null); // force onViewRecycled calls
		}
		super.onDestroyView();
	}
	@Override public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (hasHeader()) {
			// FIXME needed for hierarchy view? header.setMenuVisibility(menuVisible);
		}
	}

	protected abstract SelectionActionMode onPrepareSelectionMode(@NonNull SelectionAdapter<?> adapter);

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
			selectionMode.start(Collections.singleton(position));
			if (!selectionMode.isRunning()) {
				onListItemLongClick(position, recyclerViewItemID);
			}
		} else {
			selectionMode.toggle(position);
		}
		return true;
	}
	protected abstract void onListItemLongClick(int position, long recyclerViewItemID);

	@Override public void onTypeClick(int position, ImagedDTO dto) {
		if (!selectionMode.isRunning()) {
			new ChangeTypeListener(this, visuals, cache, dto).onClick(listController.getView());
		} else {
			onItemClick(position, dto.id);
		}
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
	protected @NonNull CursorRecyclerAdapter<?> setupGallery(RecyclerView list) {
		final int columns = getResources().getInteger(R.integer.gallery_columns);

		final SingleHeaderAdapter<?> adapter = createAdapter();
		@SuppressWarnings("unchecked")
		SelectionAdapter<?> selectionAdapter = new SelectionAdapter<>(adapter);
		if (getArgSelectionEnabled()) {
			selectionMode = onPrepareSelectionMode(selectionAdapter);
		} else {
			selectionMode = new SelectionActionMode.NoOp(requireActivity());
		}

		if (hasHeaderUI()) {
			View headerContainer = requireView().findViewById(R.id.header);
			adapter.setHeader(headerContainer);
			list.addOnScrollListener(new SynchronizedScrollListener(0, list, new StaticViewProvider(headerContainer)));
			selectionAdapter.setSelectable(0, false);
		}

		GridLayoutManager layout = new GridLayoutManager(requireContext(), columns);
		// TOFIX v21.0.3: doesn't work, false -> ladder jumpy, true -> chaotic jumpy
		//layout.setSmoothScrollbarEnabled(true);
		layout.setSpanSizeLookup(new SpanSizeLookup() {
			@Override public int getSpanSize(int position) {
				if (hasHeaderUI() && position == 0) {
					return columns;
				}
				return adapter.getSpanSize(position, columns);
			}
		});
		list.setLayoutManager(layout);
		list.setAdapter(selectionAdapter);
		// FIXME replace this with proper Glide.with calls
		list.addOnScrollListener(new PauseOnFling(Glide.with(requireContext().getApplicationContext())));

		return adapter;
	}

	private boolean getArgSelectionEnabled() {
		return requireArguments().getBoolean(KEY_ENABLE_SELECTION, true);
	}

	protected SingleHeaderAdapter<?> createAdapter() {
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

		@Override public void onViewRecycled(ViewHolder holder) {
			super.onViewRecycled(holder);
			if (holder instanceof GalleryViewHolder) {
				((GalleryViewHolder)holder).unBind();
			} else if (holder instanceof GalleryGroupViewHolder) {
				((GalleryGroupViewHolder)holder).unBind();
			}
		}
	}

	protected class BaseGalleryController extends RecyclerViewLoadersController {
		private final @StringRes int emptyText;

		public BaseGalleryController(Loaders loader, @StringRes int emptyText) {
			super(BaseGalleryFragment.this, loader);
			this.emptyText = emptyText;
		}

		@Override protected @NonNull CursorRecyclerAdapter<?> setupList() {
			return setupGallery(list);
		}

		@Override protected void onViewSet() {
			super.onViewSet();
			if (emptyText != AndroidConstants.INVALID_RESOURCE_ID) {
				TextView text = getEmpty();
				text.setText(emptyText);
			}
		}

		@Override protected boolean isEmpty(@NonNull CursorRecyclerAdapter<?> adapter) {
			return hasHeaderUI()? adapter.getItemCount() == 1 : super.isEmpty(adapter);
		}
	}
}
