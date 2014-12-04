package net.twisterrob.inventory.android;

import java.io.File;

import android.content.Context;
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
		public static final String EXPORT_FILE_NAME_FORMAT = "export-%tF_%<tH-%<tM-%<tS.csv";
		public static final String EXPORT_SDCARD_FOLDER = "Magic Home Inventory";
		public static File getImageDirectory(Context context) {
			return new File(context.getFilesDir(), INTERNAL_IMAGES_FOLDER);
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
