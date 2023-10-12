package net.twisterrob.inventory.android.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;

import net.twisterrob.android.adapter.ResourceCursorAdapterWithHolder;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter.ViewHolder;

import static net.twisterrob.inventory.android.Constants.*;

@SuppressWarnings("unused")
public class TypeAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {

	private final @NonNull Context context;

	private boolean expandable = false;
	private boolean indented = true;
	private boolean displaySource = DISABLE && BuildConfig.DEBUG;
	private boolean displayKeywords = false;

	public TypeAdapter(@NonNull Context context) {
		super(context, R.layout.item_type_spinner, null, 0);
		this.context = context;
	}

	public boolean isExpandable() {
		return expandable;
	}
	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}
	public boolean isIndented() {
		return indented;
	}
	public void setIndented(boolean indented) {
		this.indented = indented;
	}
	public boolean isDisplaySource() {
		return displaySource;
	}
	public void setDisplaySource(boolean displaySource) {
		this.displaySource = displaySource;
	}
	public boolean isDisplayKeywords() {
		return displayKeywords;
	}
	public void setDisplayKeywords(boolean displayKeywords) {
		this.displayKeywords = displayKeywords;
	}

	static class ViewHolder {
		ImageView image;
		View spacer;
		TextView state;
		TextView title;
		TextView count;
	}

	@Override protected @NonNull ViewHolder createHolder(@NonNull View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.image = convertView.findViewById(R.id.image);
		holder.spacer = convertView.findViewById(R.id.spacer);
		holder.state = convertView.findViewById(R.id.type);
		holder.title = convertView.findViewById(R.id.title);
		holder.count = convertView.findViewById(R.id.count);
		return holder;
	}

	@Override public boolean isEnabled(int position) {
		Cursor cursor = (Cursor)getItem(position);
		Boolean enabled = DatabaseTools.getOptionalBoolean(cursor, TypeSource.ENABLED);
		return enabled != null? enabled : super.isEnabled(position);
	}

	@Override protected void bindView(@NonNull ViewHolder holder, @NonNull Cursor cursor, @NonNull View convertView) {
		CharSequence title = getName(cursor);
		if (this.indented) {
			int level = DatabaseTools.getOptionalInt(cursor, TypeSource.LEVEL, 0);
			int indent = (int)(context.getResources().getDimension(R.dimen.icon_context) * level);
			ViewTools.updateWidth(holder.spacer, indent);
		}

		if (DatabaseTools.getOptionalBoolean(cursor, TypeSource.STRONG, false)) {
			holder.title.setTypeface(null, Typeface.BOLD);
		} else {
			holder.title.setTypeface(null, Typeface.NORMAL);
		}

		if (this.expandable) {
			holder.state.setVisibility(View.VISIBLE);
			boolean isOpen = isOpen(cursor);
			boolean hasChildren = 0 < DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_CHILDREN_DIRECT, 0);
			if (hasChildren) {
				if (isOpen) {
					holder.state.setText(R.string.types__prefix__opened);
				} else {
					holder.state.setText(R.string.types__prefix__closed);
					if (DatabaseTools.getOptionalBoolean(cursor, TypeSource.MIXED, false)) {
						title = TextTools.formatFormatted(context, R.string.types__format__more, title);
					}
				}
			} else {
				holder.state.setText(R.string.types__prefix__no_children);
			}
		}

		if (this.displayKeywords && (!indented || DatabaseTools.getOptionalInt(cursor, TypeSource.LEVEL, 0) != 0)) {
			String name = DatabaseTools.getString(cursor, CommonColumns.NAME);
			CharSequence keywords;
			try {
				keywords = ResourceTools.getText(context, ResourceNames.getKeywordsName(name));
			} catch (Exception ex) {
				keywords = null;
			}
			if (!TextUtils.isEmpty(keywords)) {
				title = TextTools.formatFormatted(context, R.string.types__format__keywords, title, keywords);
			}
		}

		if (this.displaySource) {
			CharSequence source = DatabaseTools.getOptionalString(cursor, TypeSource.SOURCE);
			if (!TextUtils.isEmpty(source)) {
				title = TextTools.formatFormatted(context, R.string.types__format__source, title, source);
			}
		}

		TextTools.replaceColors(context, title);
		holder.title.setText(title);
		int fallbackID = ImagedDTO.getFallbackID(context, cursor);
		Pic.svg().load(fallbackID).into(holder.image);

		int count = DatabaseTools.getOptionalInt(cursor, CommonColumns.COUNT_ITEM_ALL, 0);
		ViewTools.visibleIf(holder.count, count > 0);
		holder.count.setText(String.valueOf(count));
	}

	private boolean isOpen(Cursor cursor) {
		int position = cursor.getPosition();
		boolean mixed = DatabaseTools.getOptionalBoolean(cursor, TypeSource.MIXED, false);
		int level = DatabaseTools.getOptionalInt(cursor, TypeSource.LEVEL, 0);
		if (!mixed) {
			if (cursor.moveToNext()) {
				int nextLevel = DatabaseTools.getOptionalInt(cursor, TypeSource.LEVEL, 0);
				if (!cursor.moveToPosition(position)) {
					throw new IllegalStateException("Cannot restore cursor position");
				}
				return level < nextLevel; // the next item is deeper, so this current item must be open
			}
		} else {
			boolean foundTerminal = false;
			boolean foundGroup = false;
			while (cursor.moveToNext() && !(foundGroup && foundTerminal)) {
				int nextLevel = DatabaseTools.getOptionalInt(cursor, TypeSource.LEVEL, 0);
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
		String name = DatabaseTools.getString(cursor, CommonColumns.NAME);
		return ResourceTools.getText(context, name);
	}
}
