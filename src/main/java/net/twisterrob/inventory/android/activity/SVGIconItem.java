package net.twisterrob.inventory.android.activity;

import android.content.*;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.view.IconedItem;

class SVGIconItem implements IconedItem {
	private final int titleResourceID;
	private final int svgResourceID;
	private final Intent intent;

	public SVGIconItem(@StringRes int titleResourceID, int svgResourceID, Intent intent) {
		this.titleResourceID = titleResourceID;
		this.svgResourceID = svgResourceID;
		this.intent = intent;
	}

	@Override public CharSequence getTitle(Context context) {
		return context.getText(titleResourceID);
	}

	@Override public void loadImage(ImageView icon) {
		icon.setVisibility(View.VISIBLE);
		ViewCompat.setLayerType(icon, ViewCompat.LAYER_TYPE_SOFTWARE, null);
		App.pic().startSVG(icon.getContext()).load(svgResourceID).into(icon);
	}

	@Override public Intent getIntent() {
		return intent;
	}
}
