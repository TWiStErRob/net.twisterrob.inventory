package net.twisterrob.inventory.android.tasks;

import java.util.List;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeleteRoomsAction extends DeleteAction {
	public DeleteRoomsAction(long... roomIDs) {
		super(R.plurals.room, R.plurals.item, roomIDs);
	}

	@Override protected void doPrepare() {
		super.doPrepare();
		List<RoomDTO> dtos = retrieveRooms(IDs);
		targets = getNames(dtos);
		for (RoomDTO room : dtos) {
			children.addAll(retrieveItemNames(room.rootItemID));
		}
	}

	@Override protected void doExecute() {
		for (long roomID : IDs) {
			App.db().deleteRoom(roomID);
		}
	}
}
