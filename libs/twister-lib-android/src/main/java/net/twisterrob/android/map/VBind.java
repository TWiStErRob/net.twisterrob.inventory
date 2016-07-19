package net.twisterrob.android.map;

import android.graphics.Rect;

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
