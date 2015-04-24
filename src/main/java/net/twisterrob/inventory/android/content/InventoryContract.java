package net.twisterrob.inventory.android.content;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.content.ContentResolver.*;

import net.twisterrob.inventory.android.BuildConfig;

// content://net.twisterrob.inventory/item/12345
public final class InventoryContract {
	public static final String AUTHORITY = BuildConfig.APPLICATION_ID;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final class Item implements CommonColumns {
		private static final long ID_INVALID = net.twisterrob.inventory.android.content.contract.Item.ID_ADD;

		private static final String SUBTYPE = "vnd." + AUTHORITY + ".item";

		static final String DIR_URI_SEGMENT = "items";
		public static final Uri DIR_URI = Uri.withAppendedPath(CONTENT_URI, DIR_URI_SEGMENT);
		public static final String DIR_TYPE = CURSOR_DIR_BASE_TYPE + "/" + SUBTYPE;

		static final String ITEM_URI_SEGMENT = "item";
		public static final Uri ITEM_URI = Uri.withAppendedPath(CONTENT_URI, ITEM_URI_SEGMENT);
		public static final String ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/" + SUBTYPE;

		public static long getID(Uri data) {
			return Helpers.getID(data, ITEM_URI, ID_INVALID);
		}
		public static Uri imageUri(long id) {
			return Helpers.getImageUri(ITEM_URI, id);
		}
	}

	public static final class Room implements CommonColumns {
		private static final long ID_INVALID = net.twisterrob.inventory.android.content.contract.Room.ID_ADD;

		private static final String SUBTYPE = "vnd." + AUTHORITY + ".room";

		static final String DIR_URI_SEGMENT = "rooms";
		public static final Uri DIR_URI = Uri.withAppendedPath(CONTENT_URI, DIR_URI_SEGMENT);
		public static final String DIR_TYPE = CURSOR_DIR_BASE_TYPE + "/" + SUBTYPE;

		static final String ITEM_URI_SEGMENT = "room";
		public static final Uri ITEM_URI = Uri.withAppendedPath(CONTENT_URI, ITEM_URI_SEGMENT);
		public static final String ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/" + SUBTYPE;

		public static long getID(Uri data) {
			return Helpers.getID(data, ITEM_URI, ID_INVALID);
		}
		public static Uri imageUri(long id) {
			return Helpers.getImageUri(ITEM_URI, id);
		}
	}

	public static final class Property implements CommonColumns {
		private static final long ID_INVALID = net.twisterrob.inventory.android.content.contract.Property.ID_ADD;

		private static final String SUBTYPE = "vnd." + AUTHORITY + ".property";

		static final String DIR_URI_SEGMENT = "properties";
		public static final Uri DIR_URI = Uri.withAppendedPath(CONTENT_URI, DIR_URI_SEGMENT);
		public static final String DIR_TYPE = CURSOR_DIR_BASE_TYPE + "/" + SUBTYPE;

		static final String ITEM_URI_SEGMENT = "property";
		public static final Uri ITEM_URI = Uri.withAppendedPath(CONTENT_URI, ITEM_URI_SEGMENT);
		public static final String ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/" + SUBTYPE;

		public static long getID(Uri data) {
			return Helpers.getID(data, ITEM_URI, ID_INVALID);
		}
		public static Uri imageUri(long id) {
			return Helpers.getImageUri(ITEM_URI, id);
		}
	}

	public static final class Category implements CommonColumns {
		private static final long ID_INVALID = net.twisterrob.inventory.android.content.contract.Category.ID_ADD;

		private static final String SUBTYPE = "vnd." + AUTHORITY + ".category";

		static final String DIR_URI_SEGMENT = "categories";
		public static final Uri DIR_URI = Uri.withAppendedPath(CONTENT_URI, DIR_URI_SEGMENT);
		public static final String DIR_TYPE = CURSOR_DIR_BASE_TYPE + "/" + SUBTYPE;

		static final String ITEM_URI_SEGMENT = "category";
		public static final Uri ITEM_URI = Uri.withAppendedPath(CONTENT_URI, ITEM_URI_SEGMENT);
		public static final String ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/" + SUBTYPE;

		public static long getID(Uri data) {
			return Helpers.getID(data, ITEM_URI, ID_INVALID);
		}
		public static Uri imageUri(long id) {
			return Helpers.getImageUri(ITEM_URI, id);
		}
	}

	public static final class Search {
		static final String URI_PATH = Item.DIR_URI_SEGMENT + "/" + "search";
		public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, URI_PATH);
		public static final String TYPE = CURSOR_DIR_BASE_TYPE + "/" + Item.SUBTYPE;

		static final String URI_PATH_SUGGEST = Item.DIR_URI_SEGMENT + "/" + SearchManager.SUGGEST_URI_PATH_QUERY;
		public static final Uri URI_SUGGEST = Uri.withAppendedPath(CONTENT_URI, URI_PATH_SUGGEST);
		public static final String TYPE_SUGGEST = SearchManager.SUGGEST_MIME_TYPE;
	}

	static final class Helpers {
		static final String IMAGE_URI_SEGMENT = "image";

		private Helpers() {
			// utility class, do not instantiate
		}

		protected static long getID(Uri data, Uri base, long invalid) {
			if (data == null) {
				return invalid;
			}
			if (!data.toString().startsWith(base.toString())) {
				return invalid;
			}
			try {
				return Long.parseLong(data.getPathSegments().get(base.getPathSegments().size()));
			} catch (NumberFormatException ex) {
				return invalid;
			}
		}

		protected static Uri getImageUri(Uri base, long id) {
			return base
					.buildUpon()
					.appendEncodedPath(String.valueOf(id))
					.appendEncodedPath(IMAGE_URI_SEGMENT)
					.build()
					;
		}
	}

	/**
	 * This interface defines common columns found in multiple tables.
	 */
	public interface CommonColumns extends BaseColumns {
		String NAME = "name";
		String IMAGE = "image";
	}
}