package net.twisterrob.inventory.android.activity;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.ImageView;

import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.view.IconedItem;

abstract class SVGItem implements IconedItem {
	private final int titleResourceID;
	private final int svgResourceID;

	public SVGItem(@StringRes int titleResourceID, int svgResourceID) {
		this.titleResourceID = titleResourceID;
		this.svgResourceID = svgResourceID;
	}

	@Override public CharSequence getTitle(Context context) {
		return context.getText(titleResourceID);
	}

	@Override public void loadImage(ImageView icon) {
		Pic.SVG_REQUEST.load(svgResourceID).into(icon);
	}
}
