package net.twisterrob.inventory.android.view;

import android.content.*;
import android.widget.ImageView;

public interface IconedItem {
	CharSequence getTitle(Context context);
	void loadImage(ImageView icon);
	Intent getIntent();
}
