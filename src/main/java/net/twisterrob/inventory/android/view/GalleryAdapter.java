package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.*;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
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

	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.item_gallery, parent, false);

		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		holder.title.setText(getName(cursor));
		holder.count.setText(getCountText(cursor));

		String type = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		image = ImagedDTO.getImage(holder.itemView.getContext(), image);
		displayImageWithType(holder.image, holder.type, image, type);
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

	private void displayImageWithType(ImageView image, ImageView type, String imageName, String typeImageName) {
		final Drawable fallback = ImagedDTO.getFallbackDrawable(image.getContext(), typeImageName);

		if (imageName == null) {
			type.setVisibility(View.INVISIBLE);
			type.setImageDrawable(null);
			image.setImageDrawable(fallback);
		} else {
			type.setVisibility(View.VISIBLE);
			type.setImageDrawable(fallback);
			App.pic().start(image.getContext())
			   .placeholder(fallback)
			   .error(makeError(image.getContext(), fallback))
			   .load(imageName)
			   .into(image)
			;
		}
	}

	private static Drawable makeError(Context context, Drawable fallback) {
		Drawable error = context.getResources().getDrawable(R.drawable.image_error);
		fallback = fallback.mutate();
		fallback.setAlpha(0x80);
		return new LayerDrawable(new Drawable[] {fallback, error});
	}
}
