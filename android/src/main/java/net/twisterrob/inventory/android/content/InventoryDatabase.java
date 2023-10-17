package net.twisterrob.inventory.android.content;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import net.twisterrob.inventory.android.content.InventoryContract.*;

/**
 * @see net.twisterrob.inventory.android.content.InventoryContract
 * @see net.twisterrob.inventory.android.content.InventoryProvider
 */
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

	public long createProperty(@NonNull ContentResolver cr, String name, String image) throws NullPointerException {
		ContentValues cv = new ContentValues();
		cv.put(Item.NAME, name);
		cv.put(Item.IMAGE, image);
		Uri result = cr.insert(Item.ITEM_URI, cv);
		//noinspection ConstantConditions not yet used, likely will change
		return Long.parseLong(result.getLastPathSegment());
	}

	public Cursor listProperties(@NonNull ContentResolver cr) {
		return cr.query(Item.DIR_URI, NO_PROJ, NO_SEL, NO_ARGS, NO_SORT);
	}

	public Cursor searchItems(@NonNull ContentResolver cr, @NonNull CharSequence query) {
		String[] args = new String[] {query.toString()};
		return cr.query(Search.URI, NO_PROJ, NO_SEL, args, NO_SORT);
	}
}
