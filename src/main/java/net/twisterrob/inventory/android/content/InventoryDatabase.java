package net.twisterrob.inventory.android.content;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.inventory.android.content.InventoryContract.Item;
import net.twisterrob.inventory.android.content.InventoryContract.Search;

public class InventoryDatabase {
	private static final InventoryDatabase instance = new InventoryDatabase();
	private static final String[] NO_PROJ = null;
	private static final String NO_SEL = null;
	private static final String NO_ARGS = null;
	private static final String NO_SORT = null;

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

	public Cursor searchItems(ContentResolver contentResolver, CharSequence query) {
		String[] args = query != null? new String[]{query.toString()} : null;
		return contentResolver.query(Search.URI, NO_PROJ, NO_SEL, args, NO_SORT);
	}
}
