package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.content.contract.PropertyType;

public class PropertyDTO extends ImagedDTO {
	public PropertyDTO() {
		type = PropertyType.DEFAULT;
	}

	public static PropertyDTO fromCursor(@NonNull Cursor cursor) {
		PropertyDTO property = new PropertyDTO();
		return property.fromCursorInternal(cursor);
	}

	@Override protected PropertyDTO fromCursorInternal(@NonNull Cursor cursor) {
		super.fromCursorInternal(cursor);
		return this;
	}
	@Override public Uri getImageUri() {
		return InventoryContract.Property.imageUri(id);
	}
	@Override public CharSequence getShareDescription(Context context) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.property_share_desc, name));
		if (!TextUtils.isEmpty(description)) {
			sb.append("\n").append(description);
		}
		return sb.toString();
	}

	@Override public @NonNull String toString() {
		return String.format(Locale.ROOT, "Property #%1$d: '%2$s' / %3$s", id, name, type);
	}
}
