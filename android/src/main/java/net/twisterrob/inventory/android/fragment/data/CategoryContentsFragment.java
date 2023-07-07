package net.twisterrob.inventory.android.fragment.data;

import android.content.Intent;
import android.database.*;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.contract.InventoryLoader.LoadersCallbacksAdapter;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.adapters.*;
import net.twisterrob.inventory.android.view.adapters.CategoryViewHolder.CategoryItemEvents;

@AndroidEntryPoint
public class CategoryContentsFragment extends BaseGalleryFragment<CategoriesEvents> {
	public interface CategoriesEvents extends ItemsEvents {
		void categorySelected(long categoryID);
		void categoryActioned(long categoryID);
	}

	public CategoryContentsFragment() {
		setDynamicResource(DYN_EventsClass, CategoriesEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.category_list);
	}

	@Override protected Bundle createLoadArgs() {
		return getArguments();
	}
	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new CategoriesItemsController();
	}

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_feedback:
				MainActivity.startImproveCategories(requireContext(), cache, getArgCategoryID());
				return true;
			case R.id.action_category_help:
				Intent intent = MainActivity.list(requireContext(), MainActivity.PAGE_CATEGORY_HELP);
				Long category = getArgCategoryID();
				if (category != null) {
					intent.putExtras(Intents.bundleFromCategory(category));
				}
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected SingleHeaderAdapter<?> createAdapter() {
		return new CategoryAndItemsAdapter();
	}

	@Override protected SelectionActionMode onPrepareSelectionMode(@NonNull SelectionAdapter<?> adapter) {
		MoveTargetActivity.Builder builder = MoveTargetActivity
				.pick()
				.startFromPropertyList()
				.allowRooms()
				.allowItems();
		return new ItemSelectionActionMode(this, adapter, visuals, cache, builder);
	}
	@Override protected void onListItemClick(int position, long recyclerViewItemID) {
		// category listener doesn't call through to super
		eventsListener.itemSelected(recyclerViewItemID);
	}

	@Override protected void onListItemLongClick(int position, long recyclerViewItemID) {
		// category listener doesn't call through to super
		eventsListener.itemActioned(recyclerViewItemID);
	}

	private @Nullable Long getArgCategoryID() {
		return Intents.getOptionalCategory(requireArguments());
	}
	/** @see #createLoadArgs() */
	private boolean getArgFlatten() {
		return requireArguments().getBoolean(Extras.INCLUDE_SUBS, false);
	}

	public static CategoryContentsFragment newInstance(@Nullable Long parentCategoryID, boolean flatten) {
		CategoryContentsFragment fragment = new CategoryContentsFragment();
		Bundle args = new Bundle();
		args.putSerializable(Extras.CATEGORY_ID, parentCategoryID);
		args.putBoolean(Extras.INCLUDE_SUBS, flatten);
		fragment.setArguments(args);
		return fragment;
	}

	public CategoryContentsFragment addHeader() {
		Long id = getArgCategoryID();
		if (id != null) {
			setHeader(CategoryViewFragment.newInstance(id, getArgFlatten()));
		}
		return this;
	}

	private class CategoriesItemsController extends RecyclerViewLoaderController<CursorRecyclerAdapter<?>, Cursor> {
		private final LoaderCallbacks<Cursor> callbackState =
				Loaders.Items.createCallbacks(getContext(), new Callbacks());

		public CategoriesItemsController() {
			super(CategoryContentsFragment.this);
		}

		@Override protected @NonNull CursorRecyclerAdapter<?> setupList() {
			return setupGallery(list);
		}

		@Override protected void setData(@NonNull CursorRecyclerAdapter<?> adapter, @Nullable Cursor data) {
			Cursor oldCursor = adapter.swapCursor(data);
			if (data == null && oldCursor != null) {
				// See net.twisterrob.inventory.android.view.RecyclerViewLoadersController.setData.
				oldCursor.close();
			}
			if (data != null && selectionMode != null) {
				SelectionAdapter<?> selectionAdapter = selectionMode.getAdapter();
				Cursor cursor = ((CursorRecyclerAdapter<?>)selectionAdapter.getWrappedAdapter()).getCursor();
				selectionAdapter.resetSelectable();
				for (int i = 0; i < selectionAdapter.getItemCount(); ++i) {
					cursor.moveToPosition(i);
					if ("item".equals(DatabaseTools.getOptionalString(cursor, "type"))) {
						break;
					}
					selectionAdapter.setSelectable(i, false);
				}
			}
		}

		@Override protected void onViewSet() {
			super.onViewSet();
			TextView text = getEmpty();
			text.setText(R.string.item_empty_category);
		}

		@Override protected boolean isEmpty(@NonNull CursorRecyclerAdapter<?> adapter) {
			return adapter.getItemCount() <= 1;
		}

		@Override public void startLoad(@NonNull Bundle args) {
			// by listController contract all ctor provided Loaders must be started even if they don't return data
			Long id = Intents.getOptionalCategory(args);
			boolean flatten = args.getBoolean(Extras.INCLUDE_SUBS);

			Bundle categoriesArgs;
			Bundle itemsArgs;
			if (id == null) { // all "root" categories
				if (flatten) { // all items
					categoriesArgs = Intents.bundleFromCategory(Category.ID_ADD); // none
					itemsArgs = null; // all items
				} else { // all categories
					categoriesArgs = Intents.bundleFrom(Extras.CATEGORY_ID, null); // all
					itemsArgs = Intents.bundleFromParent(Item.ID_ADD); // no items
				}
			} else { // specific category
				if (flatten) { // all items in category
					categoriesArgs = Intents.bundleFromCategory(Category.ID_ADD); // none
					itemsArgs = Intents.bundleFromCategory(id);  // items in category
					itemsArgs.putBoolean(Extras.INCLUDE_SUBS, true);
				} else { // subcategories and direct items
					categoriesArgs = Intents.bundleFromCategory(id); // subcategories
					itemsArgs = Intents.bundleFromCategory(id); // items by category
				}
			}
			getLoaderManager().initLoader(Loaders.Categories.id(), categoriesArgs, callbackState);
			getLoaderManager().initLoader(Loaders.Items.id(), itemsArgs, callbackState);
		}
		@Override public void refresh() {
			getLoaderManager().getLoader(Loaders.Categories.id()).onContentChanged(); // counts may have changed
			getLoaderManager().getLoader(Loaders.Items.id()).onContentChanged(); // items may have moved
		}

		private class Callbacks extends LoadersCallbacksAdapter {
			private Cursor pendingCategories;
			private Cursor pendingItems;

			@Override public void preOnCreateLoader(int id, Bundle args) {
				CategoriesItemsController.this.startLoading();
			}

			@Override public void preOnLoadFinished(Loader<Cursor> loader, Cursor data) {
				if (Loaders.Categories.id() == loader.getId()) {
					pendingCategories = data;
				} else if (Loaders.Items.id() == loader.getId()) {
					pendingItems = data;
				}
				refreshAdapter();
			}

			@Override public void preOnLoaderReset(Loader<Cursor> loader) {
				if (Loaders.Categories.id() == loader.getId()) {
					pendingCategories = null;
				} else if (Loaders.Items.id() == loader.getId()) {
					pendingItems = null;
				}
				refreshAdapter();
			}

			private void refreshAdapter() {
				Cursor c;
				if (pendingCategories != null && pendingItems != null) {
					c = new MergeCursor(new Cursor[] {pendingCategories, pendingItems});
				} else if (pendingCategories != null) {
					c = pendingCategories;
				} else if (pendingItems != null) {
					c = pendingItems;
				} else {
					c = null;
				}
				updateAdapter(c);
				if (pendingCategories == null || pendingItems == null) {
					startLoading(); // restart loading indicator since we only have one part of the data
				}
			}
		}
	}

	private class CategoryItemEventsForwarder implements CategoryItemEvents {
		@Override public void showItemsInCategory(long categoryID) {
			requireActivity().startActivity(CategoryActivity.showFlattened(categoryID));
		}

		@Override public void onItemClick(int position, long recyclerViewItemID) {
			eventsListener.categorySelected(recyclerViewItemID);
		}

		@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
			eventsListener.categoryActioned(recyclerViewItemID);
			return true;
		}
	}

	private class CategoryAndItemsAdapter extends SingleHeaderAdapter<ViewHolder> {
		public CategoryAndItemsAdapter() {
			super(null);
		}

		@Override public int getSpanSize(int position, int columns) {
			return isCategory(position)? columns : 1;
		}

		public boolean isCategory(int position) {
			Cursor c = getCursor();
			// Remember c is a MergeCursor so moving to a position may change the columns:
			// only Items have "category" column, Categories don't
			return c.moveToPosition(position) && c.getColumnIndex("category") == DatabaseTools.INVALID_COLUMN;
		}

		@Override protected int getNonHeaderViewType(int position) {
			return isCategory(position)? R.layout.item_category : R.layout.item_gallery;
		}

		@Override protected ViewHolder onCreateNonHeaderViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			View view = inflater.inflate(viewType, parent, false);
			switch (viewType) {
				case R.layout.item_category:
					return new CategoryViewHolder(view, visuals, new CategoryItemEventsForwarder());
				case R.layout.item_gallery:
					return new GalleryViewHolder(view, CategoryContentsFragment.this);
				default:
					throw new IllegalArgumentException("Invalid view type: " + viewType);
			}
		}
		@Override protected void onBindNonHeaderViewHolder(ViewHolder holder, Cursor cursor) {
			if (holder instanceof CategoryViewHolder) {
				((CategoryViewHolder)holder).bind(cursor);
			} else if (holder instanceof GalleryViewHolder) {
				((GalleryViewHolder)holder).bind(cursor);
			}
		}
		@Override public void onViewRecycled(ViewHolder holder) {
			super.onViewRecycled(holder);
			if (holder instanceof CategoryViewHolder) {
				((CategoryViewHolder)holder).unBind();
			} else if (holder instanceof GalleryViewHolder) {
				((GalleryViewHolder)holder).unBind();
			}
		}
	}
}
