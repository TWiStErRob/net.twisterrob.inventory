package net.twisterrob.inventory.android.view;

import android.app.Activity;
import android.database.Cursor;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.CategoryItemsActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.CategoryAdapter.ViewHolder;

public class CategoryAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	private final Activity activity;

	public CategoryAdapter(Activity activity) {
		super(activity, R.layout.category_list_item, null, false);
		this.activity = activity;
	}

	class ViewHolder {
		ImageView image;
		TextView title;
		TextView count;
		Button items;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.image = (ImageView)convertView.findViewById(R.id.image);
		holder.title = (TextView)convertView.findViewById(R.id.title);
		holder.count = (TextView)convertView.findViewById(R.id.count);
		holder.items = (Button)convertView.findViewById(R.id.items);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		final long id = cursor.getLong(cursor.getColumnIndex(CommonColumns.ID));
		holder.title.setText(getName(cursor));
		holder.count.setText(getCountText(cursor, CommonColumns.COUNT, " subcategories"));
		String countText = getCountText(cursor, Category.ITEM_COUNT, " items");
		holder.items.setText(countText);
		holder.items.setVisibility(countText != null? View.VISIBLE : View.GONE);
		holder.items.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showItems(id);
			}
		});
		App.pic().load(getImageResource(cursor)).into(holder.image);
	}

	private static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
	}

	private static String getCountText(Cursor cursor, String columName, String postfix) {
		String countText = null;
		int countIndex = cursor.getColumnIndex(columName);
		if (countIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0) {
				countText = count + postfix;
			}
		}
		return countText;
	}

	private int getImageResource(Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		int imageResource = mContext.getResources().getIdentifier(image, "drawable", mContext.getPackageName());
		return imageResource;
	}

	private void showItems(long categoryID) {
		activity.startActivity(CategoryItemsActivity.show(categoryID));
	}
}
