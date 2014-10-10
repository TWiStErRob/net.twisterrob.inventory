package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Room;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.Dialogs;
import net.twisterrob.inventory.android.view.Dialogs.ActionParams;

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
		App.db().deleteRoom(roomID);
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
			for (String name : items) {
				sb.append(name);
				sb.append(", ");
			}
			sb.delete(sb.length() - ", ".length(), sb.length());
			sb.append(")");
		}
		return sb.toString();
	}

	private RoomDTO retrieveRoom() {
		Cursor room = App.db().getRoom(roomID);
		try {
			room.moveToFirst();
			return RoomDTO.fromCursor(room);
		} finally {
			room.close();
		}
	}

	private List<String> retrieveItemNames() {
		Cursor items = App.db().listItems(room.rootItemID);
		try {
			List<String> itemNames = new ArrayList<String>(items.getCount());
			while (items.moveToNext()) {
				itemNames.add(items.getString(items.getColumnIndexOrThrow(Room.NAME)));
			}
			return itemNames;
		} finally {
			items.close();
		}
	}
}