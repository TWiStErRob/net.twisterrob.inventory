package net.twisterrob.android.view.layout;

import android.annotation.TargetApi;
import android.os.Build.*;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * @see <a href="http://stackoverflow.com/a/29172475/253468"> </a>
 */
@TargetApi(VERSION_CODES.JELLY_BEAN)
public abstract class DoAfterLayout implements OnGlobalLayoutListener {
	private final View view;
	public DoAfterLayout(View view) {
		this.view = view;
		view.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}
	@SuppressWarnings("deprecation")
	@Override public final void onGlobalLayout() {
		// requests a non-floating observer which is guaranteed to be alive
		// this prevents IllegalStateException: "This ViewTreeObserver is not alive, call getViewTreeObserver() again"
		ViewTreeObserver observer = view.getViewTreeObserver();
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			observer.removeOnGlobalLayoutListener(this);
		} else {
			observer.removeGlobalOnLayoutListener(this);
		}
		onLayout();
	}
	protected abstract void onLayout();
}
