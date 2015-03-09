package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.*;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.*;

public class CategoryAndItemsAdapter extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {
	public interface CategoryItemEvents extends RecyclerViewItemEvents {
		void showItemsInCategory(long categoryID);
	}

	private final CategoryItemEvents listener;

	public CategoryAndItemsAdapter(CategoryItemEvents listener) {
		super(null);
		this.listener = listener;
	}

	private boolean isCategory(int position) {
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		return cursor.getInt(cursor.getColumnIndexOrThrow("variant")) == 1;
	}

	public LayoutManager createLayout(Context context) {
		final int columns = context.getResources().getInteger(R.integer.gallery_columns);
		GridLayoutManager layout = new GridLayoutManager(context, columns);
		layout.setSpanSizeLookup(new SpanSizeLookup() {
			@Override public int getSpanSize(int position) {
				return isCategory(position)? columns : 1;
			}
		});
		return layout;
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
					long id = (isCategory(getPosition())? -1 : 1) * getItemId();
					listener.onItemClick(getPosition(), id);
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override public boolean onLongClick(View v) {
					long id = (isCategory(getPosition())? -1 : 1) * getItemId();
					return listener.onItemLongClick(getPosition(), id);
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

		public void bind(Cursor cursor) {
			Context context = itemView.getContext();
			title.setText(getName(context, cursor));

			Integer subCatCount = getCount(cursor, CommonColumns.COUNT_CHILDREN_DIRECT);
			if (subCatCount != null) {
				stats.setText(context.getString(R.string.label_category_subs, subCatCount));
			} else {
				stats.setText(null);
			}

			Integer itemCountTotal = getCount(cursor, Category.COUNT_ITEM_ALL);
			if (itemCountTotal != null) {
				items.setVisibility(View.VISIBLE);
				items.setText(context.getString(R.string.label_category_items_view, itemCountTotal));
			} else {
				items.setVisibility(View.GONE);
			}

			String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
			int typeImageID = AndroidTools.getRawResourceID(context, typeImage);
			Pic.SVG_REQUEST.load(typeImageID).into(image);
		}
	}

	@Override public long getItemId(int position) {
		return (isCategory(position)? -1 : 1) * super.getItemId(position);
	}

	@Override public int getItemViewType(int position) {
		return isCategory(position)? R.layout.item_category : R.layout.item_gallery;
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType, parent, false);
		return viewType == R.layout.item_category? new ViewHolder(view) : new GalleryAdapter.ViewHolder(view, null);
	}

	@Override public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
		if (holder instanceof ViewHolder) {
			((ViewHolder)holder).bind(cursor);
		} else if (holder instanceof GalleryAdapter.ViewHolder) {
			((GalleryAdapter.ViewHolder)holder).bind(cursor);
		} else {
			throw new IllegalStateException("Unknown viewHolder: " + holder);
		}
	}

	private CharSequence getName(Context context, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		return AndroidTools.getText(context, name);
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
