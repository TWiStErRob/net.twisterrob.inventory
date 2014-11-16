package net.twisterrob.inventory.android.content.model;

import java.io.*;
import java.util.Locale;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
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
		return image == null? null : new File(image).isAbsolute()? image
				: new File(Paths.getImageDirectory(context), image).getAbsolutePath();
	}

	public void setImage(Context context, String fullImage) {
		if (fullImage == null) {
			this.image = null;
		} else {
			try {
				String root = Paths.getImageDirectory(context).getCanonicalPath();
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

	public Drawable getFallbackDrawable(Context context) {
		return getFallbackDrawable(context, fallbackImageResourceName);
	}
	public Drawable getFallbackDrawable(Context context, int size, int padding) {
		return getFallbackDrawable(context, fallbackImageResourceName, size, padding);
	}

	public static @NonNull Drawable getFallbackDrawable(Context context, String resourceName) {
		Drawable svg = App.pic().getSVG(context, AndroidTools.getRawResourceID(context, resourceName));
		if (svg != null) {
			return svg;
		}
		return getDrawableFromDrawable(context, resourceName);
	}
	public static @NonNull Drawable getFallbackDrawable(Context context, String resourceName, int size, int padding) {
		Drawable svg =
				App.pic().getSVG(context, AndroidTools.getRawResourceID(context, resourceName), size, padding);
		if (svg != null) {
			return svg;
		}
		return getDrawableFromDrawable(context, resourceName);
	}
	private static Drawable getDrawableFromDrawable(Context context, String resourceName) {
		return context.getResources().getDrawable(AndroidTools.getDrawableResourceID(context, resourceName));
	}
}
