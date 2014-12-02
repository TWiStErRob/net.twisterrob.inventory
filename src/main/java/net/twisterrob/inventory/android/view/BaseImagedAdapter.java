package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.*;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;

public class BaseImagedAdapter<VH extends BaseImagedAdapter.ViewHolder> extends CursorRecyclerAdapter<VH> {
	private final int layoutResource;
	private final RecyclerViewItemEvents listener;

	public BaseImagedAdapter(Cursor cursor, RecyclerViewItemEvents listener) {
		this(cursor, Constants.INVALID_RESOURCE_ID, listener);
	}
	public BaseImagedAdapter(Cursor cursor, int layoutResource, RecyclerViewItemEvents listener) {
		super(cursor);
		this.layoutResource = layoutResource;
		this.listener = listener;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View view) {
			super(view);
			image = (ImageView)view.findViewById(R.id.image);
			ViewCompat.setLayerType(image, ViewCompat.LAYER_TYPE_SOFTWARE, null); // for SVGs
			title = (TextView)view.findViewById(R.id.title);

			view.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					listener.onItemClick(ViewHolder.this);
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override public boolean onLongClick(View v) {
					listener.onItemLongClick(ViewHolder.this);
					return true;
				}
			});
		}

		public final ImageView image;
		public final TextView title;
	}

	@SuppressWarnings("unchecked")
	@Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		return (VH)new ViewHolder(inflateView(parent, layoutResource));
	}
	protected View inflateView(ViewGroup parent, int layoutResource) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return inflater.inflate(layoutResource, parent, false);
	}

	@Override public void onBindViewHolder(VH holder, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		image = ImagedDTO.getImage(holder.itemView.getContext(), image);

		holder.title.setText(name);
		loadImageWithFallback(holder.image, image, typeImage);
	}

	protected static void loadImageWithFallback(ImageView image, String imageName, String typeImageName) {
		final Drawable fallback = ImagedDTO.getFallbackDrawable(image.getContext(), typeImageName);

		if (imageName == null) {
			image.setImageDrawable(fallback);
		} else {
			App.pic().start(image.getContext())
			   .placeholder(fallback)
			   .error(makeError(image.getContext(), fallback))
			   .load(imageName)
			   .into(image)
			;
		}
	}

	protected static Drawable makeError(Context context, Drawable fallback) {
		Drawable error = context.getResources().getDrawable(R.drawable.image_error);
		fallback = fallback.mutate();
		fallback.setAlpha(0x80);
		return new LayerDrawable(new Drawable[] {fallback, error});
	}
}