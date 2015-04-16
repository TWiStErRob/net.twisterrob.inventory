package net.twisterrob.inventory.android.content;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.content.ContentResolver.*;

import net.twisterrob.inventory.android.BuildConfig;

// content://net.twisterrob.inventory/item/100008
public final class InventoryContract {
	public static final String AUTHORITY = BuildConfig.APPLICATION_ID;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final class Item implements CommonColumns {
		private static final long ID_INVALID = net.twisterrob.inventory.android.content.contract.Item.ID_ADD;

		private static final String SUBTYPE = "vnd." + AUTHORITY + ".item";

		static final String DIR_URI_PATH = "items";
		public static final Uri DIR_URI = Uri.withAppendedPath(CONTENT_URI, DIR_URI_PATH);
		public static final String DIR_TYPE = CURSOR_DIR_BASE_TYPE + "/" + SUBTYPE;

		static final String ITEM_URI_PATH = "item";
		public static final Uri ITEM_URI = Uri.withAppendedPath(CONTENT_URI, ITEM_URI_PATH);
		public static final String ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/" + SUBTYPE;

		public static final Uri ITEM_IMAGES_URI = Uri.withAppendedPath(ITEM_URI, Image.IMAGE_URI_PATH);

		public static long getID(Uri data) {
			if (data == null) {
				return ID_INVALID;
			}
			if (!data.toString().startsWith(ITEM_URI.toString())) {
				return ID_INVALID;
			}
			try {
				return Long.parseLong(data.getLastPathSegment());
			} catch (NumberFormatException ex) {
				return ID_INVALID;
			}
		}
	}

	private static final class Image {
		static final String IMAGE_URI_PATH = "images";
	}

	public static final class Search {
		static final String URI_PATH = Item.DIR_URI_PATH + "/" + "search";
		public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, URI_PATH);
		public static final String TYPE = CURSOR_DIR_BASE_TYPE + "/" + Item.SUBTYPE;

		static final String URI_PATH_SUGGEST = Item.DIR_URI_PATH + "/" + SearchManager.SUGGEST_URI_PATH_QUERY;
		public static final Uri URI_SUGGEST = Uri.withAppendedPath(CONTENT_URI, URI_PATH_SUGGEST);
		public static final String TYPE_SUGGEST = SearchManager.SUGGEST_MIME_TYPE;
	}

	/**
	 * This interface defines common columns found in multiple tables.
	 */
	public interface CommonColumns extends BaseColumns {
		String NAME = "name";
		String IMAGE = "image";
	}
}