package net.twisterrob.inventory.android.content.contract;

import java.util.*;

import android.content.Intent;
import android.os.Bundle;

public final class ExtrasFactory {
	private ExtrasFactory() {
		// static helper
	}

	public static Bundle bundleFromProperty(long propertyID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.PROPERTY_ID, propertyID);
		return bundle;
	}
	public static Bundle bundleFromRoom(long roomID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.ROOM_ID, roomID);
		return bundle;
	}
	public static Bundle bundleFromParent(long itemID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.PARENT_ID, itemID);
		return bundle;
	}
	public static Bundle bundleFromItem(long itemID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.ITEM_ID, itemID);
		return bundle;
	}

	public static Intent intentFromProperty(long propertyID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromProperty(propertyID));
		return intent;
	}
	public static Intent intentFromRoom(long roomID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromRoom(roomID));
		return intent;
	}
	public static Intent intentFromParent(long itemID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromParent(itemID));
		return intent;
	}
	public static Intent intentFromItem(long itemID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromItem(itemID));
		return intent;
	}
	public static Bundle bundleFromIDs(Collection<Long> IDs) {
		Bundle bundle = new Bundle();
		long[] array = new long[IDs.size()];
		int i = 0;
		for (Long roomID : IDs) {
			array[i++] = roomID;
		}
		bundle.putLongArray("IDs", array);
		return bundle;
	}
	public static Collection<Long> getIDsFrom(Bundle bundle) {
		long[] array = bundle.getLongArray("IDs");
		Collection<Long> IDs = new ArrayList<>();
		for (long id : array) {
			IDs.add(id);
		}
		return IDs;
	}
}
