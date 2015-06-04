package net.twisterrob.inventory.android.view.adapters;

import java.util.Collection;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.*;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.*;

import net.twisterrob.android.adapter.BaseListAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.view.IconedItem;
import net.twisterrob.inventory.android.view.adapters.IconedItemAdapter.ViewHolder;

public class IconedItemAdapter extends BaseListAdapter<IconedItem, ViewHolder> {
	private final int layout;
	private IconedItem active;

	public IconedItemAdapter(Context context, @LayoutRes int layoutId, Collection<IconedItem> items) {
		super(context, items);
		layout = layoutId;
	}

	protected static class ViewHolder {
		final View view;
		ImageView icon;
		TextView label;

		ViewHolder(View view) {
			this.view = view;
			icon = (ImageView)view.findViewById(R.id.image);
			label = (TextView)view.findViewById(R.id.title);
		}
	}

	@Override protected int getItemLayoutId() {
		return layout;
	}

	@Override protected ViewHolder createHolder(View convertView) {
		return new ViewHolder(convertView);
	}

	@Override protected void bindView(ViewHolder holder, IconedItem currentItem, View convertView) {
		setActive(holder.view, currentItem != active);
		CharSequence title = currentItem.getTitle(convertView.getContext());
		holder.label.setText(title);
		holder.icon.setVisibility(View.VISIBLE);
		currentItem.loadImage(holder.icon);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void setActive(View view, boolean enabled) {
		view.setEnabled(enabled);
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			view.setActivated(!enabled);
		}
	}

	public void setActive(int active) {
		try {
			this.active = getItem(active);
		} catch (IndexOutOfBoundsException ex) {
			this.active = null;
		}
	}

	@Override public boolean areAllItemsEnabled() {
		return false;
	}
	@Override public boolean isEnabled(int position) {
		return getItem(position) != active;
	}
}
