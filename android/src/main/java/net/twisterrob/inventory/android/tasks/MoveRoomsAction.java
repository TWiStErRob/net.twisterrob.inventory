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

	@Override protected void doPrepare() {
		super.doPrepare();
		target = retrieveProperty(targetID).name;
		rooms = retrieveRooms(IDs);
		stuff = getNames(rooms);
	}

	@Override protected void doExecute() {
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
