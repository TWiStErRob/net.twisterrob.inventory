package net.twisterrob.inventory.android.view.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;

public class GalleryViewHolder extends RecyclerView.ViewHolder {
	private TextView title;
	private ImageView image;
	private ImageView type;
	private TextView count;

	public GalleryViewHolder(View view, final RecyclerViewItemEvents listener) {
		super(view);
		title = (TextView)view.findViewById(R.id.title);
		image = (ImageView)view.findViewById(R.id.image);
		type = (ImageView)view.findViewById(R.id.type);
		count = (TextView)view.findViewById(R.id.count);

		view.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				listener.onItemClick(getAdapterPosition(), getItemId());
			}
		});
		view.setOnLongClickListener(new OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				return listener.onItemLongClick(getAdapterPosition(), getItemId());
			}
		});
	}

	public void bind(Cursor cursor) {
		title.setText(getName(cursor));
		String countText = getCountText(cursor);
		count.setText(countText);
		count.setVisibility(countText != null? View.VISIBLE : View.GONE);

		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		ImagedDTO.loadInto(image, type, imagePath, typeImage, false);
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
}
