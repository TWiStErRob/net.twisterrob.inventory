package net.twisterrob.inventory.android.tasks;

import java.util.*;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeleteRoomsAction extends BaseAction {
	private final long[] roomIDs;

	private Collection<RoomDTO> rooms;
	private Collection<String> items;

	public DeleteRoomsAction(long... roomIDs) {
		if (roomIDs.length == 0) {
			throw new IllegalArgumentException("Nothing to move.");
		}
		this.roomIDs = roomIDs;
	}

	@Override public void prepare() {
		rooms = retrieveRooms(roomIDs);
		if (rooms.size() == 1) {
			items = retrieveItemNames(rooms.iterator().next().rootItemID);
		}
	}

	@Override public void execute() {
		App.db().deleteRooms(roomIDs);
	}

	@Override public String getConfirmationTitle() {
		if (roomIDs.length == 1) {
			return "Deleting Room #" + roomIDs[0];
		} else {
			return "Deleting " + roomIDs.length + " Rooms";
		}
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
		sb.append("?");
		if (items != null && !items.isEmpty()) {
			sb.append("\nThe items are: ");
			for (String name : items) {
				sb.append("\n\t");
				sb.append(name);
				sb.append(",");
			}
			sb.delete(sb.length() - ",".length(), sb.length());
		}
		return sb.toString();
	}

	@Override public String getSuccessMessage() {
		return "Room #" + Arrays.toString(roomIDs) + " deleted.";
	}

	@Override public String getFailureMessage() {
		return "Cannot move Room #" + Arrays.toString(roomIDs) + ".";
	}

	@Override public Action buildUndo() {
		return null;
	}

	@Override public void undoFinished() {
		// optional override
	}
}