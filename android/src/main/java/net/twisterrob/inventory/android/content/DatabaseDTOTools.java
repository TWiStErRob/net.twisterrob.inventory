package net.twisterrob.inventory.android.content;

import java.util.*;

import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.tasks.BaseAction.ValidationException;

@WorkerThread
@SuppressWarnings({"TryFinallyCanBeTryWithResources", "resource"}) // all methods use the correct try-finally structure
public class DatabaseDTOTools {
	public static @NonNull List<String> getNames(Collection<? extends DTO> dtos) {
		List<String> names = new ArrayList<>(dtos.size());
		for (DTO dto : dtos) {
			names.add(dto.name);
		}
		return names;
	}

	public static @NonNull List<String> getNames(Cursor cursor) {
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

	public static @NonNull List<PropertyDTO> retrieveProperties(long... propertyIDs) {
		List<PropertyDTO> properties = new ArrayList<>(propertyIDs.length);
		for (long propertyID : propertyIDs) {
			properties.add(retrieveProperty(propertyID));
		}
		return properties;
	}

	public static @NonNull PropertyDTO retrieveProperty(long propertyID) {
		Cursor property = App.db().getProperty(propertyID);
		try {
			if (!property.moveToFirst()) {
				throw new ValidationException(
						R.string.generic_error_missing, new Plural(R.plurals.property, 1), propertyID);
			}
			return PropertyDTO.fromCursor(property);
		} finally {
			property.close();
		}
	}

	public static @NonNull List<String> retrieveRoomNames(long propertyID) {
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

	public static @NonNull List<RoomDTO> retrieveRooms(long... roomIDs) {
		List<RoomDTO> rooms = new ArrayList<>(roomIDs.length);
		for (long roomID : roomIDs) {
			rooms.add(retrieveRoom(roomID));
		}
		return rooms;
	}

	public static @NonNull RoomDTO retrieveRoom(long roomID) {
		Cursor room = App.db().getRoom(roomID);
		try {
			if (!room.moveToFirst()) {
				throw new ValidationException(
						R.string.generic_error_missing, new Plural(R.plurals.room, 1), roomID);
			}
			return RoomDTO.fromCursor(room);
		} finally {
			room.close();
		}
	}

	public static @NonNull List<ItemDTO> retrieveItems(long... itemIDs) {
		List<ItemDTO> items = new ArrayList<>(itemIDs.length);
		for (long itemID : itemIDs) {
			items.add(retrieveItem(itemID));
		}
		return items;
	}

	public static @NonNull ItemDTO retrieveItem(long itemID) {
		Cursor item = App.db().getItem(itemID, false);
		try {
			if (!item.moveToFirst()) {
				throw new ValidationException(
						R.string.generic_error_missing, new Plural(R.plurals.item, 1), itemID);
			}
			return ItemDTO.fromCursor(item);
		} finally {
			item.close();
		}
	}

	public static @NonNull List<String> retrieveItemNames(long itemID) {
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

	public static @NonNull ListDTO retrieveList(long listID) {
		Cursor list = App.db().getList(listID);
		try {
			if (!list.moveToFirst()) {
				throw new ValidationException(
						R.string.generic_error_missing, new Plural(R.plurals.list, 1), listID);
			}
			return ListDTO.fromCursor(list);
		} finally {
			list.close();
		}
	}

	public static long getRoot(long roomID) {
		Cursor room = App.db().getRoom(roomID);
		try {
			if (!room.moveToFirst()) {
				throw new ValidationException(
						R.string.generic_error_missing, new Plural(R.plurals.room, 1), roomID);
			}
			return room.getLong(room.getColumnIndexOrThrow(Room.ROOT_ITEM));
		} finally {
			room.close();
		}
	}

	private static class Plural implements ValidationException.Resolvable {
		private final @PluralsRes int resID;
		private final int count;
		public Plural(int resID, int count) {
			this.resID = resID;
			this.count = count;
		}
		public String resolve(@NonNull Resources res) {
			return res.getQuantityString(resID, count);
		}
	}
}
