package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.view.Dialogs;
import net.twisterrob.inventory.android.view.Dialogs.ActionParams;

public class MoveRoomTask extends ActionParams {
	private final Collection<Long> roomIDs;
	private final long newPropertyID;

	private List<RoomDTO> rooms;
	private PropertyDTO oldProperty;
	private PropertyDTO newProperty;

	public MoveRoomTask(long newPropertyID, Collection<Long> roomIDs, Dialogs.Callback callback) {
		super(callback);
		if (roomIDs.isEmpty()) {
			throw new IllegalArgumentException("Nothing to move.");
		}
		this.roomIDs = roomIDs;
		this.newPropertyID = newPropertyID;
	}

	@Override
	protected void prepare() {
		rooms = retrieveRooms();
		Long oldPropertyID = findProperty(rooms);
		if (oldPropertyID != null) {
			oldProperty = retrieveProperty(oldPropertyID);
		}
		newProperty = retrieveProperty(newPropertyID);
	}

	private Long findProperty(List<RoomDTO> rooms) {
		Set<Long> propertyIDs = new TreeSet<>();
		for (RoomDTO room : rooms) {
			propertyIDs.add(room.propertyID);
		}
		return propertyIDs.size() == 1? propertyIDs.iterator().next() : null;
	}

	@Override
	protected void execute() {
		App.db().moveRooms(newPropertyID, roomIDs);
	}

	@Override
	protected String getTitle() {
		String base;
		if (roomIDs.size() == 1) {
			base = "Moving Room #" + roomIDs.iterator().next();
		} else {
			base = "Moving " + roomIDs.size() + " Rooms";
		}
		return base + "\nto Property #" + newPropertyID;
	}

	@Override
	protected String getMessage() {
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

	private List<RoomDTO> retrieveRooms() {
		List<RoomDTO> rooms = new ArrayList<>(roomIDs.size());
		for (long roomID : roomIDs) {
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
