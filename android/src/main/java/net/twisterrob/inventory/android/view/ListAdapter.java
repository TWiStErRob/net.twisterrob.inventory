package net.twisterrob.inventory.android.view;

import java.text.NumberFormat;

import android.database.Cursor;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.view.ListAdapter.ViewHolder;

public class ListAdapter extends CursorRecyclerAdapter<ViewHolder> {
	public interface ListItemEvents {
		void removeFromList(long listID);
		void addToList(long listID);
	}

	private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();
	private final ListItemEvents listener;

	public ListAdapter(Cursor cursor, ListItemEvents listener) {
		super(cursor);
		this.listener = listener;
	}

	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.item_list, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Cursor cursor) {
		holder.bind(cursor);
	}

	class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		private final TextView title;
		private final TextView count;

		public ViewHolder(View view) {
			super(view);

			title = view.findViewById(R.id.title);
			count = view.findViewById(R.id.count);

			view.setOnClickListener(this);
		}

		@Override public void onClick(View v) {
			itemView.setEnabled(false);
			Cursor cursor = getCursor();
			if (cursor.moveToPosition(getAdapterPosition())) {
				if (cursor.getShort(cursor.getColumnIndexOrThrow("exists")) != 0) {
					listener.removeFromList(getItemId());
				} else {
					listener.addToList(getItemId());
				}
			}
		}

		private void bind(Cursor cursor) {
			String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
			int childCount = cursor.getInt(cursor.getColumnIndexOrThrow(CommonColumns.COUNT_CHILDREN_DIRECT));
			boolean exists = cursor.getShort(cursor.getColumnIndexOrThrow("exists")) != 0;

			title.setText(name);
			count.setText(NUMBER.format(childCount));
			itemView.setSelected(exists);
			itemView.setEnabled(true);
		}
	}
}
