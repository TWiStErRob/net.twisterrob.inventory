package net.twisterrob.inventory.android.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.*;
import android.widget.*;

import com.bumptech.glide.Glide;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;

public class CategoryViewHolder extends RecyclerView.ViewHolder {
	public interface CategoryItemEvents extends RecyclerViewItemEvents {
		void showItemsInCategory(long categoryID);
	}

	private final CategoryItemEvents listener;

	private final ImageView image;
	private final TextView title;
	private final TextView stats;
	private final TextView count;

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
		Integer subCatCount = getCount(cursor, CommonColumns.COUNT_CHILDREN_DIRECT);

		Context context = itemView.getContext();

		CharSequence titleText = ResourceTools.getText(context, name);
		if (subCatCount != null) {
			String subCats = context.getResources().getQuantityString(
					R.plurals.label_category_subs, subCatCount, subCatCount);
			CharSequence text = context.getResources().getText(R.string.label_category_subs_with_title);
			title.setText(TextUtils.expandTemplate(text, titleText, subCats));
		} else {
			title.setText(titleText);
		}

		CharSequence description = CategoryDTO.getDescription(context, name);
		if (description != null && description.length() != 0) {
			stats.setText(description);
		} else {
			stats.setText(CategoryDTO.getKeywordsExtended(context, name));
		}
		ViewTools.displayedIfHasText(stats);

		Integer itemCountTotal = getCount(cursor, Category.COUNT_ITEM_ALL);
		if (itemCountTotal != null) {
			count.setText(String.valueOf(itemCountTotal));
		} else {
			count.setText(null);
		}
		ViewTools.displayedIfHasText(count);

		Pic.svg().load(ResourceTools.getRawResourceID(context, typeImage)).into(image);
	}

	public void unBind() {
		// FIXME replace this with proper Glide.with calls
		Glide.clear(image);
	}

	private static Integer getCount(Cursor cursor, String columnName) {
		Integer result = DatabaseTools.getOptionalInt(cursor, columnName);
		if (result != null && result == 0) {
			result = null;
		}
		return result;
	}
}
