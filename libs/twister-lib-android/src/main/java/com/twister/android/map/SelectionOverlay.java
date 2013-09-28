package com.twister.android.map;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.*;

public class SelectionOverlay extends BaseItemizedOverlay<SelectionOverlay.SelectionOverlayItem> {
	private static int s_defaultDrawableId;
	private final GeoPoint m_loc;

	public SelectionOverlay(Context context, GeoPoint loc) {
		super(getIcon(context));
		if (loc == null) {
			throw new IllegalArgumentException("Location must not be null, nothing to display");
		}
		m_loc = loc;
		setShadow(false);
		populate();
	}

	protected static Drawable getIcon(Context context) {
		Drawable drawable = context.getResources().getDrawable(s_defaultDrawableId);
		return bindCenter(drawable);
	}

	public static void setDefaultDrawable(int defaultIcon) {
		s_defaultDrawableId = defaultIcon;
	}

	@Override
	protected SelectionOverlayItem createItem(int i) {
		return new SelectionOverlayItem(m_loc);
	}

	@Override
	public int size() {
		return 1;
	}

	static class SelectionOverlayItem extends OverlayItem {
		public SelectionOverlayItem(GeoPoint loc) {
			super(loc, "Selected location", null);
		}
	}
}
