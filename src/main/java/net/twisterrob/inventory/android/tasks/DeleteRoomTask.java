package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.activity.Dialogs.ActionParams;
import net.twisterrob.inventory.android.content.contract.Room;
import net.twisterrob.inventory.android.content.model.RoomDTO;

public class DeleteRoomTask extends ActionParams {
	private final long roomID;

	private RoomDTO room;
	private List<String> items;

	public DeleteRoomTask(long id, Dialogs.Callback callback) {
		super(callback);
		this.roomID = id;
	}

	@Override
	protected void prepare() {
		room = retrieveRoom();
		items = retrieveItemNames();
	}

	@Override
	protected void execute() {
		App.getInstance().getDataBase().deleteRoom(roomID);
	}

	@Override
	protected String getTitle() {
		return "Deleting Room #" + roomID;
	}

	@Override
	protected String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Are you sure you want to delete the room named");
		sb.append(' ');
		sb.append("'").append(room.name).append("'");
		if (!items.isEmpty()) {
			sb.append(" and all ");
			sb.append(items.size());
			sb.append(" items with all items in it");
		}
		sb.append("?");
		if (!items.isEmpty()) {
			sb.append("\n(The items are: ");
			for (String name: items) {
				sb.append(name);
				sb.append(", ");
			}
			sb.delete(sb.length() - ", ".length(), sb.length());
			sb.append(")");
		}
		return sb.toString();
	}

	private RoomDTO retrieveRoom() {
		Cursor room = App.getInstance().getDataBase().getRoom(roomID);
		try {
			room.moveToFirst();
			return RoomDTO.fromCursor(room);
		} finally {
			room.close();
		}
	}

	private List<String> retrieveItemNames() {
		Cursor rooms = App.getInstance().getDataBase().listItems(room.rootItemID);
		try {
			List<String> roomNames = new ArrayList<String>(rooms.getCount());
			while (rooms.moveToNext()) {
				roomNames.add(rooms.getString(rooms.getColumnIndexOrThrow(Room.NAME)));
			}
			return roomNames;
		} finally {
			rooms.close();
		}
	}
}