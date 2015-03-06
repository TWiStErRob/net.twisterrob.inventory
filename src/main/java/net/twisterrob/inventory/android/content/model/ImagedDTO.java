package net.twisterrob.inventory.android.content.model;

import java.io.*;
import java.util.Locale;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.RawRes;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.Constants;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public class ImagedDTO extends DTO {
	private static final Logger LOG = LoggerFactory.getLogger(ImagedDTO.class);

	public String image;
	public String fallbackImageResourceName;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		fallbackImageResourceName = DatabaseTools.getOptionalString(cursor, CommonColumns.TYPE_IMAGE);
		image = DatabaseTools.getOptionalString(cursor, CommonColumns.IMAGE);

		return this;
	}

	public String getImage(Context context) {
		return Constants.Paths.getImagePath(context, image);
	}

	public void setImage(Context context, String fullImage) {
		if (fullImage == null) {
			this.image = null;
		} else {
			try {
				String root = Paths.getImageDirectory(context).getCanonicalPath() + File.separator;
				String image = new File(fullImage).getCanonicalPath();
				if (!image.startsWith(root)) {
					throw new IllegalArgumentException(String.format(Locale.ROOT,
							"Image is not in internal storage (%3$s): %2$s (<- %1$s)", fullImage, image, root));
				}
				this.image = image.substring(root.length());
			} catch (IOException e) {
				LOG.error("Cannot find out the location of {}", fullImage, e);
			}
		}
	}

	public @RawRes int getFallbackResource(Context context) {
		return AndroidTools.getRawResourceID(context, fallbackImageResourceName);
	}

	public static @RawRes int getFallbackID(Context context, Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		return AndroidTools.getRawResourceID(context, image);
	}
}
