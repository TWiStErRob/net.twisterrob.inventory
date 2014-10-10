package net.twisterrob.inventory.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.*;
import android.widget.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.view.TypeAdapter.ViewHolder;
import net.twisterrob.inventory.android.view.lib.ResourceCursorAdapterWithHolder;

public class TypeAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public TypeAdapter(Context context) {
		super(context, R.layout.type_spinner_item, null, false);
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
		holder.title.setLayoutParams(updateFormat(cursor, holder.title));

		App.pic().loadSVG(mContext, getImageResource(cursor)).into(holder.image);
	}
	private CharSequence getName(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(mContext, name);
	}

	private int getImageResource(Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		return AndroidTools.getRawResourceID(mContext, image);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private LayoutParams updateFormat(Cursor cursor, TextView title) {
		int level = getLevel(cursor);

		if (level == 1) {
			title.setTypeface(null, Typeface.BOLD);
		} else {
			title.setTypeface(null, Typeface.NORMAL);
		}

		int margin = (int)(mContext.getResources().getDimension(R.dimen.margin) * (3 * level + 1));
		MarginLayoutParams marginParams = (MarginLayoutParams)title.getLayoutParams();
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			marginParams.leftMargin = margin;
		} else {
			marginParams.setMarginStart(margin);
		}
		return marginParams;
	}

	private static int getLevel(Cursor cursor) {
		int levelColumn = cursor.getColumnIndex("level");
		int level = 0;
		if (levelColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			level = cursor.getInt(levelColumn);
		}
		return level;
	}
}
