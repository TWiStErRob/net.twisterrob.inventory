package net.twisterrob.inventory.android.content;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;

public class DatabaseDTOTools {
	public static List<String> getNames(Collection<? extends DTO> dtos) {
		List<String> names = new ArrayList<>(dtos.size());
		for (DTO dto : dtos) {
			names.add(dto.name);
		}
		return names;
	}

	public static List<String> getNames(Cursor cursor) {
		try {
			List<String> names = new ArrayList<>(cursor.getCount());
			while (cursor.moveToNext()) {
				names.add(cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME)));
			}
			return names;
		} finally {
			cursor.close();
		}
	}

	public static List<PropertyDTO> retrieveProperties(long... propertyIDs) {
		List<PropertyDTO> properties = new ArrayList<>(propertyIDs.length);
		for (long propertyID : propertyIDs) {
			properties.add(retrieveProperty(propertyID));
		}
		return properties;
	}

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

	public static List<ItemDTO> retrieveItems(long... itemIDs) {
		List<ItemDTO> items = new ArrayList<>(itemIDs.length);
		for (long itemID : itemIDs) {
			items.add(retrieveItem(itemID));
		}
		return items;
	}

	public static ItemDTO retrieveItem(long itemID) {
		Cursor item = App.db().getItem(itemID, false);
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

	public static ListDTO retrieveList(long listID) {
		Cursor list = App.db().getList(listID);
		try {
			list.moveToFirst();
			return ListDTO.fromCursor(list);
		} finally {
			list.close();
		}
	}
}
