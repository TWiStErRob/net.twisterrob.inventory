package net.twisterrob.android.map;

import android.view.*;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;

public class DelegatingOnGestureListener implements OnGestureListener, OnDoubleTapListener {
	private OnGestureListener m_onGestureListener;
	private OnDoubleTapListener m_onDoubleTapListener;

	public DelegatingOnGestureListener() {
		this(null, null);
	}

	public DelegatingOnGestureListener(OnGestureListener onGestureListener) {
		this(onGestureListener, null);
	}
	public DelegatingOnGestureListener(OnGestureListener onGestureListener, boolean checkOther) {
		this(onGestureListener, (OnDoubleTapListener)(checkOther && onGestureListener instanceof OnDoubleTapListener
				? onGestureListener
				: null));
	}
	public DelegatingOnGestureListener(OnDoubleTapListener onDoubleTapListener) {
		this(null, onDoubleTapListener);
	}
	public DelegatingOnGestureListener(OnDoubleTapListener onDoubleTapListener, boolean checkOther) {
		this((OnGestureListener)(checkOther && onDoubleTapListener instanceof OnGestureListener
				? onDoubleTapListener
				: null), onDoubleTapListener);
	}

	public DelegatingOnGestureListener(OnGestureListener onGestureListener, OnDoubleTapListener onDoubleTapListener) {
		setOnGesureListener(onGestureListener);
		setOnDoubleTapListener(onDoubleTapListener);
	}

	public void setOnGesureListener(OnGestureListener onGestureListener) {
		if (onGestureListener != null) {
			this.m_onGestureListener = onGestureListener;
		} else {
			this.m_onGestureListener = new GestureDetector.SimpleOnGestureListener();
		}
	}

	public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
		if (onDoubleTapListener != null) {
			this.m_onDoubleTapListener = onDoubleTapListener;
		} else {
			this.m_onDoubleTapListener = new GestureDetector.OnDoubleTapListener() {
				/**
				 * No-op event handler.
				 * @param e unused event
				 */
				public boolean onSingleTapConfirmed(MotionEvent e) {
					return false;
				}
				/**
				 * No-op event handler.
				 * @param e unused event
				 */
				public boolean onDoubleTapEvent(MotionEvent e) {
					return false;
				}
				/**
				 * No-op event handler.
				 * @param e unused event
				 */
				public boolean onDoubleTap(MotionEvent e) {
					return false;
				}
			};
		}
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return m_onGestureListener.onSingleTapUp(e);
	}
	public void onShowPress(MotionEvent e) {
		m_onGestureListener.onShowPress(e);
	}
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return m_onGestureListener.onScroll(e1, e2, distanceX, distanceY);
	}
	public void onLongPress(MotionEvent e) {
		m_onGestureListener.onLongPress(e);
	}
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return m_onGestureListener.onFling(e1, e2, velocityX, velocityY);
	}

	public boolean onDown(MotionEvent e) {
		return m_onGestureListener.onDown(e);
	}
	public boolean onDoubleTap(MotionEvent e) {
		return m_onDoubleTapListener.onDoubleTap(e);
	}
	public boolean onDoubleTapEvent(MotionEvent e) {
		return m_onDoubleTapListener.onDoubleTapEvent(e);
	}
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return m_onDoubleTapListener.onSingleTapConfirmed(e);
	}
}
