package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.*;

import com.squareup.picasso.Callback;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.view.GalleryAdapter.ViewHolder;

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
		setImageAndType(holder, cursor);
	}

	private static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
	}

	private static String getCountText(Cursor cursor) {
		String countText = null;
		int countIndex = cursor.getColumnIndex(CommonColumns.COUNT);
		if (countIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0) {
				countText = String.valueOf(count);
			}
		}
		return countText;
	}

	private void setImageAndType(final ViewHolder holder, Cursor cursor) {
		int typeColumn = cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE);
		String type = cursor.getString(typeColumn);
		int typeResource = mContext.getResources().getIdentifier(type, "drawable", mContext.getPackageName());
		holder.type.setImageResource(typeResource);
		holder.type.setVisibility(View.INVISIBLE);

		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		//App.pic().getPicasso().cancelRequest(holder.image); // TODO check scrolling
		App.pic().loadDriveId(image).placeholder(typeResource).into(holder.image, new Callback() {
			public void onSuccess() {
				holder.type.setVisibility(View.VISIBLE);
			}

			public void onError() {
				holder.type.setVisibility(View.INVISIBLE);
			}
		});
	}
}
