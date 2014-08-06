package net.twisterrob.inventory.android.content.model;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public class ImagedDTO extends DTO {
	private static final Logger LOG = LoggerFactory.getLogger(ImagedDTO.class);

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

	public Drawable getFallbackDrawable(Context context) {
		return getFallbackDrawable(context, fallbackImageResourceName);
	}

	public static Drawable getFallbackDrawable(Context context, String resourceName) {
		try {
			return App.pic().getSVG(AndroidTools.getRawResourceID(context, resourceName));
		} catch (NotFoundException ex) {
			LOG.error("TODO Convert {} to SVG", resourceName);
			return context.getResources().getDrawable(AndroidTools.getDrawableResourceID(context, resourceName));
		}
	}
}
