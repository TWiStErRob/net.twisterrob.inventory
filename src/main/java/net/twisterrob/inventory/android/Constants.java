package net.twisterrob.inventory.android;

public interface Constants {
	String DEFAULT_DRIVE_FOLDER_NAME = "Magic Home Inventory";
	String EXPORT_FILE_NAME_FORMAT = "export-%tF_%<tH-%<tM-%<tS.csv";
	String EXPORT_SDCARD_FOLDER = "Magic Home Inventory";

	interface Prefs {
		String DRIVE_FOLDER_ID = "driveRootFolder";
		String CURRENT_LANGUAGE = "currentLanguage";
		String DEFAULT_ENTITY_DETAILS_PAGE = "defaultEntityDetailsPage";
		String DEFAULT_ENTITY_DETAILS_PAGE_DEFAULT = "image";
		String LAST_EXPORT_DRIVE_ID = "lastExportDriveId";
	}
}
