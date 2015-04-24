package net.twisterrob.inventory.android.content.model;

import org.slf4j.*;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.RawRes;
import android.widget.ImageView;

import net.twisterrob.android.content.glide.LongSignature;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.java.utils.StringTools;

public abstract class ImagedDTO extends DTO {
	private static final Logger LOG = LoggerFactory.getLogger(ImagedDTO.class);

	public boolean hasImage;
	public long imageTime;
	public String typeImage;
	public long type;
	public byte[] image;

	@Override
	protected ImagedDTO fromCursorInternal(Cursor cursor) {
		super.fromCursorInternal(cursor);

		typeImage = DatabaseTools.getOptionalString(cursor, CommonColumns.TYPE_IMAGE, typeImage);
		hasImage = DatabaseTools.getOptionalBoolean(cursor, CommonColumns.HAS_IMAGE, hasImage);
		imageTime = DatabaseTools.getOptionalLong(cursor, CommonColumns.IMAGE_TIME, imageTime);
		type = DatabaseTools.getOptionalLong(cursor, CommonColumns.TYPE_ID, type);

		return this;
	}

	public abstract Uri getImageUri();

	public static @RawRes int getFallbackID(Context context, Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		return AndroidTools.getRawResourceID(context, image);
	}

	public void loadInto(ImageView image, ImageView type, boolean alwaysShowType) {
		String fullImagePath = hasImage? StringTools.toString(getImageUri(), null) : null;
		int typeID = AndroidTools.getRawResourceID(image.getContext(), this.typeImage);
		loadInto(image, type, fullImagePath, imageTime, typeID, alwaysShowType);
	}

	public static void loadInto(ImageView image, ImageView type, Type entity, long id, long signature, String typeName,
			boolean alwaysShowType) {
		Uri uri = entity != null? entity.getImageUri(id) : null;
		String fullImagePath = StringTools.toString(uri, null);
		int typeID = AndroidTools.getRawResourceID(image.getContext(), typeName);
		loadInto(image, type, fullImagePath, signature, typeID, alwaysShowType);
	}
	private static void loadInto(ImageView image, ImageView type, String fullImagePath, long signature, int typeID,
			boolean alwaysShowType) {
		if (fullImagePath == null) {
			if (alwaysShowType) {
				Pic.svg().load(typeID).into(type);
			} else {
				type.setImageDrawable(null); // == Pic.IMAGE_REQUEST.load(null).into(type); Glide#268
			}
			Pic.svg().load(typeID).into(image);
		} else {
			Pic.svg().load(typeID).into(type);
			Pic.jpg()
			   .signature(new LongSignature(signature))
			   .load(fullImagePath)
			   .into(image);
		}
	}

	public static void loadInto(ImageView image, Type entity, long id, long signature, String typeName) {
		Uri uri = entity != null? entity.getImageUri(id) : null;
		String fullImagePath = StringTools.toString(uri, null);
		int typeID = AndroidTools.getRawResourceID(image.getContext(), typeName);
		loadInto(image, fullImagePath, signature, typeID);
	}
	private static void loadInto(ImageView image, String fullImagePath, long signature, int typeID) {
		if (fullImagePath == null) {
			Pic.svg().load(typeID).into(image);
		} else {
			Pic.jpg()
			   .signature(new LongSignature(signature))
			   .load(fullImagePath)
			   .into(image);
		}
	}
}
