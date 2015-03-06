package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.view.View;
import android.widget.*;

public interface IconedItem {
	CharSequence getTitle(Context context);
	void loadImage(ImageView icon);
	void onClick();

	class OnClick implements AdapterView.OnItemClickListener {
		@Override public void onItemClick(AdapterView parent, View view, int position, long id) {
			IconedItem item = (IconedItem)parent.getItemAtPosition(position);
			item.onClick();
		}
	}
}
