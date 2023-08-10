package net.twisterrob.inventory.android.view.adapters;

import android.annotation.SuppressLint;
import android.database.*;
import android.view.*;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.android.view.DeepScrollFixListener;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public abstract class SingleHeaderAdapter<VH extends ViewHolder> extends CursorRecyclerAdapter<ViewHolder> {
	private static final String DATABASE_TYPE_HEADER = "header";
	
	private View header;

	public SingleHeaderAdapter(Cursor cursor) {
		super(cursor);
	}

	public void setHeader(View header) {
		this.header = header;
		if (getCursor() == null) {
			// Ensure valid data as soon as this method returns.
			super.changeCursor(new MatrixCursor(new String[0], 0));
		}
	}

	@SuppressWarnings("resource") // These cursors (Matrix and Merge) will be closed by RecyclerViewLoadersController.close()
	@Override public @Nullable Cursor swapCursor(@Nullable Cursor newCursor) {
		if (newCursor == null) { // No new cursor, don't create MergeCursor.
			return super.swapCursor(null);
		} else if (header != null) { // Add one extra row for header.
			return super.swapCursor(new MergeCursor(new Cursor[] {createHeaderRow(), newCursor}));
		} else { // No header, just forward original.
			return super.swapCursor(newCursor);
		}
	}

	private static @NonNull Cursor createHeaderRow() {
		MatrixCursor header = new MatrixCursor(new String[] {"type", "_id"}, 1);
		header.addRow(new Object[] {DATABASE_TYPE_HEADER, CommonColumns.ID_ADD});
		return header;
	}

	public int getSpanSize(int position, int columns) {
		return 1;
	}

	private static class HeaderViewHolder extends ViewHolder {
		private final View header;

		HeaderViewHolder(View view, View header) {
			super(view);
			this.header = header;

			view.setOnTouchListener(new DeepScrollFixListener() {
				@SuppressLint("ClickableViewAccessibility") // not detecting touches, just forwarding
				@Override public boolean onTouch(View v, MotionEvent event) {
					super.onTouch(v, event);
					return HeaderViewHolder.this.header.dispatchTouchEvent(event);
				}
			});
		}
		void bind() {
			LayoutParams layout = itemView.getLayoutParams();
			layout.height = header.getHeight();
			itemView.setLayoutParams(layout);
		}
	}

	protected abstract int getNonHeaderViewType(int position);
	@Override public final int getItemViewType(int position) {
		if (position == 0 && header != null) {
			assert getItemId(position) == CommonColumns.ID_ADD;
			assert getCursor().moveToPosition(position)
					&& DATABASE_TYPE_HEADER.equals(DatabaseTools.getString(getCursor(), "type"));
			return R.layout.item_header_placeholder;
		}
		return getNonHeaderViewType(position);
	}

	protected abstract VH onCreateNonHeaderViewHolder(ViewGroup parent, int viewType);
	@Override public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == R.layout.item_header_placeholder) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			View view = inflater.inflate(viewType, parent, false);
			return new HeaderViewHolder(view, header);
		} else {
			return onCreateNonHeaderViewHolder(parent, viewType);
		}
	}

	protected abstract void onBindNonHeaderViewHolder(VH holder, Cursor cursor);
	@Override public final void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Cursor cursor) {
		if (holder instanceof HeaderViewHolder) {
			((HeaderViewHolder)holder).bind();
		} else {
			@SuppressWarnings("unchecked") VH nonHeaderHolder = (VH)holder;
			onBindNonHeaderViewHolder(nonHeaderHolder, cursor);
		}
	}
}
