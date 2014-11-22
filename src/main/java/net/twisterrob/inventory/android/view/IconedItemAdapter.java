package net.twisterrob.inventory.android.view;

import java.util.Collection;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.*;

import net.twisterrob.android.adapter.BaseListAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.view.IconedItemAdapter.ViewHolder;

public class IconedItemAdapter extends BaseListAdapter<IconedItem, ViewHolder> {
	private final int layout;

	public IconedItemAdapter(Context context, @LayoutRes int layoutId, Collection<IconedItem> items) {
		super(context, items);
		layout = layoutId;
	}

	protected static class ViewHolder {
		ImageView icon;
		TextView label;

		ViewHolder(View view) {
			icon = (ImageView)view.findViewById(R.id.icon);
			label = (TextView)view.findViewById(R.id.title);
		}
	}

	@Override
	protected int getItemLayoutId() {
		return layout;
	}

	@Override
	protected ViewHolder createHolder(View convertView) {
		return new ViewHolder(convertView);
	}

	@Override
	protected void bindView(ViewHolder holder, IconedItem currentItem, View convertView) {
		CharSequence title = currentItem.getTitle(convertView.getContext());
		holder.label.setText(title);
		currentItem.loadImage(holder.icon);
	}
}
