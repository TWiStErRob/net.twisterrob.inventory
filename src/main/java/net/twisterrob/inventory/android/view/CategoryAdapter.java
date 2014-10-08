package net.twisterrob.inventory.android.view;

import android.app.Activity;
import android.database.Cursor;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.CategoryItemsActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.CategoryAdapter.ViewHolder;
import net.twisterrob.inventory.android.view.lib.ResourceCursorAdapterWithHolder;

public class CategoryAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	private final Activity activity;

	public CategoryAdapter(Activity activity) {
		super(activity, R.layout.category_list_item, null, false);
		this.activity = activity;
	}

	class ViewHolder {
		ImageView image;
		TextView title;
		TextView stats;
		Button items;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.image = (ImageView)convertView.findViewById(R.id.image);
		holder.title = (TextView)convertView.findViewById(R.id.title);
		holder.stats = (TextView)convertView.findViewById(R.id.stats);
		holder.items = (Button)convertView.findViewById(R.id.items);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		final long id = cursor.getLong(cursor.getColumnIndex(CommonColumns.ID));
		holder.title.setText(getName(cursor));
		Integer subCatCount = getCount(cursor, CommonColumns.COUNT_CHILDREN_DIRECT);
		if (subCatCount != null) {
			holder.stats.setText(mContext.getString(R.string.label_category_subs, subCatCount));
		} else {
			holder.stats.setText(null);
		}

		Integer itemCountTotal = getCount(cursor, Category.COUNT_ITEM_ALL);
		if (itemCountTotal != null) {
			holder.items.setVisibility(View.VISIBLE);
			holder.items.setText(mContext.getString(R.string.label_category_items_view, itemCountTotal));
			holder.items.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					showItems(id);
				}
			});
		} else {
			holder.items.setVisibility(View.GONE);
		}
		setImage(cursor, holder.image);
	}

	private CharSequence getName(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(mContext, name);
	}

	private static Integer getCount(Cursor cursor, String columName) {
		int countIndex = cursor.getColumnIndex(columName);
		if (countIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0) {
				return count;
			}
		}
		return null;
	}

	private void setImage(Cursor cursor, ImageView target) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		int raw = AndroidTools.getRawResourceID(mContext, image);
		if (raw > 0) {
			App.pic().loadSVG(mContext, raw).into(target);
			return;
		}
		int drawable = AndroidTools.getDrawableResourceID(mContext, image);
		if (drawable > 0) {
			App.pic().loadDrawable(mContext, drawable).into(target);
			return;
		}
		App.pic().loadDrawable(mContext, R.drawable.category_unknown).into(target);
	}
	private void showItems(long categoryID) {
		activity.startActivity(CategoryItemsActivity.show(categoryID));
	}
}
