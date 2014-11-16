package net.twisterrob.inventory.android.view;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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
		View view = inflater.inflate(R.layout.fragment_list_item_gallery, parent, false);

		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		holder.title.setText(getName(cursor));
		holder.count.setText(getCountText(cursor));

		String type = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
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

	private void displayImageWithType(ImageView image, final ImageView type, String imageName, String typeImageName) {
		final Drawable fallback = ImagedDTO.getFallbackDrawable(image.getContext(), typeImageName);
		ViewCompat.setLayerType(type, ViewCompat.LAYER_TYPE_SOFTWARE, null);
		type.setVisibility(View.INVISIBLE);

		App.pic().start(image.getContext(), new RequestListener<String, GlideDrawable>() {
			public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
					boolean isFromMemoryCache, boolean isFirstResource) {
				type.setVisibility(View.VISIBLE);
				type.setImageDrawable(fallback);
				return false;
			}

			public boolean onException(Exception e, String model, Target<GlideDrawable> target,
					boolean isFirstResource) {
				if (model != null) {
					type.setVisibility(View.VISIBLE);
					type.setImageResource(R.drawable.image_error);
				}
				return false;
			}
		}).placeholder(fallback).error(fallback).load(imageName).into(image);
	}
}
