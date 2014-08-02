package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Room;
import net.twisterrob.inventory.android.view.RoomAdapter.ViewHolder;

public class RoomAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public RoomAdapter(Context context) {
		super(context, R.layout.room_list_item, null, false);
	}

	class ViewHolder {
		TextView text;
		ImageView image;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView)convertView.findViewById(R.id.roomName);
		holder.image = (ImageView)convertView.findViewById(R.id.roomImage);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		holder.text.setText(cursor.getString(cursor.getColumnIndexOrThrow(Room.NAME)));
		String id = cursor.getString(cursor.getColumnIndexOrThrow(Room.IMAGE));
		//App.pic().getPicasso().cancelRequest(holder.image); // TODO check scrolling
		App.pic().loadDriveId(id) //
				.placeholder(R.drawable.room_bedroom) //
				.error(R.drawable.room_bedroom) //
				.into(holder.image);
	}
}
