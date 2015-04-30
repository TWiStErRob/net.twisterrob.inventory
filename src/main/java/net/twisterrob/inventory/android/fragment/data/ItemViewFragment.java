package net.twisterrob.inventory.android.fragment.data;

import java.util.Date;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.view.MenuItem;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.ListsActivity;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.ItemViewFragment.ItemEvents;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemViewFragment extends BaseViewFragment<ItemDTO, ItemEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemViewFragment.class);

	private static final int MOVE_REQUEST = 0;

	public interface ItemEvents {
		void itemLoaded(ItemDTO item);
		void itemDeleted(ItemDTO item);
	}

	private long parentID = Item.ID_ADD;

	public ItemViewFragment() {
		setDynamicResource(DYN_EventsClass, ItemEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.item);
		setDynamicResource(DYN_TypeLoader, Loaders.ItemCategories);
		setDynamicResource(DYN_TypeChangeTitle, "Change Category");
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleItem.id()).onContentChanged();
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleItem.id(),
				Intents.bundleFromItem(getArgItemID()), new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		ItemDTO item = ItemDTO.fromCursor(cursor);
		parentID = item.parentID;
		super.onSingleRowLoaded(item);
		eventsListener.itemLoaded(item);
	}

	@Override
	protected CharSequence getDetailsString(ItemDTO entity, boolean DEBUG) {
		return new DescriptionBuilder()
				.append("Item ID", entity.id, DEBUG)
				.append("Item Name", entity.name)
				.append("Parent ID", entity.parentID, DEBUG)
				.append("Inside", entity.parentName != null? entity.parentName : "the room")
				.append("Room ID", entity.room, DEBUG)
				.append("Room", entity.roomName)
				.append("Room Root", entity.roomRoot, DEBUG)
				.append("Property ID", entity.property, DEBUG)
				.append("Property", entity.propertyName)
				.append("Category ID", entity.type, DEBUG)
				.append("Category Name", entity.categoryName, DEBUG)
				.append("Category", AndroidTools.getText(getContext(), entity.categoryName))
				.append("# of items in this item", entity.numDirectItems)
				.append("# of items inside", entity.numAllItems)
				.append(entity.hasImage? "image" : "image removed", new Date(entity.imageTime), DEBUG)
				.append("Description", entity.description)
				.build();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_edit:
				startActivity(ItemEditActivity.edit(getArgItemID()));
				return true;
			case R.id.action_item_move:
				Intent intent = MoveTargetActivity.pick()
				                                  .startFromItem(parentID)
				                                  .allowRooms()
				                                  .allowItems()
				                                  .forbidItems(getArgItemID())
				                                  .build();
				startActivityForResult(intent, MOVE_REQUEST);
				return true;
			case R.id.action_item_delete:
				delete(getArgItemID());
				return true;
			case R.id.action_list_manage:
				startActivity(ListsActivity.manage(getArgItemID()));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MOVE_REQUEST) {
			switch (resultCode) {
				case MoveTargetActivity.ROOM: {
					long roomID = data.getLongExtra(Extras.ROOM_ID, Room.ID_ADD);
					moveToRoom(roomID, getArgItemID());
					return;
				}
				case MoveTargetActivity.ITEM: {
					long parentID = data.getLongExtra(Extras.ITEM_ID, Item.ID_ADD);
					move(parentID, getArgItemID());
					return;
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void moveToRoom(final long roomID, final long... itemIDs) {
		Dialogs.executeDirect(getActivity(), new MoveItemsToRoomAction(roomID, itemIDs) {
			public void finished() {
				startActivity(RoomViewActivity.show(roomID));
				getActivity().finish();
			}
			@Override public Action buildUndo() {
				// we navigated away from current activity, no undo
				return null;
			}
			@Override public void undoFinished() {
				// no undo
			}
		});
	}

	private void move(final long parentID, final long... itemIDs) {
		Dialogs.executeDirect(getActivity(), new MoveItemsAction(parentID, itemIDs) {
			public void finished() {
				startActivity(ItemViewActivity.show(parentID));
				getActivity().finish();
			}
			@Override public Action buildUndo() {
				// we navigated away from current activity, no undo
				return null;
			}
			@Override public void undoFinished() {
				// no undo
			}
		});
	}

	private void delete(final long itemID) {
		Dialogs.executeConfirm(getActivity(), new DeleteItemsAction(itemID) {
			@Override public void finished() {
				ItemDTO item = new ItemDTO();
				item.id = itemID;
				eventsListener.itemDeleted(item);
			}
		});
	}

	@Override protected void editImage() {
		startActivity(BaseEditActivity.takeImage(ItemEditActivity.edit(getArgItemID())));
	}

	@Override protected void update(ItemDTO entity, long newType) {
		App.db().updateItem(entity.id, newType, entity.name, entity.description);
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	public static ItemViewFragment newInstance(long itemID) {
		if (itemID == Item.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing item");
		}

		ItemViewFragment fragment = new ItemViewFragment();
		fragment.setArguments(Intents.bundleFromItem(itemID));
		return fragment;
	}
}
