package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.*;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.utils.glide.VisibilityToggler;
import net.twisterrob.inventory.android.view.GalleryAdapter.ViewHolder;
import net.twisterrob.inventory.android.view.lib.ResourceCursorAdapterWithHolder;

public class GalleryAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public GalleryAdapter(Context context) {
		super(context, R.layout.gallery_item, null, false);
	}

	class ViewHolder {
		TextView title;
		ImageView image;
		ImageView type;
		TextView count;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.title = (TextView)convertView.findViewById(R.id.title);
		holder.image = (ImageView)convertView.findViewById(R.id.image);
		holder.type = (ImageView)convertView.findViewById(R.id.type);
		holder.count = (TextView)convertView.findViewById(R.id.count);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		holder.title.setText(getName(cursor));
		holder.count.setText(getCountText(cursor));

		String type = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		DriveId id = image == null? null : DriveId.decodeFromString(image);
		displayImageWithType(holder.image, holder.type, id, type);
	}

	private static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
	}

	private static String getCountText(Cursor cursor) {
		String countText = null;
		int countIndex = cursor.getColumnIndex(CommonColumns.COUNT_CHILDREN_DIRECT);
		if (countIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0) {
				countText = String.valueOf(count);
			}
		}
		return countText;
	}

	private void displayImageWithType(ImageView image, final ImageView type, DriveId imageName, String typeImageName) {
		Drawable fallback = ImagedDTO.getFallbackDrawable(mContext, typeImageName);
		type.setImageDrawable(fallback);
		type.setVisibility(View.INVISIBLE);
		App.pic().loadDrive(mContext, imageName, new VisibilityToggler(type)).placeholder(fallback).into(image);
	}
}
