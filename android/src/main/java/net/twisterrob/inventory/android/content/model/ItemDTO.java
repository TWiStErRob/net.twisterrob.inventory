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

public class ItemDTO extends ImagedDTO {
	public long parentID = Item.ID_ADD;
	public String parentName;
	public String categoryName;
	public long property;
	public String propertyName;
	public String lists;
	public long room;
	public long roomRoot;
	public String roomName;

	public ItemDTO() {
		type = Category.DEFAULT;
	}

	public static ItemDTO fromCursor(@NonNull Cursor cursor) {
		ItemDTO item = new ItemDTO();
		return item.fromCursorInternal(cursor);
	}

	@Override protected ItemDTO fromCursorInternal(@NonNull Cursor cursor) {
		super.fromCursorInternal(cursor);

		parentID = DatabaseTools.getOptionalLong(cursor, Item.PARENT_ID, Item.ID_ADD);
		parentName = DatabaseTools.getOptionalString(cursor, Item.PARENT_NAME);
		categoryName = DatabaseTools.getOptionalString(cursor, Item.CATEGORY_NAME);
		property = DatabaseTools.getOptionalLong(cursor, Item.PROPERTY_ID, Property.ID_ADD);
		propertyName = DatabaseTools.getOptionalString(cursor, Item.PROPERTY_NAME);
		room = DatabaseTools.getOptionalLong(cursor, Item.ROOM_ID, Room.ID_ADD);
		roomRoot = DatabaseTools.getOptionalLong(cursor, Item.ROOM_ROOT, Room.ID_ADD);
		roomName = DatabaseTools.getOptionalString(cursor, Item.ROOM_NAME);
		lists = DatabaseTools.getOptionalString(cursor, Item.LISTS);

		return this;
	}

	@Override public Uri getImageUri() {
		return InventoryContract.Item.imageUri(id);
	}
	@Override public CharSequence getShareDescription(Context context) {
		StringBuilder sb = new StringBuilder();
		if (parentName == null) {
			sb.append(context.getString(R.string.item_share_desc_room, name, roomName, propertyName));
		} else {
			sb.append(context.getString(R.string.item_share_desc, name, parentName, roomName, propertyName));
		}

		if (!TextUtils.isEmpty(description)) {
			sb.append("\n").append(description);
		}
		return sb.toString();
	}

	@Override public @NonNull String toString() {
		return String.format(Locale.ROOT, "Item #%1$d: '%2$s' / %3$s", id, name, type);
	}
}
