package com.twister.android.map;

import android.graphics.Rect;

public enum HBind {
	LEFT {
		@Override
		protected void set(Rect r, int w) {
			r.left = 0;
			r.right = w;
		}
	},
	CENTER {
		@Override
		protected void set(Rect r, int w) {
			r.left = -w / 2;
			r.right = +w / 2;
		}
	},
	RIGHT {
		@Override
		protected void set(Rect r, int w) {
			r.left = -w;
			r.right = 0;
		}
	};
	protected abstract void set(Rect rect, int width);
}