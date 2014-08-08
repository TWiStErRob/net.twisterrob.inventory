package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.view.TypeAdapter.ViewHolder;

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
		holder.title.setLayoutParams(updateMargin(cursor, holder.title.getLayoutParams()));

		App.pic().loadSVG(getImageResource(cursor)).into(holder.image);
	}
	private CharSequence getName(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(mContext, name);
	}

	private int getImageResource(Cursor cursor) {
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		return AndroidTools.getRawResourceID(mContext, image);
	}

	private LayoutParams updateMargin(Cursor cursor, LayoutParams layoutParams) {
		int level = getLevel(cursor);
		float margin = mContext.getResources().getDimension(R.dimen.margin) * (3 * level + 1);

		MarginLayoutParams marginParams = (MarginLayoutParams)layoutParams;
		marginParams.leftMargin = (int)margin; // TODO setStartMargin
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
