package net.twisterrob.inventory.android.view.adapters;

import java.text.NumberFormat;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.*;
import android.widget.*;

import com.bumptech.glide.Glide;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ImagedDTO;

public class GalleryGroupViewHolder extends RecyclerView.ViewHolder {
	private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();
	private final TextView title;
	private final ImageView image;
	private final TextView count;

	public GalleryGroupViewHolder(View view, final GalleryEvents listener) {
		super(view);
		title = view.findViewById(R.id.title);
		image = view.findViewById(R.id.image);
		count = view.findViewById(R.id.count);

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
		count.setText(getCountText(cursor));

		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		boolean hasImage = DatabaseTools.getBoolean(cursor, CommonColumns.HAS_IMAGE);
		long imageTime = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE_TIME));
		Type type = Type.from(cursor, CommonColumns.TYPE);
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		ImagedDTO.loadInto(image, hasImage? type : null, id, imageTime, typeImage);
	}

	public void unBind() {
		// FIXME replace this with proper Glide.with calls
		Glide.clear(image);
	}

	private static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
	}

	private static String getCountText(Cursor cursor) {
		int count = cursor.getInt(cursor.getColumnIndexOrThrow(CommonColumns.COUNT_CHILDREN_DIRECT));
		return NUMBER.format(count);
	}
}
