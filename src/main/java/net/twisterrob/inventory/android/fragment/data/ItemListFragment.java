package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.app.*;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.*;

public class ItemListFragment extends BaseGalleryFragment<ItemsEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemListFragment.class);
	private static final int PICK_REQUEST = 1;

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

	@Override
	protected SelectionActionMode onPrepareSelectionMode(Activity activity, SelectionAdapter<?> adapter) {
		return new SelectionActionMode(activity, adapter) {
			@Override public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.item_bulk, menu);
				return super.onCreateActionMode(mode, menu);
			}

			@Override public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
					case R.id.action_item_delete:
						delete(selectionMode.getSelectedIDs());
						return true;
					case R.id.action_item_move:
						Intent intent = MoveTargetActivity.pick()
						                                  .startFromItem(getArgParentItemID())
						                                  .startFromRoom(getArgRoomID())
						                                  .allowRooms()
						                                  .allowItems()
						                                  .forbidItems(getSelectedIDs())
						                                  .build();
						startActivityForResult(intent, PICK_REQUEST);
						return true;
				}
				return super.onActionItemClicked(mode, item);
			}
		};
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_REQUEST) {
			switch (resultCode) {
				case MoveTargetActivity.ROOM: {
					long roomID = data.getLongExtra(Extras.ROOM_ID, Room.ID_ADD);
					moveToRoom(roomID, selectionMode.getSelectedIDs());
					return;
				}
				case MoveTargetActivity.ITEM: {
					long parentID = data.getLongExtra(Extras.ITEM_ID, Item.ID_ADD);
					move(parentID, selectionMode.getSelectedIDs());
					return;
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void delete(final long... itemIDs) {
		Dialogs.executeConfirm(getActivity(), new DeleteItemsAction(itemIDs) {
			public void finished() {
				selectionMode.finish();
				refresh();
			}
		});
	}

	private void moveToRoom(final long roomID, final long... itemIDs) {
		Dialogs.executeDirect(getActivity(), new MoveItemsToRoomAction(roomID, itemIDs) {
			public void finished() {
				selectionMode.finish();
				refresh();
			}
			@Override public void undoFinished() {
				refresh();
			}
		});
	}

	private void move(final long parentID, final long... itemIDs) {
		Dialogs.executeDirect(getActivity(), new MoveItemsAction(parentID, itemIDs) {
			public void finished() {
				selectionMode.finish();
				refresh();
			}
			@Override public void undoFinished() {
				refresh();
			}
		});
	}

	@Override protected Bundle createLoadArgs() {
		if (listController.getLoader() == Loaders.ItemSearch) {
			return ExtrasFactory.bundleFromQuery(getArgQuery());
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
			Bundle args = ExtrasFactory.bundleFromRoom(getArgRoomID());
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
		fragment.setArguments(ExtrasFactory.bundleFromRoom(roomID));
		return fragment;
	}

	public static ItemListFragment newInstance(long parentItemID) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromParent(parentItemID));
		return fragment;
	}

	@Deprecated public static ItemListFragment newCategoryInstance(long categoryID, boolean include) {
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

	public static ItemListFragment newListInstance(long listID) {
		ItemListFragment fragment = new ItemListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromList(listID));
		return fragment;
	}
}
