package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Room;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.Action;

public abstract class DeleteRoomTask implements Action {
	private final long[] roomIDs;

	private Collection<RoomDTO> rooms;
	private List<String> items;

	public DeleteRoomTask(long... roomIDs) {
		if (roomIDs.length == 0) {
			throw new IllegalArgumentException("Nothing to move.");
		}
		this.roomIDs = roomIDs;
	}

	@Override public void prepare() {
		rooms = retrieveRooms(roomIDs);
		if (rooms.size() == 1) {
			items = retrieveItemNames(rooms.iterator().next());
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

	private List<RoomDTO> retrieveRooms(long[] roomIDs) {
		List<RoomDTO> rooms = new ArrayList<>(roomIDs.length);
		for (Long roomID : roomIDs) {
			rooms.add(retrieveRoom(roomID));
		}
		return rooms;
	}

	private RoomDTO retrieveRoom(long roomID) {
		Cursor room = App.db().getRoom(roomID);
		try {
			room.moveToFirst();
			return RoomDTO.fromCursor(room);
		} finally {
			room.close();
		}
	}

	private List<String> retrieveItemNames(RoomDTO room) {
		Cursor items = App.db().listItems(room.rootItemID);
		try {
			List<String> itemNames = new ArrayList<>(items.getCount());
			while (items.moveToNext()) {
				itemNames.add(items.getString(items.getColumnIndexOrThrow(Room.NAME)));
			}
			return itemNames;
		} finally {
			items.close();
		}
	}
}