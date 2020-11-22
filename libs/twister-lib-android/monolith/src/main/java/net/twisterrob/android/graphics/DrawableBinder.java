package net.twisterrob.android.graphics;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class DrawableBinder {
	public static Drawable bind(Drawable drawable,
			VBind vertical, HBind horizontal) {
		return bind(drawable, vertical, drawable.getIntrinsicHeight(), horizontal, drawable.getIntrinsicWidth());
	}
	public static Drawable bind(Drawable drawable,
			VBind vertical, int height, HBind horizontal, int width) {
		Rect rect = new Rect();
		vertical.set(rect, height);
		horizontal.set(rect, width);
		drawable.setBounds(rect);
		return drawable;
	}

	public static @NonNull Drawable bindCenter(@NonNull Drawable drawable) {
		return bindCenter(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	}
	public static @NonNull Drawable bindCenter(@NonNull Drawable drawable, int w, int h) {
		drawable.setBounds(-w / 2 /*left*/, -h / 2 /*top*/, +w / 2 /*right*/, +h / 2 /*bottom*/);
		return drawable;
	}

	public static @NonNull Drawable bindBottomLeft(@NonNull Drawable drawable) {
		return bindBottomLeft(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	}
	public static @NonNull Drawable bindBottomLeft(@NonNull Drawable drawable, int w, int h) {
		drawable.setBounds(0 /*left*/, -h /*top*/, w /*right*/, 0 /*bottom*/);
		return drawable;
	}

	public static @NonNull Drawable bindBottomRight(@NonNull Drawable drawable) {
		return bindBottomRight(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	}
	public static @NonNull Drawable bindBottomRight(@NonNull Drawable drawable, int w, int h) {
		drawable.setBounds(-w /*left*/, -h /*top*/, 0 /*right*/, 0 /*bottom*/);
		return drawable;
	}

	public static @NonNull Drawable bindTopLeft(@NonNull Drawable drawable) {
		return bindTopLeft(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	}
	public static @NonNull Drawable bindTopLeft(@NonNull Drawable drawable, int w, int h) {
		drawable.setBounds(0 /*left*/, 0 /*top*/, w /*right*/, h /*bottom*/);
		return drawable;
	}

	public static @NonNull Drawable bindTopRight(@NonNull Drawable drawable) {
		return bindTopRight(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	}
	public static @NonNull Drawable bindTopRight(@NonNull Drawable drawable, int w, int h) {
		drawable.setBounds(-w /*left*/, 0 /*top*/, 0 /*right*/, h /*bottom*/);
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
