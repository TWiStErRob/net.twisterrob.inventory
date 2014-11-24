package net.twisterrob.inventory.android;

import java.io.File;

import android.content.Context;
import android.support.annotation.AnyRes;
import android.util.TypedValue;

import static android.util.TypedValue.*;

public interface Constants {
	@AnyRes int INVALID_RESOURCE_ID = 0;
	boolean DISABLE = Boolean.parseBoolean("false");

	class Paths {
		public static final String DEFAULT_FOLDER_NAME = "Magic Home Inventory";
		public static final String EXPORT_FILE_NAME_FORMAT = "export-%tF_%<tH-%<tM-%<tS.csv";
		public static final String EXPORT_SDCARD_FOLDER = DEFAULT_FOLDER_NAME;
		public static File getImageDirectory(Context context) {
			return new File(context.getFilesDir(), DEFAULT_FOLDER_NAME);
		}
	}

	interface Prefs {
		@Deprecated
		String DRIVE_FOLDER_ID = "driveRootFolder";
		String CURRENT_LANGUAGE = "currentLanguage";
		String DEFAULT_ENTITY_DETAILS_PAGE = "defaultEntityDetailsPage";
		String DEFAULT_ENTITY_DETAILS_PAGE_DEFAULT = "image";
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
