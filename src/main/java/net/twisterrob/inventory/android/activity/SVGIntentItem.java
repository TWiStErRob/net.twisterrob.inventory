package net.twisterrob.inventory.android.activity;

import android.content.*;
import android.support.annotation.StringRes;

public class SVGIntentItem extends SVGItem {
	private final Context context;
	private final Intent intent;

	public SVGIntentItem(@StringRes int titleResourceID, int svgResourceID, Context context, Intent intent) {
		super(titleResourceID, svgResourceID);
		this.context = context;
		this.intent = intent;
	}

	public Intent getIntent() {
		return intent;
	}

	@Override public void onClick() {
		context.startActivity(getIntent());
	}
}
