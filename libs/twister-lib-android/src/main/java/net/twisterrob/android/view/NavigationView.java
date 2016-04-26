package net.twisterrob.android.view;

import android.content.Context;
import android.os.Build.*;
import android.os.*;
import android.util.AttributeSet;

/**
 * @see <a href="https://code.google.com/p/android/issues/detail?id=196430#c15">Workaround for Android#196430</a>
 */
// FIXME similar for CoordinatorLayout? Need to repro it first so the fix can be confirmed.
public class NavigationView extends android.support.design.widget.NavigationView {
	public NavigationView(Context context) {
		super(context);
	}
	public NavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override protected void onRestoreInstanceState(Parcelable savedState) {
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB && savedState instanceof SavedState) {
			// Only work around for 2.3.x; based on https://code.google.com/p/android/issues/detail?id=196430#c10
			Bundle menuState = ((SavedState)savedState).menuState;
			if (menuState != null) {
				menuState.setClassLoader(SavedState.class.getClassLoader());
			}
		}
		//noinspection ConstantConditions the super would've received the same if we didn't override
		super.onRestoreInstanceState(savedState);
	}
}
