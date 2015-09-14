package net.twisterrob.inventory.android.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import net.twisterrob.android.adapter.ResourceCursorAdapterWithHolder;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter.ViewHolder;

public class TypeAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public TypeAdapter(Context context) {
		super(context, R.layout.item_type_spinner, null, false);
	}

	class ViewHolder {
		ImageView image;
		View spacer;
		TextView state;
		TextView title;
	}

	@Override protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.image = (ImageView)convertView.findViewById(R.id.image);
		holder.spacer = convertView.findViewById(R.id.spacer);
		holder.state = (TextView)convertView.findViewById(R.id.type);
		holder.title = (TextView)convertView.findViewById(R.id.title);
		return holder;
	}

	@Override public boolean isEnabled(int position) {
		Cursor cursor = (Cursor)getItem(position);
		Boolean enabled = DatabaseTools.getOptionalBoolean(cursor, "enabled");
		return enabled != null? enabled : super.isEnabled(position);
	}

	@Override protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		holder.title.setText(getName(cursor));
		int level = DatabaseTools.getOptionalInt(cursor, "level", 0);
		int indent = (int)(mContext.getResources().getDimension(R.dimen.marginBig) * level);
		AndroidTools.updateWidth(holder.spacer, indent);

		if (DatabaseTools.getOptionalBoolean(cursor, "strong", false)) {
			holder.title.setTypeface(null, Typeface.BOLD);
		} else {
			holder.title.setTypeface(null, Typeface.NORMAL);
		}

		boolean isOpen = isOpen(cursor);
		boolean hasChildren = 0 < DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_CHILDREN_DIRECT, 0);
		if (hasChildren) {
			if (isOpen) {
				holder.state.setText("-");
			} else {
				holder.state.setText("+");
				if (DatabaseTools.getOptionalBoolean(cursor, "mixed", false)) {
					holder.title.setText(TextUtils.concat(holder.title.getText(), " (more)"));
				}
			}
		} else {
			holder.state.setText(" ");
		}

		if (BuildConfig.DEBUG) {
			String source = DatabaseTools.getOptionalString(cursor, "source");
			if (source != null) {
				holder.title.setText(TextUtils.concat(holder.title.getText(), " - ", source));
			}
		}

		int fallbackID = ImagedDTO.getFallbackID(mContext, cursor);
		Pic.svg().load(fallbackID).into(holder.image);
	}
	private boolean isOpen(Cursor cursor) {
		int position = cursor.getPosition();
		boolean mixed = DatabaseTools.getOptionalBoolean(cursor, "mixed", false);
		int level = DatabaseTools.getOptionalInt(cursor, "level", 0);
		if (!mixed) {
			if (cursor.moveToNext()) {
				int nextLevel = DatabaseTools.getOptionalInt(cursor, "level", 0);
				boolean open = level < nextLevel;
				if (!cursor.moveToPosition(position)) {
					throw new IllegalStateException("Cannot restore cursor position");
				}
				return open;
			}
		} else {
			boolean foundTerminal = false;
			boolean foundGroup = false;
			while (cursor.moveToNext() && !(foundGroup && foundTerminal)) {
				int nextLevel = DatabaseTools.getOptionalInt(cursor, "level", 0);
				if (nextLevel <= level) {
					break;
				}
				int children = DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_CHILDREN_DIRECT, 0);
				if (level + 1 == nextLevel && children > 0) {
					foundGroup = true;
				}
				if (level + 1 == nextLevel && children == 0) {
					foundTerminal = true;
				}
			}
			if (!cursor.moveToPosition(position)) {
				throw new IllegalStateException("Cannot restore cursor position");
			}
			return foundGroup && foundTerminal;
		}
		if (cursor.isAfterLast()) {
			if (!cursor.moveToPosition(position)) {
				throw new IllegalStateException("Cannot restore cursor position");
			}
		}
		return false;
	}

	private CharSequence getName(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(mContext, name);
	}
}
