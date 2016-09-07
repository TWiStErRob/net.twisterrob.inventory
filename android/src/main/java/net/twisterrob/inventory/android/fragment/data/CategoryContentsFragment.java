package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.content.*;
import android.database.*;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.*;
import android.widget.TextView;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.Loaders.LoadersCallbacks;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.adapters.*;
import net.twisterrob.inventory.android.view.adapters.CategoryViewHolder.CategoryItemEvents;

public class CategoryContentsFragment extends BaseGalleryFragment<CategoriesEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryContentsFragment.class);

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
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new CategoriesItemsController();
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_feedback:
				MainActivity.startImproveCategories(getContext(), getArgCategoryID());
				return true;
			case R.id.action_category_help:
				Intent intent = MainActivity.list(MainActivity.PAGE_CATEGORY_HELP);
				intent.putExtras(Intents.bundleFromCategory(getArgCategoryID()));
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected SingleHeaderAdapter<?> createAdapter() {
		return new CategoryAndItemsAdapter();
	}

	@Override protected SelectionActionMode onPrepareSelectionMode(SelectionAdapter<?> adapter) {
		MoveTargetActivity.Builder builder = MoveTargetActivity
				.pick()
				.startFromPropertyList()
				.allowRooms()
				.allowItems();
		return new ItemSelectionActionMode(this, adapter, builder);
	}
	@Override protected void onListItemClick(int position, long recyclerViewItemID) {
		// category listener doesn't call through to super
		eventsListener.itemSelected(recyclerViewItemID);
	}

	@Override protected void onListItemLongClick(int position, long recyclerViewItemID) {
		// category listener doesn't call through to super
		eventsListener.itemActioned(recyclerViewItemID);
	}

	private Long getArgCategoryID() {
		return (Long)getArguments().get(Extras.CATEGORY_ID);
	}
	/** @see #createLoadArgs() */
	@SuppressWarnings("unused")
	private boolean getArgFlatten() {
		return getArguments().getBoolean(Extras.INCLUDE_SUBS, false);
	}

	public static CategoryContentsFragment newInstance(Long parentCategoryID, boolean flatten) {
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
			setHeader(CategoryViewFragment.newInstance(id));
		}
		return this;
	}

	private class CategoriesItemsController extends RecyclerViewLoaderController<CursorRecyclerAdapter<?>, Cursor> {
		private final Callbacks callbackState = new Callbacks(getContext());

		public CategoriesItemsController() {
			super(CategoryContentsFragment.this);
		}

		@Override protected @NonNull CursorRecyclerAdapter<?> setupList() {
			return setupGallery(list);
		}

		@Override protected void setData(@NonNull CursorRecyclerAdapter<?> adapter, @Nullable Cursor data) {
			adapter.swapCursor(data);
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

		@Override public void startLoad(Bundle args) {
			// by listController contract all ctor provided Loaders must be started even if they don't return data
			Long id = (Long)args.get(Extras.CATEGORY_ID);
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
			getLoaderManager().initLoader(Loaders.Categories.ordinal(), categoriesArgs, callbackState);
			getLoaderManager().initLoader(Loaders.Items.ordinal(), itemsArgs, callbackState);
		}
		@Override public void refresh() {
			getLoaderManager().getLoader(Loaders.Categories.ordinal()).onContentChanged(); // counts may have changed
			getLoaderManager().getLoader(Loaders.Items.ordinal()).onContentChanged(); // items may have moved
		}

		private class Callbacks extends LoadersCallbacks {
			private Cursor pendingCategories;
			private Cursor pendingItems;

			public Callbacks(Context context) {
				super(context);
			}

			@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				CategoriesItemsController.this.startLoading();
				return super.onCreateLoader(id, args);
			}

			@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				if (Loaders.Categories.ordinal() == loader.getId()) {
					pendingCategories = data;
				} else if (Loaders.Items.ordinal() == loader.getId()) {
					pendingItems = data;
				}
				refreshAdapter();
				super.onLoadFinished(loader, data);
			}

			@Override public void onLoaderReset(Loader<Cursor> loader) {
				if (Loaders.Categories.ordinal() == loader.getId()) {
					pendingCategories = null;
				} else if (Loaders.Items.ordinal() == loader.getId()) {
					pendingItems = null;
				}
				refreshAdapter();
				super.onLoaderReset(loader);
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
			getActivity().startActivity(CategoryActivity.showFlattened(categoryID));
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
			return c.moveToPosition(position) && c.getColumnIndex("category") == DatabaseOpenHelper.CURSOR_NO_COLUMN;
		}

		@Override protected int getNonHeaderViewType(int position) {
			return isCategory(position)? R.layout.item_category : R.layout.item_gallery;
		}

		@Override protected ViewHolder onCreateNonHeaderViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			View view = inflater.inflate(viewType, parent, false);
			switch (viewType) {
				case R.layout.item_category:
					return new CategoryViewHolder(view, new CategoryItemEventsForwarder());
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
