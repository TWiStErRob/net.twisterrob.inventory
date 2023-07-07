package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.utils.tools.ViewTools;
import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.view.*;

@AndroidEntryPoint
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

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Loaders loader = getArgQuery() != null? Loaders.ItemSearch : Loaders.Items;
		final boolean canContainItems = getArgParentItemID() != Item.ID_ADD || getArgRoomID() != Room.ID_ADD;
		int emptyText = canContainItems? R.string.item_empty_child : R.string.item_empty_list;
		listController = new BaseGalleryController(loader, emptyText) {
			@Override public boolean canCreateNew() {
				return canContainItems || getArgListID() != CommonColumns.ID_ADD;
			}

			@Override protected void onCreateNew() {
				eventsListener.newItem(getArgParentItemID());
			}
		};
	}

	@Override
	public void onPrepareOptionsMenu(@NonNull Menu menu) {
		super.onPrepareOptionsMenu(menu);
		ViewTools.visibleIf(menu, R.id.action_item_add, listController.canCreateNew());
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_add:
				listController.createNew();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected SelectionActionMode onPrepareSelectionMode(@NonNull SelectionAdapter<?> adapter) {
		MoveTargetActivity.Builder builder = MoveTargetActivity
				.pick()
				.allowRooms()
				.allowItems();
		// one of parentItem or room will be a valid ID, so try both
		if (getArgParentItemID() != Item.ID_ADD) {
			builder.startFromItem(getArgParentItemID());
		} else if (getArgRoomID() != Item.ID_ADD) {
			builder.startFromRoom(getArgRoomID());
		}
		return new ItemSelectionActionMode(this, adapter, visuals, cache, builder);
	}

	@Override protected Bundle createLoadArgs() {
		if (getArgQuery() != null) {
			return Intents.bundleFromQuery(getArgQuery());
		} else {
			Bundle args = new Bundle();
			if (getArgParentItemID() != Item.ID_ADD) {
				args.putLong(Extras.PARENT_ID, getArgParentItemID());
			}
			if (getArgRoomID() != Room.ID_ADD) {
				args.putLong(Extras.ROOM_ID, getArgRoomID());
			}
			if (getArgListID() != CommonColumns.ID_ADD) {
				args.putLong(Extras.LIST_ID, getArgListID());
			}
			return args;
		}
	}

	@Override protected void onStartLoading() {
		super.onStartLoading();
		if (getArgRoomID() != Room.ID_ADD) {
			Bundle args = Intents.bundleFromRoom(getArgRoomID());
			Loaders loader = Loaders.SingleRoom;
			getLoaderManager().initLoader(loader.id(), args, loader.createCallbacks(requireContext(), new LoadSingleRow() {
				@Override protected void process(@NonNull Cursor data) {
					super.process(data);
					@SuppressLint("Range")
					long root = data.getLong(data.getColumnIndex(Room.ROOT_ITEM));
					requireArguments().putLong(Extras.PARENT_ID, root);
				}
			}));
		}
	}

	private long getArgListID() {
		return requireArguments().getLong(Extras.LIST_ID, CommonColumns.ID_ADD);
	}
	private long getArgParentItemID() {
		return requireArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
	}
	private long getArgRoomID() {
		return requireArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}
	private CharSequence getArgQuery() {
		return requireArguments().getCharSequence(SearchManager.QUERY);
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

	public ItemListFragment addHeader(@Nullable Bundle extras) {
		Bundle args = requireArguments();
		BaseFragment<?> header = null;
		if (args.containsKey(Extras.PARENT_ID)) {
			header = ItemViewFragment.newInstance(getArgParentItemID());
		} else if (args.containsKey(Extras.ROOM_ID)) {
			header = RoomViewFragment.newInstance(getArgRoomID());
		} else if (args.containsKey(Extras.LIST_ID)) {
			header = ListViewFragment.newInstance(getArgListID());
		}
		if (header != null && extras != null) {
			// TODO lazy one, maybe be more explicit with BaseViewFragment.SHOW_DETAILS
			header.requireArguments().putAll(extras);
		}
		setHeader(header);
		return this;
	}
}
