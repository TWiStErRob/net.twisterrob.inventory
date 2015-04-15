package net.twisterrob.inventory.android.view.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ImagedDTO;

public class BaseImagedAdapter<VH extends BaseImagedAdapter.ViewHolder> extends CursorRecyclerAdapter<VH> {
	private final int layoutResource;
	private final RecyclerViewItemEvents listener;

	public BaseImagedAdapter(Cursor cursor, RecyclerViewItemEvents listener) {
		this(cursor, AndroidTools.INVALID_RESOURCE_ID, listener);
	}
	public BaseImagedAdapter(Cursor cursor, int layoutResource, RecyclerViewItemEvents listener) {
		super(cursor);
		this.layoutResource = layoutResource;
		this.listener = listener;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View view) {
			super(view);
			image = (ImageView)view.findViewById(R.id.image);
			title = (TextView)view.findViewById(R.id.title);

			view.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					listener.onItemClick(getAdapterPosition(), getItemId());
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override public boolean onLongClick(View v) {
					listener.onItemLongClick(getAdapterPosition(), getItemId());
					return true;
				}
			});
		}

		public final ImageView image;
		public final TextView title;
	}

	@SuppressWarnings("unchecked")
	@Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		return (VH)new ViewHolder(inflateView(parent, layoutResource));
	}
	protected View inflateView(ViewGroup parent, int layoutResource) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return inflater.inflate(layoutResource, parent, false);
	}

	@Override public void onBindViewHolder(VH holder, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		String image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));

		holder.title.setText(name);
		ImagedDTO.loadInto(holder.image, image, typeImage);
	}
}