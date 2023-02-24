package net.twisterrob.inventory.android.content.contract;

import android.provider.*;

public interface ImageDataColumns {
	//String COLUMN_DATA_URI = "_data";
	String COLUMN_ID = BaseColumns._ID;
	String COLUMN_SIZE = OpenableColumns.SIZE;
	String COLUMN_DISPLAY_NAME = OpenableColumns.DISPLAY_NAME;
	String COLUMN_BLOB = "_dataBlob";
}
