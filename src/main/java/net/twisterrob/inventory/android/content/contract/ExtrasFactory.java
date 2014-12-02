package net.twisterrob.inventory.android.content.contract;

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
	public static Bundle bundleFromCategory(long categoryID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.CATEGORY_ID, categoryID);
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
	public static Intent intentFromCategory(long categoryID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromCategory(categoryID));
		return intent;
	}

	public static Bundle bundleFromIDs(long[] IDs) {
		Bundle bundle = new Bundle();
		bundle.putLongArray("IDs", IDs);
		return bundle;
	}
	public static long[] getIDsFrom(Bundle bundle) {
		return bundle.getLongArray("IDs");
	}
}
