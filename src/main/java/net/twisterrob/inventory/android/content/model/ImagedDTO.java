package net.twisterrob.inventory.android.content.model;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.utils.DatabaseUtils;

public class ImagedDTO extends DTO {
	private static final Logger LOG = LoggerFactory.getLogger(ImagedDTO.class);

	public DriveId image;
	public String fallbackImageResourceName;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		fallbackImageResourceName = DatabaseUtils.getOptionalString(cursor, CommonColumns.TYPE_IMAGE);

		String imageDriveId = DatabaseUtils.getOptionalString(cursor, CommonColumns.IMAGE);
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

	public static Drawable getFallbackDrawable(Context context, String resourceName) {
		try {
			Drawable svg = App.pic().getSVG(AndroidTools.getRawResourceID(context, resourceName));
			if (svg != null) {
				return svg;
			}
		} catch (NotFoundException ex) {
			LOG.error("TODO Convert {} to SVG", resourceName);
		}
		return context.getResources().getDrawable(AndroidTools.getDrawableResourceID(context, resourceName));
	}

	public void loadInto(ImageView imageView) {
		Drawable fallback = getFallbackDrawable(imageView.getContext());
		App.pic().load(image).placeholder(fallback).into(imageView);
	}
}
