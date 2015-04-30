package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.app.SearchManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity.Builder;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.ListViewFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.view.*;

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
				return ItemListFragment.super.setupGallery(list);
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

	@Override
	protected SelectionActionMode onPrepareSelectionMode(SelectionAdapter<?> adapter) {
		Builder builder = MoveTargetActivity.pick()
		                                    .startFromItem(getArgParentItemID())
		                                    .startFromRoom(getArgRoomID())
		                                    .allowRooms()
		                                    .allowItems();
		return new ItemSelectionActionMode(this, adapter, builder);
	}

	@Override protected Bundle createLoadArgs() {
		if (getArgQuery() != null) {
			return Intents.bundleFromQuery(getArgQuery());
		} else {
			Bundle args = new Bundle();
			if (getArgParentItemID() != Item.ID_ADD) {
				args.putLong(Extras.PARENT_ID, getArgParentItemID());
			}
			if (getArgCategoryID() != Category.ID_ADD) {
				args.putLong(Extras.CATEGORY_ID, getArgCategoryID());
			}
			if (getArgRoomID() != Room.ID_ADD) {
				args.putLong(Extras.ROOM_ID, getArgRoomID());
			}
			if (getArgListID() != CommonColumns.ID_ADD) {
				args.putLong(Extras.LIST_ID, getArgListID());
			}
			args.putBoolean(Extras.INCLUDE_SUBS, getArgIncludeSubs());
			return args;
		}
	}

	@Override protected void onStartLoading() {
		super.onStartLoading();
		if (getArgRoomID() != Room.ID_ADD) {
			Bundle args = Intents.bundleFromRoom(getArgRoomID());
			getLoaderManager().initLoader(Loaders.SingleRoom.id(), args, new LoadSingleRow(getContext()) {
				@Override protected void process(Cursor data) {
					super.process(data);
					long root = data.getLong(data.getColumnIndex(Room.ROOT_ITEM));
					getArguments().putLong(Extras.PARENT_ID, root);
				}
			});
		}
	}

	private long getArgListID() {
		return getArguments().getLong(Extras.LIST_ID, CommonColumns.ID_ADD);
	}
	private long getArgParentItemID() {
		return getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
	}
	@Deprecated private long getArgCategoryID() {
		return getArguments().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}
	private long getArgRoomID() {
		return getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}
	@Deprecated private boolean getArgIncludeSubs() {
		return getArguments().getBoolean(Extras.INCLUDE_SUBS, false);
	}
	private CharSequence getArgQuery() {
		return getArguments().getCharSequence(SearchManager.QUERY);
	}

	@Override protected void onListItemClick(int position, long recyclerViewItemID) {
		eventsListener.itemSelected(recyclerViewItemID);
	}

	@Override protected void onListItemLongClick(int position, long recyclerViewItemID) {
		eventsListener.itemActioned(recyclerViewItemID);
	}

	public static ItemListFragment newRoomInstance(long roomID) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(Intents.bundleFromRoom(roomID));
		return fragment;
	}

	public static ItemListFragment newInstance(long parentItemID) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(Intents.bundleFromParent(parentItemID));
		return fragment;
	}

	@Deprecated public static ItemListFragment newCategoryInstance(long categoryID, boolean include) {
		ItemListFragment fragment = new ItemListFragment();

		Bundle args = Intents.bundleFromCategory(categoryID);
		args.putBoolean(Extras.INCLUDE_SUBS, include);

		fragment.setArguments(args);
		return fragment;
	}

	public static ItemListFragment newSearchInstance(CharSequence query) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(Intents.bundleFromQuery(query));
		return fragment;
	}

	public static ItemListFragment newListInstance(long listID) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(Intents.bundleFromList(listID));
		return fragment;
	}

	public ItemListFragment addHeader() {
		Bundle args = getArguments();
		if (args.containsKey(Extras.PARENT_ID)) {
			setHeader(ItemViewFragment.newInstance(getArgParentItemID()));
		} else if (args.containsKey(Extras.ROOM_ID)) {
			setHeader(RoomViewFragment.newInstance(getArgRoomID()));
		} else if (args.containsKey(Extras.CATEGORY_ID)) {
			setHeader(CategoryViewFragment.newInstance(getArgCategoryID()));
		} else if (args.containsKey(Extras.LIST_ID)) {
			setHeader(ListViewFragment.newInstance(getArgListID()));
		}
		return this;
	}
}
