package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.app.SearchManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.view.RecyclerViewLoadersController;

public class ItemListFragment extends BaseGalleryFragment<ItemsEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemListFragment.class);

	public interface ItemsEvents {
		void newItem(long parentID);
		void itemSelected(long itemID);
		void itemActioned(long itemID);
	}

	public ItemListFragment() {
		setDynamicResource(DYN_EventsClass, ItemsEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.item_list);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Loaders loader = getArgQuery() != null? Loaders.ItemSearch : Loaders.Items;
		listController = new RecyclerViewLoadersController(this, loader) {
			@Override protected CursorRecyclerAdapter setupList() {
				return ItemListFragment.super.setupList(list);
			}

			@Override public boolean canCreateNew() {
				return getArgParentItemID() != Item.ID_ADD || getArgRoomID() != Room.ID_ADD;
			}

			@Override protected void onCreateNew() {
				eventsListener.newItem(getArgParentItemID());
			}
		};
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_item_add).setVisible(listController.canCreateNew());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_add:
				listController.createNew();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected Bundle createLoadArgs() {
		if (listController.getLoader() == Loaders.ItemSearch) {
			return ExtrasFactory.bundleFromQuery(getArgQuery());
		} else {
			Bundle args = new Bundle();
			args.putLong(Extras.PARENT_ID, getArgParentItemID());
			args.putLong(Extras.CATEGORY_ID, getArgCategoryID());
			args.putLong(Extras.ROOM_ID, getArgRoomID());
			args.putBoolean(Extras.INCLUDE_SUBS, getArgIncludeSubs());
			return args;
		}
	}

	@Override protected void onStartLoading() {
		super.onStartLoading();
		if (getArgRoomID() != Room.ID_ADD) {
			Bundle args = ExtrasFactory.bundleFromRoom(getArgRoomID());
			getLoaderManager().initLoader(Loaders.SingleRoom.ordinal(), args, new LoadSingleRow(getContext()) {
				@Override protected void process(Cursor data) {
					super.process(data);
					long root = data.getLong(data.getColumnIndex(Room.ROOT_ITEM));
					getArguments().putLong(Extras.PARENT_ID, root);
				}
			});
		}
	}

	private long getArgParentItemID() {
		return getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
	}
	private long getArgCategoryID() {
		return getArguments().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}
	private long getArgRoomID() {
		return getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}
	private boolean getArgIncludeSubs() {
		return getArguments().getBoolean(Extras.INCLUDE_SUBS, false);
	}
	private CharSequence getArgQuery() {
		return getArguments().getCharSequence(SearchManager.QUERY);
	}

	@Override protected void onListItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.itemSelected(holder.getItemId());
	}

	@Override protected void onListItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.itemActioned(holder.getItemId());
	}

	public static ItemListFragment newRoomInstance(long roomID) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromRoom(roomID));
		return fragment;
	}

	public static ItemListFragment newInstance(long parentItemID) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromParent(parentItemID));
		return fragment;
	}

	public static ItemListFragment newCategoryInstance(long categoryID, boolean include) {
		ItemListFragment fragment = new ItemListFragment();

		Bundle args = ExtrasFactory.bundleFromCategory(categoryID);
		args.putBoolean(Extras.INCLUDE_SUBS, include);

		fragment.setArguments(args);
		return fragment;
	}

	public static ItemListFragment newSearchInstance(CharSequence query) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromQuery(query));
		return fragment;
	}
}
