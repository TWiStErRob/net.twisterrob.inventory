package net.twisterrob.inventory.android.content.model;

import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.content.contract.*;

public class RoomDTO extends ImagedDTO {
	public long propertyID = Property.ID_ADD;
	public String propertyName;
	public long rootItemID = Item.ID_ADD;

	public RoomDTO() {
		type = RoomType.DEFAULT;
	}

	public static RoomDTO fromCursor(@NonNull Cursor cursor) {
		RoomDTO room = new RoomDTO();
		return room.fromCursorInternal(cursor);
	}

	@Override protected RoomDTO fromCursorInternal(@NonNull Cursor cursor) {
		super.fromCursorInternal(cursor);

		rootItemID = DatabaseTools.getOptionalLong(cursor, Room.ROOT_ITEM, Item.ID_ADD);
		propertyID = DatabaseTools.getOptionalLong(cursor, Room.PROPERTY_ID, Property.ID_ADD);
		propertyName = DatabaseTools.getOptionalString(cursor, Room.PROPERTY_NAME);

		return this;
	}
	@Override public Uri getImageUri() {
		return InventoryContract.Room.imageUri(id);
	}
	@Override public CharSequence getShareDescription(Context context) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getString(R.string.room_share_desc, name, propertyName));
		if (!TextUtils.isEmpty(description)) {
			sb.append("\n").append(description);
		}
		return sb.toString();
	}

	@Override public @NonNull String toString() {
		return String.format(Locale.ROOT, "Room #%2$d: '%3$s' / %4$s in property #%1$d", propertyID, id, name, type);
	}
}
