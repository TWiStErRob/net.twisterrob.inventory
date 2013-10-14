package net.twisterrob.android.map;

import android.content.Context;
import android.view.*;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;

import com.google.android.maps.*;

public class GestureOverlay extends Overlay {
	private final Context m_context;
	private GestureDetector m_gestureDetector;
	private OnDoubleTapListener m_onDoubleTapListener;

	public GestureOverlay() {
		this(null, null);
	}

	public GestureOverlay(Context context) {
		this(context, null);
	}

	public GestureOverlay(OnGestureListener onGestureListener) {
		this(null, onGestureListener);
	}

	public GestureOverlay(Context context, OnGestureListener onGestureListener) {
		m_context = context;
		setOnGestureListener(onGestureListener);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (m_gestureDetector != null) {
			return m_gestureDetector.onTouchEvent(event);
		}
		return false;
	}
	public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
		if (m_gestureDetector != null) {
			m_gestureDetector.setOnDoubleTapListener(m_onDoubleTapListener = onDoubleTapListener);
		}
	}

	public boolean isLongpressEnabled() {
		if (m_gestureDetector != null) {
			return m_gestureDetector.isLongpressEnabled();
		}
		return false;
	}

	public void setIsLongpressEnabled(boolean isLongpressEnabled) {
		if (m_gestureDetector != null) {
			m_gestureDetector.setIsLongpressEnabled(isLongpressEnabled);
		}
	}

	public void setOnGestureListener(OnGestureListener onGestureListener) {
		if (onGestureListener == null) {
			m_gestureDetector = null;
			return;
		}
		m_gestureDetector = new GestureDetector(m_context, onGestureListener);
		if (onGestureListener instanceof OnDoubleTapListener) {
			m_onDoubleTapListener = (OnDoubleTapListener)onGestureListener;
		}
		setOnDoubleTapListener(m_onDoubleTapListener);
	}
}
