package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Item;
import net.twisterrob.inventory.android.view.ItemAdapter.ViewHolder;

public class ItemAdapter extends ResourceCursorAdapterWithHolder<ViewHolder> {
	public ItemAdapter(Context context) {
		super(context, R.layout.item_list_item, null, false);
	}

	class ViewHolder {
		TextView text;
		ImageView image;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView)convertView.findViewById(R.id.itemName);
		holder.image = (ImageView)convertView.findViewById(R.id.itemImage);
		return holder;
	}

	@Override
	protected void bindView(ViewHolder holder, Cursor cursor, View convertView) {
		String name = cursor.getString(cursor.getColumnIndexOrThrow(Item.NAME));
		String id = cursor.getString(cursor.getColumnIndexOrThrow(Item.IMAGE));
		String category = cursor.getString(cursor.getColumnIndex(Item.CATEGORY_IMAGE));
		int categoryResource = mContext.getResources().getIdentifier(category, "drawable", mContext.getPackageName());

		holder.text.setText(name);
		//App.pic().getPicasso().cancelRequest(holder.image); // TODO check scrolling
		App.pic().loadDriveId(id).placeholder(categoryResource).error(categoryResource).into(holder.image);
	}
}
