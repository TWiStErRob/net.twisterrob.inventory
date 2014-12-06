package net.twisterrob.inventory.android.content;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;

public class DatabaseDTOTools {
	public static PropertyDTO retrieveProperty(long propertyID) {
		Cursor property = App.db().getProperty(propertyID);
		try {
			property.moveToFirst();
			return PropertyDTO.fromCursor(property);
		} finally {
			property.close();
		}
	}

	public static List<String> retrieveRoomNames(long propertyID) {
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

	public static List<RoomDTO> retrieveRooms(long... roomIDs) {
		List<RoomDTO> rooms = new ArrayList<>(roomIDs.length);
		for (long roomID : roomIDs) {
			rooms.add(retrieveRoom(roomID));
		}
		return rooms;
	}

	public static RoomDTO retrieveRoom(long roomID) {
		Cursor room = App.db().getRoom(roomID);
		try {
			room.moveToFirst();
			return RoomDTO.fromCursor(room);
		} finally {
			room.close();
		}
	}

	public static ItemDTO retrieveItem(long itemID) {
		Cursor item = App.db().getItem(itemID);
		try {
			item.moveToFirst();
			return ItemDTO.fromCursor(item);
		} finally {
			item.close();
		}
	}

	public static List<String> retrieveItemNames(long itemID) {
		Cursor items = App.db().listItems(itemID);
		try {
			List<String> itemNames = new ArrayList<>(items.getCount());
			while (items.moveToNext()) {
				itemNames.add(items.getString(items.getColumnIndexOrThrow(Item.NAME)));
			}
			return itemNames;
		} finally {
			items.close();
		}
	}
}
