package net.twisterrob.inventory.android;

import java.io.File;
import java.util.*;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.AnyRes;
import android.util.TypedValue;

import static android.util.TypedValue.*;

public interface Constants {
	@AnyRes int INVALID_RESOURCE_ID = 0;
	boolean DISABLE = Boolean.parseBoolean("false");
	String AUTHORITY_IMAGES = BuildConfig.APPLICATION_ID + ".images";

	class Paths {
		/** Warning: this is used inlined in paths_images.xml because path doesn't support string resources */
		private static final String INTERNAL_IMAGES_FOLDER = "images";
		public static final String DRIVE_HOME_FOLDER = "Magic Home Inventory";
		public static File getImageDirectory(Context context) {
			return new File(context.getFilesDir(), INTERNAL_IMAGES_FOLDER);
		}
		public static File getImageFile(Context context, String image) {
			if (image == null) {
				return null;
			}
			File imageFile = new File(image);
			return imageFile.isAbsolute()? imageFile : new File(Paths.getImageDirectory(context), image);
		}
		public static String getImagePath(Context context, String image) {
			File imageFile = getImageFile(context, image);
			return imageFile != null? imageFile.getAbsolutePath() : null;
		}
		public static String getExportFileName() {
			return String.format(Locale.ROOT, "MagicHomeInventory-%tF_%<tH-%<tM-%<tS.zip", Calendar.getInstance());
		}
		public static File getPhoneHome() {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}
	}

	interface Prefs {
		@Deprecated
		String DRIVE_FOLDER_ID = "driveRootFolder";
		String CURRENT_LANGUAGE = "currentLanguage";
		String LAST_EXPORT_DRIVE_ID = "lastExportDriveId";
	}

	class Dimensions {
		/** dp */
		private static final int ACTIONBAR_ICON_SIZE = 48;
		/** dp */
		private static final int ACTIONBAR_ICON_PADDING = 4;

		public static int getActionbarIconSize(Context context) {
			return (int)dpToPixels(context, ACTIONBAR_ICON_SIZE);
		}
		public static int getActionbarIconPadding(Context context) {
			return (int)dpToPixels(context, ACTIONBAR_ICON_PADDING);
		}
		private static float dpToPixels(Context context, int dp) {
			return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
		}
	}
}
