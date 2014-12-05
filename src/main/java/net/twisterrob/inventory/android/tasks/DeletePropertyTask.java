package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Room;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.view.Action;

public abstract class DeletePropertyTask implements Action {
	private final long propertyID;

	private PropertyDTO property;
	private List<String> rooms;

	public DeletePropertyTask(long id) {
		this.propertyID = id;
	}

	@Override public void prepare() {
		property = retrieveProperty();
		rooms = retrieveRoomNames();
	}

	@Override public void execute() {
		App.db().deleteProperty(propertyID);
	}

	@Override public String getConfirmationTitle() {
		return "Deleting Property #" + propertyID;
	}

	@Override public String getConfirmationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Are you sure you want to delete the property named");
		sb.append(' ');
		sb.append("'").append(property.name).append("'");
		if (!rooms.isEmpty()) {
			sb.append(" and all ");
			sb.append(rooms.size());
			sb.append(" rooms with all items in it");
		}
		sb.append("?");
		if (!rooms.isEmpty()) {
			sb.append("\n(The rooms are: ");
			for (String name : rooms) {
				sb.append(name);
				sb.append(", ");
			}
			sb.delete(sb.length() - ", ".length(), sb.length());
			sb.append(")");
		}
		return sb.toString();
	}

	@Override public String getSuccessMessage() {
		return "Property #" + propertyID + "deleted.";
	}

	@Override public String getFailureMessage() {
		return "Cannot delete property #" + propertyID + ".";
	}

	@Override public Action buildUndo() {
		return null;
	}

	private PropertyDTO retrieveProperty() {
		Cursor property = App.db().getProperty(propertyID);
		try {
			property.moveToFirst();
			return PropertyDTO.fromCursor(property);
		} finally {
			property.close();
		}
	}

	private List<String> retrieveRoomNames() {
		Cursor rooms = App.db().listRooms(propertyID);
		try {
			List<String> roomNames = new ArrayList<>(rooms.getCount());
			while (rooms.moveToNext()) {
				roomNames.add(rooms.getString(rooms.getColumnIndexOrThrow(Room.NAME)));
			}
			return roomNames;
		} finally {
			rooms.close();
		}
	}
}