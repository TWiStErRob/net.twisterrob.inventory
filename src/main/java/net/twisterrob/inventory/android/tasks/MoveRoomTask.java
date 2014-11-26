package net.twisterrob.inventory.android.tasks;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.view.Dialogs;
import net.twisterrob.inventory.android.view.Dialogs.ActionParams;

public class MoveRoomTask extends ActionParams {
	private final long roomID;
	private final long newPropertyID;

	private RoomDTO room;
	private PropertyDTO oldProperty;
	private PropertyDTO newProperty;

	public MoveRoomTask(long roomID, long newPropertyID, Dialogs.Callback callback) {
		super(callback);
		this.roomID = roomID;
		this.newPropertyID = newPropertyID;
	}

	@Override
	protected void prepare() {
		room = retrieveRoom();
		oldProperty = retrieveProperty(room.propertyID);
		newProperty = retrieveProperty(newPropertyID);
	}

	@Override
	protected void execute() {
		App.db().moveRoom(roomID, newPropertyID);
	}

	@Override
	protected String getTitle() {
		return "Moving Room #" + roomID + "\nto Property #" + newPropertyID;
	}

	@Override
	protected String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Are you sure you want to move the room named");
		sb.append(' ');
		sb.append("'").append(room.name).append("'");
		if (room.numAllItems != 0) {
			sb.append(" with all ");
			sb.append(room.numAllItems);
			sb.append(" items in it");
		}
		sb.append(" from ").append(oldProperty.name);
		sb.append(" to ").append(newProperty.name);
		sb.append("?");
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

	private PropertyDTO retrieveProperty(long propertyID) {
		Cursor property = App.db().getProperty(propertyID);
		try {
			property.moveToFirst();
			return PropertyDTO.fromCursor(property);
		} finally {
			property.close();
		}
	}
}
