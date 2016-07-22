package android.support.v4.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.*;

public class ClickThroughDrawerLayout extends android.support.v4.widget.DrawerLayout {
	/** Copied from DrawerLayout for access */
	private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

	private int oldScrimColor = DEFAULT_SCRIM_COLOR;
	private boolean allowClickThrough = false;
	private boolean onlyWhenLocked = false;

	public ClickThroughDrawerLayout(Context context) {
		super(context);
	}
	public ClickThroughDrawerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ClickThroughDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setAllowClickThrough(boolean allowClickThrough) {
		this.allowClickThrough = allowClickThrough;
		if (allowClickThrough) {
			super.setScrimColor(Color.TRANSPARENT);
		} else {
			super.setScrimColor(oldScrimColor);
		}
	}
	public void setAllowClickThroughOnlyWhenLocked(boolean onlyWhenLocked) {
		this.onlyWhenLocked = onlyWhenLocked;
	}
	@Override public void setScrimColor(@ColorInt int color) {
		super.setScrimColor(color);
		oldScrimColor = color;
	}

	@Override public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (allowClickThrough) {
			// Use explicit left and right gravity because we're doing math on their width.
			@SuppressLint("RtlHardcoded") View leftDrawer = findDrawerWithGravity(Gravity.LEFT);
			@SuppressLint("RtlHardcoded") View rightDrawer = findDrawerWithGravity(Gravity.RIGHT);
			boolean leftVisible = leftDrawer != null && isDrawerVisible(leftDrawer)
					&& (!onlyWhenLocked || getDrawerLockMode(leftDrawer) == LOCK_MODE_LOCKED_OPEN);
			boolean rightVisible = rightDrawer != null && isDrawerVisible(rightDrawer)
					&& (!onlyWhenLocked || getDrawerLockMode(rightDrawer) == LOCK_MODE_LOCKED_OPEN);
			boolean leftSaysClickable = leftVisible && leftDrawer.getWidth() < ev.getRawX();
			boolean rightSaysClickable = rightVisible && ev.getRawX() < getWidth() - rightDrawer.getWidth();
			if (leftSaysClickable && rightSaysClickable) {
				return false; // both are visible only between the two drawers is clickable
			} else if (!rightVisible && leftSaysClickable || !leftVisible && rightSaysClickable) {
				return false; // only one is visible, that one decides
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
}
