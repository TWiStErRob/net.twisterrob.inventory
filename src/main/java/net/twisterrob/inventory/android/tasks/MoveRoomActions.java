package net.twisterrob.inventory.android.tasks;

import java.util.*;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class MoveRoomActions extends BaseAction {
	private final long[] roomIDs;
	private final long newPropertyID;

	private Collection<RoomDTO> rooms;
	private PropertyDTO oldProperty;
	private PropertyDTO newProperty;

	public MoveRoomActions(long newPropertyID, long... roomIDs) {
		if (roomIDs.length == 0) {
			throw new IllegalArgumentException("Nothing to move.");
		}
		this.roomIDs = roomIDs;
		this.newPropertyID = newPropertyID;
	}

	@Override public void prepare() {
		rooms = retrieveRooms();
		Long oldPropertyID = findCommonProperty(rooms);
		if (oldPropertyID != null) {
			oldProperty = retrieveProperty(oldPropertyID);
		}
		newProperty = retrieveProperty(newPropertyID);
	}

	private Long findCommonProperty(Collection<RoomDTO> rooms) {
		Set<Long> propertyIDs = new TreeSet<>();
		for (RoomDTO room : rooms) {
			propertyIDs.add(room.propertyID);
		}
		return propertyIDs.size() == 1? propertyIDs.iterator().next() : null;
	}

	@Override public void execute() {
		App.db().moveRooms(newPropertyID, roomIDs);
	}

	@Override public String getConfirmationTitle() {
		String base;
		if (roomIDs.length == 1) {
			base = "Moving Room #" + roomIDs[0];
		} else {
			base = "Moving " + roomIDs.length + " Rooms";
		}
		return base + "\nto Property #" + newPropertyID;
	}
	@Override public String getConfirmationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Are you sure you want to move the ");
		if (rooms.size() == 1) {
			sb.append("room named ").append("'").append(rooms.iterator().next().name).append("'");
		} else {
			sb.append(rooms.size()).append(" rooms");
		}
		sb.append(" with all items in ").append(rooms.size() == 1? "it" : "them");
		sb.append(" from ").append(oldProperty != null? oldProperty.name : "various properties");
		sb.append(" to ").append(newProperty.name);
		sb.append("?");
		return sb.toString();
	}

	@Override public String getSuccessMessage() {
		// TODO String message = getResources().getQuantityString(R.plurals.room_moved, roomIDs.length, roomIDs.length);
		return "Room #" + Arrays.toString(roomIDs) + " moved to property #" + newPropertyID + ".";
	}

	@Override public String getFailureMessage() {
		return "Cannot move Room #" + Arrays.toString(roomIDs) + " to property #" + newPropertyID + ".";
	}

	@Override public Action buildUndo() {
		return new UndoAction(this) {
			private final Collection<RoomDTO> rooms = MoveRoomActions.this.rooms;

			@Override public void execute() {
				for (RoomDTO room : rooms) {
					App.db().moveRoom(room.id, room.propertyID);
				}
			}
		};
	}
}
