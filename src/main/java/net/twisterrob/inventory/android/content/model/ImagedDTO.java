package net.twisterrob.inventory.android.content.model;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.DatabaseOpenHelper;

public class ImagedDTO extends DTO {
	private String driveColumn;
	public DriveId image;

	private String drawableColumn;
	public String fallbackImageResourceName;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		if (driveColumn != null) {
			int driveColumnIndex = cursor.getColumnIndex(driveColumn);
			if (driveColumnIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
				if (!cursor.isNull(driveColumnIndex)) {
					image = DriveId.decodeFromString(cursor.getString(driveColumnIndex));
				}
			}
		}

		if (drawableColumn != null) {
			int drawableColumnIndex = cursor.getColumnIndex(drawableColumn);
			if (drawableColumnIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
				fallbackImageResourceName = cursor.getString(drawableColumnIndex);
			}
		}

		return this;
	}

	public int getFallbackDrawableID(Context context) {
		return context.getResources().getIdentifier(fallbackImageResourceName, "drawable", context.getPackageName());
	}

	protected void setImageDriveIdColumnName(String columnName) {
		this.driveColumn = columnName;
	}

	protected void setImageDrawableColumnName(String typeImage) {
		this.drawableColumn = typeImage;
	}
}
