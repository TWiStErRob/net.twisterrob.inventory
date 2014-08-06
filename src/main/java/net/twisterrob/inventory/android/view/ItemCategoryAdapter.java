package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.view.ItemCategoryAdapter.ViewHolder;

public class ItemCategoryAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public ItemCategoryAdapter(Context context) {
		super(context, R.layout.category_spinner_item, null, false);
	}

	class ViewHolder {
		ImageView image;
		TextView title;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.image = (ImageView)convertView.findViewById(R.id.image);
		holder.title = (TextView)convertView.findViewById(R.id.title);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		holder.title.setText(getName(cursor));
		holder.title.setLayoutParams(updateMargin(cursor, holder.title.getLayoutParams()));
		holder.image.setImageDrawable(getImageResource(cursor));
	}

	private LayoutParams updateMargin(Cursor cursor, LayoutParams layoutParams) {
		int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
		float margin = mContext.getResources().getDimension(R.dimen.margin) * 3 * level;

		MarginLayoutParams marginParams = (MarginLayoutParams)layoutParams;
		marginParams.leftMargin = (int)margin; // TODO setStartMargin
		return marginParams;
	}

	private CharSequence getName(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(mContext, name);
	}

	private Drawable getImageResource(Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		return ImagedDTO.getFallbackDrawable(mContext, image);
	}
}
