package net.twisterrob.inventory.android.view.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.*;

import net.twisterrob.android.adapter.ResourceCursorAdapterWithHolder;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter.ViewHolder;

public class TypeAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public TypeAdapter(Context context) {
		super(context, R.layout.item_type_spinner, null, false);
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
		int margin = updateFormat(cursor, holder.title);
		updateLeftStartMargin(holder.title, margin);

		int fallbackID = ImagedDTO.getFallbackID(mContext, cursor);
		Pic.SVG_REQUEST.load(fallbackID).into(holder.image);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void updateLeftStartMargin(View view, int margin) {
		MarginLayoutParams marginParams = (MarginLayoutParams)view.getLayoutParams();
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			marginParams.leftMargin = margin;
		} else {
			marginParams.setMarginStart(margin);
		}
	}
	private CharSequence getName(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(mContext, name);
	}

	private int updateFormat(Cursor cursor, TextView title) {
		int level = getLevel(cursor);

		if (level == 0) {
			title.setTypeface(null, Typeface.BOLD);
		} else {
			title.setTypeface(null, Typeface.NORMAL);
		}

		return (int)(mContext.getResources().getDimension(R.dimen.margin) * (4 * level));
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
