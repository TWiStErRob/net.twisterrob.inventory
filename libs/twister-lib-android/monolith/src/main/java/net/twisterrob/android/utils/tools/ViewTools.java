package net.twisterrob.android.utils.tools;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.*;
import android.support.annotation.IdRes;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.*;
import android.view.ViewGroup.*;
import android.widget.*;

@SuppressWarnings({"unused", "StaticMethodOnlyUsedInOneClass", "SameParameterValue"})
public /*static*/ abstract class ViewTools {
	private static final Logger LOG = LoggerFactory.getLogger(ViewTools.class);

	public static void enabledIf(View view, boolean isEnabled) {
		if (view != null) {
			view.setEnabled(isEnabled);
		}
	}
	public static void enabledIf(Menu menu, @IdRes int itemID, boolean isEnabled) {
		enabledIf(menu.findItem(itemID), isEnabled);
	}
	public static void enabledIf(MenuItem item, boolean isEnabled) {
		if (item == null) {
			return;
		}
		item.setEnabled(isEnabled);
		Drawable icon = item.getIcon();
		if (icon != null) {
			icon = icon.mutate();
			icon.setAlpha(isEnabled? 0xFF : 0x80);
			item.setIcon(icon);
		}
	}

	/** Borrowing from CSS terminology: <code>display:block/none</code> */
	public static void displayedIf(View view, boolean isVisible) {
		if (view != null) {
			view.setVisibility(isVisible? View.VISIBLE : View.GONE);
		}
	}
	/** Borrowing from CSS terminology: <code>visibility:visible/hidden</code> */
	public static void visibleIf(View view, boolean isVisible) {
		if (view != null) {
			view.setVisibility(isVisible? View.VISIBLE : View.INVISIBLE);
		}
	}
	public static void visibleIf(Menu menu, @IdRes int itemID, boolean isVisible) {
		visibleIf(menu.findItem(itemID), isVisible);
	}
	public static void visibleIf(MenuItem item, boolean isVisible) {
		if (item != null) {
			item.setVisible(isVisible);
		}
	}
	public static void displayedIfHasText(TextView view) {
		if (view == null) {
			return;
		}
		CharSequence text = view.getText();
		displayedIf(view, !TextUtils.isEmpty(text) && 0 < TextUtils.getTrimmedLength(text));
	}
	public static void visibleIfHasText(TextView view) {
		if (view == null) {
			return;
		}
		CharSequence text = view.getText();
		visibleIf(view, text != null && 0 < text.length());
	}

	public static int getTopMargin(View view) {
		LayoutParams params = view.getLayoutParams();
		if (params instanceof MarginLayoutParams) {
			return ((MarginLayoutParams)params).topMargin;
		} else {
			return 0;
		}
	}

	public static int getBottomMargin(View view) {
		LayoutParams params = view.getLayoutParams();
		if (params instanceof MarginLayoutParams) {
			return ((MarginLayoutParams)params).bottomMargin;
		} else {
			return 0;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static void updateStartMargin(View view, int margin) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			params.leftMargin = margin;
		} else {
			params.setMarginStart(margin);
		}
		view.setLayoutParams(params);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static void updateEndMargin(View view, int margin) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			params.rightMargin = margin;
		} else {
			params.setMarginEnd(margin);
		}
		view.setLayoutParams(params);
	}

	public static void updateHeight(View view, int height) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.height = height;
		view.setLayoutParams(params);
	}

	public static void updateWidth(View view, int width) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = width;
		view.setLayoutParams(params);
	}

	public static void updateWidthAndHeight(View view, int width, int height) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = width;
		params.height = height;
		view.setLayoutParams(params);
	}

	@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
	// TOFIX all built-in and support LayoutParams
	public static void updateGravity(View view, int gravity) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (params instanceof FrameLayout.LayoutParams) {
			((FrameLayout.LayoutParams)params).gravity = gravity;
		} else if (params instanceof LinearLayout.LayoutParams) {
			((LinearLayout.LayoutParams)params).gravity = gravity;
		} else if (params instanceof DrawerLayout.LayoutParams) {
			((DrawerLayout.LayoutParams)params).gravity = gravity;
		} else if (VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT
				&& params instanceof GridLayout.LayoutParams) {
			((GridLayout.LayoutParams)params).setGravity(gravity);
		}
		view.setLayoutParams(params);
	}

	public static View findClosest(View view, @IdRes int viewId) {
		if (view.getId() == viewId) {
			return view;
		}
		ViewParent parent = view.getParent();
		if (parent instanceof ViewGroup) {
			ViewGroup group = (ViewGroup)parent;
			for (int index = 0; index < group.getChildCount(); index++) {
				View child = group.getChildAt(index);
				if (child.getId() == viewId) {
					return child;
				}
			}
			return findClosest(group, viewId);
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(VERSION_CODES.JELLY_BEAN)
	public static void setBackground(View view, Drawable backgroundDrawable) {
		if (VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
			view.setBackground(backgroundDrawable);
		} else {
			view.setBackgroundDrawable(backgroundDrawable);
		}
	}

	protected ViewTools() {
		// static utility class
	}
}
