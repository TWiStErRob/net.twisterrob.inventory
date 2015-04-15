package net.twisterrob.inventory.android.content.model;

import java.io.*;
import java.util.Locale;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.RawRes;
import android.widget.ImageView;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public class ImagedDTO extends DTO {
	private static final Logger LOG = LoggerFactory.getLogger(ImagedDTO.class);

	public String image;
	public String typeImage;
	public long type;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		typeImage = DatabaseTools.getOptionalString(cursor, CommonColumns.TYPE_IMAGE);
		image = DatabaseTools.getOptionalString(cursor, CommonColumns.IMAGE);
		type = DatabaseTools.getOptionalLong(cursor, CommonColumns.TYPE, type);

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

	public @RawRes int getFallbackID(Context context) {
		return AndroidTools.getRawResourceID(context, typeImage);
	}
	public static @RawRes int getFallbackID(Context context, Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		return AndroidTools.getRawResourceID(context, image);
	}

	public void loadInto(ImageView image, ImageView type, boolean alwaysShowType) {
		loadInto(image, type, this.image, this.typeImage, alwaysShowType);
	}
	public static void loadInto(ImageView image, ImageView type, String imagePath, String typeName,
			boolean alwaysShowType) {
		String fullImagePath = Constants.Paths.getImagePath(image.getContext(), imagePath);
		int typeID = AndroidTools.getRawResourceID(image.getContext(), typeName);
		loadInto(image, type, fullImagePath, typeID, alwaysShowType);
	}
	public static void loadInto(ImageView image, ImageView type, String fullImagePath, int typeID,
			boolean alwaysShowType) {
		if (fullImagePath == null) {
			if (alwaysShowType) {
				Pic.loadSVG(type.getContext(), typeID).placeholder(R.drawable.transparent_32dp).into(type);
			} else {
				type.setImageDrawable(null); // == Pic.IMAGE_REQUEST.load(null).into(type); Glide#268
			}
			Pic.loadSVG(image.getContext(), typeID).into(image);
		} else {
			Pic.loadSVG(type.getContext(), typeID).placeholder(R.drawable.transparent_32dp).into(type);
			Pic.IMAGE_REQUEST.load(fullImagePath).into(image);
		}
	}

	public void loadInto(ImageView image) {
		loadInto(image, this.image, this.typeImage);
	}
	public static void loadInto(ImageView image, String imagePath, String typeName) {
		String fullImagePath = Constants.Paths.getImagePath(image.getContext(), imagePath);
		int typeID = AndroidTools.getRawResourceID(image.getContext(), typeName);
		loadInto(image, fullImagePath, typeID);
	}
	public static void loadInto(ImageView image, String fullImagePath, int typeID) {
		if (fullImagePath == null) {
			Pic.loadSVG(image.getContext(), typeID).into(image);
		} else {
			Pic.IMAGE_REQUEST.load(fullImagePath).into(image);
		}
	}
}
