package net.twisterrob.android.content.glide;

import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.util.*;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.*;

import net.twisterrob.android.utils.tools.ResourceTools;

public class ActionBarIconTarget extends BaseTarget<GlideDrawable> {
	private final ActionBar actionBar;

	public ActionBarIconTarget(ActionBar actionBar) {
		this.actionBar = actionBar;
	}

	@Override public void onLoadStarted(Drawable placeholder) {
		actionBar.setIcon(placeholder);
	}
	@Override public void onResourceReady(GlideDrawable resource,
			GlideAnimation<? super GlideDrawable> glideAnimation) {
		actionBar.setIcon(resource);
	}
	@Override public void onLoadCleared(Drawable placeholder) {
		actionBar.setIcon(placeholder);
	}
	@Override public void onLoadFailed(Exception e, Drawable errorDrawable) {
		actionBar.setIcon(errorDrawable);
	}

	@Override public void getSize(SizeReadyCallback cb) {
		int size;
		TypedValue tv = new TypedValue();
		if (actionBar.getThemedContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			DisplayMetrics metrics = actionBar.getThemedContext().getResources().getDisplayMetrics();
			size = TypedValue.complexToDimensionPixelSize(tv.data, metrics);
		} else {
			size = ResourceTools.dipInt(actionBar.getThemedContext(), 48); // standard size
		}
		cb.onSizeReady(size, size);
	}
}
