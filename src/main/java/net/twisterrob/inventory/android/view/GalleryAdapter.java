package net.twisterrob.inventory.android.view;

import android.database.*;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;
import net.twisterrob.inventory.android.view.GalleryAdapter.GalleryViewHolder;

public class GalleryAdapter extends CursorRecyclerAdapter<GalleryViewHolder> {
	public interface GalleryItemEvents extends RecyclerViewItemEvents {
	}

	private final GalleryItemEvents listener;
	private View header;

	public GalleryAdapter(Cursor cursor, GalleryItemEvents listener) {
		super(cursor);
		this.listener = listener;
	}

	public void setHeader(View header) {
		this.header = header;
	}

	@Override public Cursor swapCursor(Cursor newCursor) {
		if (header != null) { // add one extra row for header
			MatrixCursor header = new MatrixCursor(new String[] {"_id"}, 1);
			header.addRow(new Object[] {CommonColumns.ID_ADD});
			newCursor = new MergeCursor(new Cursor[] {header, newCursor});
		}
		return super.swapCursor(newCursor);
	}

	public static abstract class GalleryViewHolder extends RecyclerView.ViewHolder {
		GalleryViewHolder(View view) {
			super(view);
		}
		abstract void bind(Cursor cursor);
	}

	private static class HeaderViewHolder extends GalleryViewHolder {
		private View header;

		HeaderViewHolder(View view, View header) {
			super(view);
			this.header = header;

			view.setOnTouchListener(new OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent event) {
					v.getParent().requestDisallowInterceptTouchEvent(true);
					return HeaderViewHolder.this.header.dispatchTouchEvent(event);
				}
			});
		}
		@Override void bind(Cursor cursor) {
			LayoutParams layout = itemView.getLayoutParams();
			layout.height = header.getHeight();
			itemView.setLayoutParams(layout);
		}
	}

	private static class ViewHolder extends GalleryViewHolder {
		ViewHolder(View view, final GalleryItemEvents listener) {
			super(view);
			title = (TextView)view.findViewById(R.id.title);
			image = (ImageView)view.findViewById(R.id.image);
			type = (ImageView)view.findViewById(R.id.type);
			count = (TextView)view.findViewById(R.id.count);

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
		}

		TextView title;
		ImageView image;
		ImageView type;
		TextView count;

		public void bind(Cursor cursor) {
			title.setText(getName(cursor));
			String countText = getCountText(cursor);
			count.setText(countText);
			count.setVisibility(countText != null? View.VISIBLE : View.GONE);

			String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
			String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
			if (getItemViewType() == R.layout.item_gallery) {
				ImagedDTO.loadInto(image, type, imagePath, typeImage, false);
			} else {
				ImagedDTO.loadInto(image, image, imagePath, typeImage, false);
			}
		}
	}

	@Override public int getItemViewType(int position) {
		if (position == 0 && header != null) {
			return R.layout.item_header_placeholder;
		}
		return isGroup(position)? R.layout.item_gallery_group : R.layout.item_gallery;
	}
	@Override public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType, parent, false);
		switch (viewType) {
			case R.layout.item_header_placeholder:
				return new HeaderViewHolder(view, header);
			case R.layout.item_gallery:
			case R.layout.item_gallery_group:
				return new ViewHolder(view, listener);
			default:
				throw new IllegalArgumentException("Unhandled viewType: " + viewType);
		}
	}

	@Override public void onBindViewHolder(GalleryViewHolder holder, Cursor cursor) {
		holder.bind(cursor);
	}

	public boolean isGroup(int position) {
		Cursor c = getCursor();
		c.moveToPosition(position);
		return isGroup(c);
	}

	private static boolean isGroup(Cursor cursor) {
		int groupIndex = cursor.getColumnIndex("group"); // boolean
		return groupIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN && cursor.getInt(groupIndex) == 1;
	}

	private static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
	}

	private static String getCountText(Cursor cursor) {
		String countText = null;
		int countIndex = cursor.getColumnIndex(CommonColumns.COUNT_CHILDREN_DIRECT);
		if (countIndex != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			int count = cursor.getInt(countIndex);
			if (count > 0 || isGroup(cursor)) {
				countText = String.valueOf(count);
			}
		}
		return countText;
	}
}
