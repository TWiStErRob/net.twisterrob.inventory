package net.twisterrob.inventory.android.activity;

import android.content.Context;
import android.support.annotation.*;
import android.widget.ImageView;

import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.view.IconedItem;

abstract class SVGItem implements IconedItem {
	private final @StringRes int titleResourceID;
	private final @RawRes int svgResourceID;

	public SVGItem(@StringRes int titleResourceID, @RawRes int svgResourceID) {
		this.titleResourceID = titleResourceID;
		this.svgResourceID = svgResourceID;
	}

	@Override public CharSequence getTitle(Context context) {
		return context.getText(titleResourceID);
	}

	@Override public void loadImage(ImageView icon) {
		Pic.svg().load(svgResourceID).into(icon);
	}
}
