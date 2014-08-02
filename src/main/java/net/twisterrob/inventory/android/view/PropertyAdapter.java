package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.view.PropertyAdapter.ViewHolder;

public class PropertyAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public PropertyAdapter(Context context) {
		super(context, R.layout.property_list_item, null, false);
	}

	class ViewHolder {
		TextView text;
		ImageView image;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView)convertView.findViewById(R.id.propertyName);
		holder.image = (ImageView)convertView.findViewById(R.id.propertyImage);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(Property.NAME));
		String imageDriveId = cursor.getString(cursor.getColumnIndexOrThrow(Property.IMAGE));
		String type = cursor.getString(cursor.getColumnIndex(Property.TYPE_IMAGE));
		int typeResource = mContext.getResources().getIdentifier(type, "drawable", mContext.getPackageName());

		holder.text.setText(name);
		//App.pic().getPicasso().cancelRequest(holder.image); // TODO check scrolling
		App.pic().loadDriveId(imageDriveId).placeholder(typeResource).error(typeResource).into(holder.image);
	}
}
