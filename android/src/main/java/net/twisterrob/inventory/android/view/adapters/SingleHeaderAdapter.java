package net.twisterrob.inventory.android.view.adapters;

import android.annotation.SuppressLint;
import android.database.*;
import android.view.*;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.view.DeepScrollFixListener;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;

public abstract class SingleHeaderAdapter<VH extends ViewHolder> extends CursorRecyclerAdapter<ViewHolder> {
	private View header;

	public SingleHeaderAdapter(Cursor cursor) {
		super(cursor);
	}

	public void setHeader(View header) {
		this.header = header;
	}

	@SuppressWarnings("resource") // these cursor don't need to be closed
	@Override public Cursor swapCursor(Cursor newCursor) {
		if (header != null) { // add one extra row for header
			MatrixCursor header = new MatrixCursor(new String[] {"type", "_id"}, 1);
			header.addRow(new Object[] {"header", CommonColumns.ID_ADD});
			newCursor = new MergeCursor(new Cursor[] {header, newCursor});
		}
		return super.swapCursor(newCursor);
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
			return R.layout.item_header_placeholder;
		}
		return getNonHeaderViewType(position);
	}

	protected abstract VH onCreateNonHeaderViewHolder(ViewGroup parent, int viewType);
	@Override public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case R.layout.item_header_placeholder:
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(viewType, parent, false);
				return new HeaderViewHolder(view, header);
			default:
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
