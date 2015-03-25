package net.twisterrob.android.view;

import android.annotation.SuppressLint;
import android.view.*;
import android.view.View.OnTouchListener;

/**
 * Attach this to a scrollable View and will work regardless of any parent container which may be scrollable in turn.
 *
 * @see <a href="http://stackoverflow.com/q/8121491/253468">Scrollable TextView in a ListView</a>
 */
public class DeepScrollFixListener implements OnTouchListener {
	@SuppressLint("ClickableViewAccessibility")
	@Override public boolean onTouch(View v, MotionEvent event) {
		v.getParent().requestDisallowInterceptTouchEvent(true);
		return false;
	}
}
