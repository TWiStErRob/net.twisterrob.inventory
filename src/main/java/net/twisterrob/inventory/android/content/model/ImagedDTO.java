package net.twisterrob.inventory.android.content.model;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public class ImagedDTO extends DTO {
	public DriveId image;
	public String fallbackImageResourceName;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		int driveColumnIndex = cursor.getColumnIndex(CommonColumns.IMAGE);
		if (driveColumnIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			if (!cursor.isNull(driveColumnIndex)) {
				image = DriveId.decodeFromString(cursor.getString(driveColumnIndex));
			}
		}

		int drawableColumnIndex = cursor.getColumnIndex(CommonColumns.TYPE_IMAGE);
		if (drawableColumnIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			fallbackImageResourceName = cursor.getString(drawableColumnIndex);
		}

		return this;
	}

	public int getFallbackDrawableID(Context context) {
		return context.getResources().getIdentifier(fallbackImageResourceName, "drawable", context.getPackageName());
	}
}
