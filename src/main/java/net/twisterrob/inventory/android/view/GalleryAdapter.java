package net.twisterrob.inventory.android.view;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.view.GalleryAdapter.ViewHolder;

public class GalleryAdapter extends CursorRecyclerAdapter<ViewHolder> {
	public interface GalleryItemEvents extends RecyclerViewItemEvents {
	}

	private final GalleryItemEvents listener;

	public GalleryAdapter(Cursor cursor, GalleryItemEvents listener) {
		super(cursor);
		this.listener = listener;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View view) {
			super(view);
			title = (TextView)view.findViewById(R.id.title);
			image = (ImageView)view.findViewById(R.id.image);
			type = (ImageView)view.findViewById(R.id.type);
			count = (TextView)view.findViewById(R.id.count);

			view.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					listener.onItemClick(ViewHolder.this);
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override public boolean onLongClick(View v) {
					return listener.onItemLongClick(ViewHolder.this);
				}
			});
		}

		TextView title;
		ImageView image;
		ImageView type;
		TextView count;
	}

	@Override public int getItemViewType(int position) {
		return isGroup(position)? R.layout.item_gallery_group : R.layout.item_gallery;
	}
	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		holder.title.setText(getName(cursor));
		holder.count.setText(getCountText(cursor));

		String type = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		image = Constants.Paths.getImagePath(holder.itemView.getContext(), image);
		if (holder.getItemViewType() == R.layout.item_gallery) {
			displayImageWithType(holder.image, holder.type, image, type);
		} else {
			displayImageWithType(holder.image, holder.image, image, type);
		}
	}

	public boolean isGroup(int position) {
		Cursor c = getCursor();
		c.moveToPosition(position);
		return isGroup(c);
	}

	private static boolean isGroup(Cursor cursor) {
		int groupIndex = cursor.getColumnIndex("group"); // boolean
		return groupIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN && cursor.getInt(groupIndex) == 1;
	}

	private static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
	}

	private static String getCountText(Cursor cursor) {
		String countText = null;
		int countIndex = cursor.getColumnIndex(CommonColumns.COUNT_CHILDREN_DIRECT);
		if (countIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0 || isGroup(cursor)) {
				countText = String.valueOf(count);
			}
		}
		return countText;
	}

	private void displayImageWithType(ImageView image, ImageView type, String imagePath, String typeImageName) {
		int typeID = AndroidTools.getRawResourceID(type.getContext(), typeImageName);

		if (imagePath == null) {
			type.setImageDrawable(null);
			Pic.SVG_REQUEST.load(typeID).into(image);
		} else {
			Pic.SVG_REQUEST.load(typeID).into(type);
			Pic.IMAGE_REQUEST
					.load(imagePath)
					.thumbnail(Pic.SVG_REQUEST.load(typeID))
					.into(image);
		}
	}
}
