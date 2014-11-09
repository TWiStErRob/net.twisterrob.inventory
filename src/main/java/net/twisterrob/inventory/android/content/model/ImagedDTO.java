package net.twisterrob.inventory.android.content.model;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public class ImagedDTO extends DTO {
	private static final Logger LOG = LoggerFactory.getLogger(ImagedDTO.class);

	public DriveId image;
	public String fallbackImageResourceName;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		fallbackImageResourceName = DatabaseTools.getOptionalString(cursor, CommonColumns.TYPE_IMAGE);

		String imageDriveId = DatabaseTools.getOptionalString(cursor, CommonColumns.IMAGE);
		if (imageDriveId != null) {
			image = DriveId.decodeFromString(imageDriveId);
		} else {
			image = null;
		}

		return this;
	}

	public Drawable getFallbackDrawable(Context context) {
		return getFallbackDrawable(context, fallbackImageResourceName);
	}
	public Drawable getFallbackDrawable(Context context, int size, int padding) {
		return getFallbackDrawable(context, fallbackImageResourceName, size, padding);
	}

	public static Drawable getFallbackDrawable(Context context, String resourceName) {
		try {
			Drawable svg = App.pic().getSVG(context, AndroidTools.getRawResourceID(context, resourceName));
			if (svg != null) {
				return svg;
			}
		} catch (NotFoundException ex) {
			LOG.error("TODO Convert {} to SVG", resourceName);
		}
		return context.getResources().getDrawable(AndroidTools.getDrawableResourceID(context, resourceName));
	}
	public static Drawable getFallbackDrawable(Context context, String resourceName, int size, int padding) {
		try {
			Drawable svg =
					App.pic().getSVG(context, AndroidTools.getRawResourceID(context, resourceName), size, padding);
			if (svg != null) {
				return svg;
			}
		} catch (NotFoundException ex) {
			LOG.error("TODO Convert {} to SVG", resourceName);
		}
		return context.getResources().getDrawable(AndroidTools.getDrawableResourceID(context, resourceName));
	}
}
