package net.twisterrob.inventory.android.content.model;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.DatabaseOpenHelper;

public class ImagedDTO extends DTO {
	private String driveColumn;
	public DriveId imageDriveID;

	private String drawableColumn;
	public String imageResourceName;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		int driveColumnIndex = cursor.getColumnIndex(driveColumn);
		if (driveColumnIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			imageDriveID = DriveId.decodeFromString(cursor.getString(driveColumnIndex));
		}

		int drawableColumnIndex = cursor.getColumnIndex(drawableColumn);
		if (drawableColumnIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			imageResourceName = cursor.getString(drawableColumnIndex);
		}

		return this;
	}

	public int getImageResourceID(Context context) {
		return context.getResources().getIdentifier(imageResourceName, "drawable", context.getPackageName());
	}

	protected void setImageDriveIdColumnName(String columnName) {
		this.driveColumn = columnName;
	}

	protected void setImageDrawableColumnName(String typeImage) {
		this.drawableColumn = typeImage;
	}
}
