package net.twisterrob.inventory.android.content;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;

import net.twisterrob.inventory.android.content.InventoryContract.*;

public class InventoryDatabase {
	private static final InventoryDatabase INSTANCE = new InventoryDatabase();
	private static final String[] NO_PROJ = null;
	private static final String NO_SEL = null;
	private static final String[] NO_ARGS = null;
	private static final String NO_SORT = null;

	private InventoryDatabase() {
		// Singleton
	}

	public static InventoryDatabase getInstance() {
		return INSTANCE;
	}

	public long createProperty(ContentResolver cr, String name, String image) {
		ContentValues cv = new ContentValues();
		cv.put(Item.NAME, name);
		cv.put(Item.IMAGE, image);
		Uri result = cr.insert(Item.ITEM_URI, cv);
		return Long.parseLong(result.getLastPathSegment());
	}

	public Cursor listProperties(ContentResolver cr) {
		return cr.query(Item.DIR_URI, NO_PROJ, NO_SEL, NO_ARGS, NO_SORT);
	}

	public Cursor searchItems(ContentResolver cr, CharSequence query) {
		String[] args = query != null? new String[]{query.toString()} : null;
		return cr.query(Search.URI, NO_PROJ, NO_SEL, args, NO_SORT);
	}
}
