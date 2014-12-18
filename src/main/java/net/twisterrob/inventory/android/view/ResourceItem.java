package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.support.annotation.*;
import android.widget.ImageView;

public abstract class ResourceItem implements IconedItem {
	private final int title;
	private final int icon;
	public ResourceItem(@StringRes int title, @DrawableRes int icon) {
		this.title = title;
		this.icon = icon;
	}

	@Override public CharSequence getTitle(Context context) {
		return context.getString(this.title);
	}
	@Override public void loadImage(ImageView icon) {
		icon.setImageResource(this.icon);
	}
}
