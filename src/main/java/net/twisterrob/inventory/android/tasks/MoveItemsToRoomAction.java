package net.twisterrob.inventory.android.tasks;

import java.util.List;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class MoveItemsToRoomAction extends MoveAction {
	private List<ItemDTO> items;

	public MoveItemsToRoomAction(long roomID, long... itemIDs) {
		super(roomID, R.plurals.item, R.plurals.room, itemIDs);
	}

	@Override protected void doPrepare() {
		super.doPrepare();
		items = retrieveItems(IDs);
		stuff = getNames(items);
		RoomDTO room = retrieveRoom(targetID);
		target = room.name;
		targetID = room.rootItemID;
	}

	@Override protected void doExecute() {
		App.db().moveItems(targetID, IDs);
	}

	@Override public Action buildUndo() {
		return new UndoAction(this) {
			@Override public void execute() {
				for (ItemDTO item : items) {
					App.db().moveItem(item.id, item.parentID);
				}
			}
		};
	}
}
