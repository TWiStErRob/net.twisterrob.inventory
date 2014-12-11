package net.twisterrob.inventory.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.*;
import android.view.View;
import android.widget.*;

public interface IconedItem {
	CharSequence getTitle(Context context);
	void loadImage(ImageView icon);
	void onClick();

	public static class OnClickCallback implements AdapterView.OnItemClickListener {
		@Override public void onItemClick(AdapterView parent, View view, int position, long id) {
			check(parent, position);
			IconedItem item = (IconedItem)parent.getItemAtPosition(position);
			item.onClick();
		}

		@TargetApi(VERSION_CODES.HONEYCOMB)
		private void check(AdapterView parent, int position) {
			if (parent instanceof ListView) {
				((ListView)parent).setItemChecked(position, true);
			} else if (parent instanceof AbsListView) {
				if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
					((AbsListView)parent).setItemChecked(position, true);
				}
			}
		}
	}
}
