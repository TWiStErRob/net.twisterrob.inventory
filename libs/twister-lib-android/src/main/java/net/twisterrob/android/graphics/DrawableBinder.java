package net.twisterrob.android.graphics;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class DrawableBinder {
	public static Drawable bind(Drawable drawable, VBind vertical, HBind horizontal) {
		Rect rect = new Rect();
		vertical.set(rect, drawable.getIntrinsicHeight());
		horizontal.set(rect, drawable.getIntrinsicWidth());
		drawable.setBounds(rect);
		return drawable;
	}

	public static Drawable bindCenter(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = -w / 2;
		int right = +w / 2;
		int top = -h / 2;
		int bottom = +h / 2;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindBottomLeft(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = 0;
		int right = w;
		int top = -h;
		int bottom = 0;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindBottomRight(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = -w;
		int right = 0;
		int top = -h;
		int bottom = 0;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindTopLeft(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = 0;
		int right = w;
		int top = 0;
		int bottom = h;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindTopRight(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = -w;
		int right = 0;
		int top = 0;
		int bottom = h;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public enum VBind {
		TOP {
			@Override protected void set(Rect r, int h) {
				r.top = 0;
				r.bottom = h;
			}
		},
		CENTER {
			@Override protected void set(Rect r, int h) {
				r.top = -h / 2;
				r.bottom = +h / 2;
			}
		},
		BOTTOM {
			@Override protected void set(Rect r, int h) {
				r.top = -h;
				r.bottom = 0;
			}
		};
		protected abstract void set(Rect rect, int height);
	}

	public enum HBind {
		LEFT {
			@Override protected void set(Rect r, int w) {
				r.left = 0;
				r.right = w;
			}
		},
		CENTER {
			@Override protected void set(Rect r, int w) {
				r.left = -w / 2;
				r.right = +w / 2;
			}
		},
		RIGHT {
			@Override protected void set(Rect r, int w) {
				r.left = -w;
				r.right = 0;
			}
		};
		protected abstract void set(Rect rect, int width);
	}
}
