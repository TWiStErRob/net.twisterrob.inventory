package net.twisterrob.inventory.android.view.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ImagedDTO;

public class GalleryGroupViewHolder extends RecyclerView.ViewHolder {
	private TextView title;
	private ImageView image;
	private TextView count;

	public GalleryGroupViewHolder(View view, final RecyclerViewItemEvents listener) {
		super(view);
		title = (TextView)view.findViewById(R.id.title);
		image = (ImageView)view.findViewById(R.id.image);
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
		count.setText(getCountText(cursor));

		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		boolean hasImage = DatabaseTools.getBoolean(cursor, CommonColumns.HAS_IMAGE);
		long imageTime = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE_TIME));
		Type type = Type.from(cursor, CommonColumns.TYPE);
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		ImagedDTO.loadInto(image, hasImage? type : null, id, imageTime, typeImage);
	}

	private static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
	}

	private static String getCountText(Cursor cursor) {
		int count = cursor.getInt(cursor.getColumnIndexOrThrow(CommonColumns.COUNT_CHILDREN_DIRECT));
		return String.valueOf(count);
	}
}
