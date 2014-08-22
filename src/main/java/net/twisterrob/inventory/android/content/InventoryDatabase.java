package net.twisterrob.inventory.android.content;

import android.content.*;
import android.net.Uri;

import net.twisterrob.inventory.android.content.InventoryContract.Item;

public class InventoryDatabase {
	private static final InventoryDatabase instance = new InventoryDatabase();

	private InventoryDatabase() {}

	public static InventoryDatabase getInstance() {
		return instance;
	}

	public long createProperty(ContentResolver contentResolver, String name, String image) {
		ContentValues cv = new ContentValues();
		cv.put(Item.NAME, name);
		cv.put(Item.IMAGE, image);
		Uri result = contentResolver.insert(Item.ITEM_URI, cv);
		return Long.parseLong(result.getLastPathSegment());
	}
}
