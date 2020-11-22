package net.twisterrob.inventory.android.content.model;

import org.slf4j.*;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.*;

import net.twisterrob.android.content.glide.LongSignature;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.*;

public abstract class ImagedDTO extends DTO {
	private static final Logger LOG = LoggerFactory.getLogger(ImagedDTO.class);

	public boolean hasImage;
	public long imageTime;
	public String typeImage;
	public long type;
	public byte[] image;

	@Override protected ImagedDTO fromCursorInternal(@NonNull Cursor cursor) {
		super.fromCursorInternal(cursor);

		typeImage = DatabaseTools.getOptionalString(cursor, CommonColumns.TYPE_IMAGE, typeImage);
		hasImage = DatabaseTools.getOptionalBoolean(cursor, CommonColumns.HAS_IMAGE, hasImage);
		imageTime = DatabaseTools.getOptionalLong(cursor, CommonColumns.IMAGE_TIME, imageTime);
		type = DatabaseTools.getOptionalLong(cursor, CommonColumns.TYPE_ID, type);

		return this;
	}

	public abstract Uri getImageUri();
	public abstract CharSequence getShareDescription(Context context);

	public Intent createShareIntent(Context context) {
		@StringRes int id = R.string.action_share;
		Intent shareIntent = new Intent(Intent.ACTION_SEND)
				.setType("text/plain")
				.putExtra(Intent.EXTRA_SUBJECT, name)
				.putExtra(Intent.EXTRA_TEXT, name);
		// use better description if available
		CharSequence text = getShareDescription(context);
		if (text != null) {
			id = R.string.action_share_details;
			shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		}
		// attach image if possible
		if (hasImage) {
			id = R.string.action_share_image;
			shareIntent
					.setType("image/jpeg")
					.putExtra(Intent.EXTRA_STREAM, getImageUri())
					.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			;
		}
		return Intent.createChooser(shareIntent, context.getText(id));
	}

	public static @RawRes int getFallbackID(Context context, Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		return ResourceTools.getRawResourceID(context, image);
	}

	public void loadInto(ImageView image, ImageView type, boolean alwaysShowType) {
		Uri fullImagePath = hasImage? getImageUri() : null;
		int typeID = ResourceTools.getRawResourceID(image.getContext(), this.typeImage);
		loadInto(image, type, fullImagePath, imageTime, typeID, alwaysShowType);
	}

	public static void loadInto(ImageView image, ImageView type, Type entity, long id, long signature, String typeName,
			boolean alwaysShowType) {
		Uri uri = entity != null? entity.getImageUri(id) : null;
		int typeID = ResourceTools.getRawResourceID(image.getContext(), typeName);
		loadInto(image, type, uri, signature, typeID, alwaysShowType);
	}
	private static void loadInto(ImageView image, ImageView type, Uri fullImagePath, long signature, int typeID,
			boolean alwaysShowType) {
		if (fullImagePath == null) {
			if (alwaysShowType) {
				Pic.svg().load(typeID).into(type);
			} else {
				type.setImageDrawable(null); // == Pic.IMAGE_REQUEST.load(null).into(type); Glide#268
			}
			Pic.svgNoTint().load(typeID).into(image);
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
		int typeID = ResourceTools.getRawResourceID(image.getContext(), typeName);
		loadInto(image, uri, signature, typeID);
	}
	private static void loadInto(ImageView image, Uri fullImagePath, long signature, int typeID) {
		if (fullImagePath == null) {
			Pic.svgNoTint().load(typeID).into(image);
		} else {
			Pic.jpg()
			   .signature(new LongSignature(signature))
			   .load(fullImagePath)
			   .into(image);
		}
	}
}
