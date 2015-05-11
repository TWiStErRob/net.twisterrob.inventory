package net.twisterrob.inventory.android.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;

public class CategoryViewHolder extends RecyclerView.ViewHolder {
	public interface CategoryItemEvents extends RecyclerViewItemEvents {
		void showItemsInCategory(long categoryID);
	}

	private final CategoryItemEvents listener;

	private ImageView image;
	private TextView title;
	private TextView stats;
	private TextView count;

	public CategoryViewHolder(View view, CategoryItemEvents events) {
		super(view);
		this.listener = events;
		image = (ImageView)view.findViewById(R.id.image);
		title = (TextView)view.findViewById(R.id.title);
		stats = (TextView)view.findViewById(R.id.stats);
		count = (TextView)view.findViewById(R.id.count);

		view.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				listener.onItemClick(getAdapterPosition(), getItemId());
			}
		});
		view.setOnLongClickListener(new OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				return listener.onItemLongClick(getAdapterPosition(), getItemId());
			}
		});
		count.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				listener.showItemsInCategory(getItemId());
			}
		});
	}

	public void bind(Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));

		Context context = itemView.getContext();
		title.setText(AndroidTools.getText(context, name));

		Integer subCatCount = getCount(cursor, CommonColumns.COUNT_CHILDREN_DIRECT);
		if (subCatCount != null) {
			stats.setText(context.getResources().getQuantityString(
					R.plurals.label_category_subs, subCatCount, subCatCount));
		} else {
			if (App.getBPref(R.string.pref_preferExpandedKeywords, R.bool.pref_preferExpandedKeywords_default)) {
				stats.setText(CategoryDTO.getKeywords(context, name));
			} else {
				stats.setText(CategoryDTO.getShortKeywords(context, name));
			}
		}

		Integer itemCountTotal = getCount(cursor, Category.COUNT_ITEM_ALL);
		if (itemCountTotal != null) {
			count.setText(String.valueOf(itemCountTotal));
		}
		AndroidTools.displayedIfHasText(count);

		Pic.svg().load(AndroidTools.getRawResourceID(context, typeImage)).into(image);
	}

	private static Integer getCount(Cursor cursor, String columnName) {
		int countIndex = cursor.getColumnIndex(columnName);
		if (countIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0) {
				return count;
			}
		}
		return null;
	}
}
