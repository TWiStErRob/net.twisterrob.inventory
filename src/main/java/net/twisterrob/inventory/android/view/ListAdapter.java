package net.twisterrob.inventory.android.view;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.view.ListAdapter.ViewHolder;

public class ListAdapter extends CursorRecyclerAdapter<ViewHolder> {
	public interface ListItemEvents {
		void removeFromList(RecyclerView.ViewHolder holder);
		void addToList(RecyclerView.ViewHolder holder);
	}

	private final ListItemEvents listener;

	public ListAdapter(Cursor cursor, ListItemEvents listener) {
		super(cursor);
		this.listener = listener;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View view) {
			super(view);

			title = (TextView)view.findViewById(R.id.title);
			count = (TextView)view.findViewById(R.id.count);

			view.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					Cursor cursor = getCursor();
					cursor.moveToPosition(getPosition());
					boolean exists = cursor.getShort(cursor.getColumnIndexOrThrow("exists")) != 0;
					if (exists) {
						listener.removeFromList(ViewHolder.this);
					} else {
						listener.addToList(ViewHolder.this);
					}
				}
			});
		}

		TextView title;
		TextView count;
	}

	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.item_list, parent, false);
		return new ViewHolder(view);
	}

	@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		int count = cursor.getInt(cursor.getColumnIndexOrThrow(CommonColumns.COUNT_CHILDREN_DIRECT));
		boolean exists = cursor.getShort(cursor.getColumnIndexOrThrow("exists")) != 0;

		holder.title.setText(name);
		holder.count.setText(String.valueOf(count));
		holder.itemView.setSelected(exists);
	}
}
