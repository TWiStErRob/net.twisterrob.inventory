package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Room;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.Dialogs;
import net.twisterrob.inventory.android.view.Dialogs.ActionParams;

public class DeleteRoomTask extends ActionParams {
	private final Collection<Long> roomIDs;

	private Collection<RoomDTO> rooms;
	private List<String> items;

	public DeleteRoomTask(Collection<Long> roomIDs, Dialogs.Callback callback) {
		super(callback);
		if (roomIDs.isEmpty()) {
			throw new IllegalArgumentException("Nothing to move.");
		}
		this.roomIDs = roomIDs;
	}

	@Override
	protected void prepare() {
		rooms = retrieveRooms(roomIDs);
		if (rooms.size() == 1) {
			items = retrieveItemNames(rooms.iterator().next());
		}
	}

	@Override
	protected void execute() {
		App.db().deleteRooms(roomIDs);
	}

	@Override
	protected String getTitle() {
		return "Deleting Room #" + roomIDs;
	}

	@Override
	protected String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Are you sure you want to move the");
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

	private List<RoomDTO> retrieveRooms(Collection<Long> roomIDs) {
		List<RoomDTO> rooms = new ArrayList<>();
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