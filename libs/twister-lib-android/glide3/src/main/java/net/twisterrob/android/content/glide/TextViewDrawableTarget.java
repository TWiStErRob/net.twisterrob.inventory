package net.twisterrob.android.content.glide;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.*;

import static net.twisterrob.android.content.glide.TextViewDrawableTarget.Location.*;

public class TextViewDrawableTarget<Z extends Drawable>
		extends ViewTarget<TextView, Z>
		implements GlideAnimation.ViewAdapter {
	private final Location location;
	private final int width;
	private final int height;

	public enum Location {
		LEFT,
		TOP,
		RIGHT,
		BOTTOM
	}

	public TextViewDrawableTarget(TextView view, Location location, int width, int height) {
		super(view);
		this.location = location;
		this.width = width;
		this.height = height;
	}

	@Override public Drawable getCurrentDrawable() {
		return view.getCompoundDrawables()[location.ordinal()];
	}

	@Override public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
		if (glideAnimation == null || !glideAnimation.animate(resource, this)) {
			set(resource);
		}
	}

	@Override public void setDrawable(Drawable drawable) {
		set(drawable);
	}
	@Override public void onLoadCleared(Drawable placeholder) {
		set(placeholder);
	}
	@Override public void onLoadStarted(Drawable placeholder) {
		set(placeholder);
	}
	@Override public void onLoadFailed(Exception e, Drawable error) {
		set(error);
	}

	private void set(Drawable drawable) {
		drawable.setBounds(0, 0, width, height);
		view.setCompoundDrawables(f(drawable, LEFT), f(drawable, TOP), f(drawable, RIGHT), f(drawable, BOTTOM));
	}

	private Drawable f(Drawable drawable, Location location) {
		return location == this.location? drawable : null;
	}

	@Override public void getSize(final SizeReadyCallback cb) {
		if (0 <= width && 0 <= height) {
			cb.onSizeReady(width, height);
		} else {
			super.getSize(new SizeReadyCallback() {
				@Override public void onSizeReady(int width, int height) {
					switch (location) {
						case LEFT:
						case RIGHT:
							cb.onSizeReady(Target.SIZE_ORIGINAL, height);
							break;
						case TOP:
						case BOTTOM:
							cb.onSizeReady(width, Target.SIZE_ORIGINAL);
							break;
					}
				}
			});
		}
	}
}
