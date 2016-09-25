package net.twisterrob.inventory.android.view.adapters;

import java.text.NumberFormat;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.*;
import android.widget.*;

import com.bumptech.glide.Glide;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ImagedDTO;

public class GalleryViewHolder extends RecyclerView.ViewHolder {
	private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();
	private final TextView title;
	private final ImageView image;
	private final ImageView type;
	private final TextView count;
	private ImagedDTO entity;

	public GalleryViewHolder(View view, final GalleryEvents listener) {
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
		type.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				listener.onTypeClick(getAdapterPosition(), entity);
			}
		});
		type.setOnLongClickListener(new OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				return listener.onItemLongClick(getAdapterPosition(), getItemId());
			}
		});
	}

	public void bind(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		Type type = Type.from(cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE)));
		boolean hasImage = DatabaseTools.getBoolean(cursor, CommonColumns.HAS_IMAGE);
		long imageTime = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE_TIME));
		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		String countText = getCountText(cursor);
		entity = (ImagedDTO)type.fromCursor(cursor); // TODO collapse cursor getters to use DTO

		title.setText(name);
		count.setText(countText);
		AndroidTools.displayedIfHasText(count);

		ImagedDTO.loadInto(this.image, this.type, hasImage? type : null, id, imageTime, typeImage, true);
	}

	public void unBind() {
		// FIXME replace this with proper Glide.with calls
		Glide.clear(image);
		Glide.clear(type);
	}

	private static String getCountText(Cursor cursor) {
		String countText = null;
		int countIndex = cursor.getColumnIndex(CommonColumns.COUNT_CHILDREN_DIRECT);
		if (countIndex != DatabaseTools.INVALID_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0) {
				countText = NUMBER.format(count);
			}
		}
		return countText;
	}
}
