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
		ImageView type;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView)convertView.findViewById(R.id.roomName);
		holder.image = (ImageView)convertView.findViewById(R.id.roomImage);
		holder.type = (ImageView)convertView.findViewById(R.id.roomType);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(Room.NAME));
		String imageDriveId = cursor.getString(cursor.getColumnIndexOrThrow(Room.IMAGE));
		String type = cursor.getString(cursor.getColumnIndex(Room.TYPE_IMAGE));
		int typeResource = mContext.getResources().getIdentifier(type, "drawable", mContext.getPackageName());

		holder.text.setText(name);
		holder.type.setImageResource(typeResource);
		//App.pic().getPicasso().cancelRequest(holder.image); // TODO check scrolling
		App.pic().loadDriveId(imageDriveId).placeholder(typeResource).error(typeResource).into(holder.image);
	}
}
