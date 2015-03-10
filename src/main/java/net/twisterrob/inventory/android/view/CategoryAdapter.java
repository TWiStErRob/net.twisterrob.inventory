package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.CategoryAdapter.ViewHolder;

public class CategoryAdapter extends CursorRecyclerAdapter<ViewHolder> {
	public interface CategoryItemEvents extends RecyclerViewItemEvents {
		void showItemsInCategory(long categoryID);
	}

	private final CategoryItemEvents listener;

	public CategoryAdapter(CategoryItemEvents listener) {
		super(null);
		this.listener = listener;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View view) {
			super(view);
			image = (ImageView)view.findViewById(R.id.image);
			title = (TextView)view.findViewById(R.id.title);
			stats = (TextView)view.findViewById(R.id.stats);
			items = (Button)view.findViewById(R.id.items);

			view.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					listener.onItemClick(getPosition(), getItemId());
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override public boolean onLongClick(View v) {
					return listener.onItemLongClick(getPosition(), getItemId());
				}
			});
			items.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					listener.showItemsInCategory(getItemId());
				}
			});
		}

		ImageView image;
		TextView title;
		TextView stats;
		Button items;
	}

	@Override public int getItemViewType(int position) {
		return R.layout.item_category;
	}
	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		Context context = holder.itemView.getContext();
		holder.title.setText(getName(context, cursor));

		Integer subCatCount = getCount(cursor, CommonColumns.COUNT_CHILDREN_DIRECT);
		if (subCatCount != null) {
			holder.stats.setText(context.getString(R.string.label_category_subs, subCatCount));
		} else {
			holder.stats.setText(null);
		}

		Integer itemCountTotal = getCount(cursor, Category.COUNT_ITEM_ALL);
		if (itemCountTotal != null) {
			holder.items.setVisibility(View.VISIBLE);
			holder.items.setText(context.getString(R.string.label_category_items_view, itemCountTotal));
		} else {
			holder.items.setVisibility(View.GONE);
		}

		Pic.SVG_REQUEST.load(getTypeImage(cursor, context)).into(holder.image);
	}

	private CharSequence getName(Context context, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(context, name);
	}

	private int getTypeImage(Cursor cursor, Context context) {
		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
		return AndroidTools.getRawResourceID(context, typeImage);
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
