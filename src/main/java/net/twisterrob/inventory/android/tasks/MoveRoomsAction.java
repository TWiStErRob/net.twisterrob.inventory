package net.twisterrob.inventory.android.tasks;

import java.util.List;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class MoveRoomsAction extends MoveAction {
	private List<RoomDTO> rooms;

	public MoveRoomsAction(long propertyID, long... roomIDs) {
		super(propertyID, R.plurals.room, R.plurals.property, roomIDs);
	}

	@Override public void prepare() {
		rooms = retrieveRooms(IDs);
		stuff = getNames(rooms);
		target = retrieveProperty(targetID).name;
	}

	@Override public void execute() {
		App.db().moveRooms(targetID, IDs);
	}

	@Override public Action buildUndo() {
		return new UndoAction(this) {
			@Override public void execute() {
				for (RoomDTO room : rooms) {
					App.db().moveRoom(room.id, room.propertyID);
				}
			}
		};
	}
}
