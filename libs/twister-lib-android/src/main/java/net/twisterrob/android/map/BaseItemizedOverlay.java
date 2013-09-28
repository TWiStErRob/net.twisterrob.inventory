package net.twisterrob.android.map;

import android.graphics.*;
import android.graphics.drawable.Drawable;

import com.google.android.maps.*;

/**
 * Binders constraints:<br>
 * <ul>
 * <li>width = right - left</li>
 * <li>height = bottom - top</li>
 * </ul>
 */
public abstract class BaseItemizedOverlay<T extends OverlayItem> extends ItemizedOverlay<T> {
	private final Drawable m_defaultMarker;
	private final Drawable m_focusedMarker;
	private Boolean m_wantShadow;

	/**
	 * Constructor for BaseItemizedOverlay
	 * 
	 * @param defaultMarker Default marker to show for the items. Don't forget to bind it with
	 *            {@link #bind(Drawable, VBind, HBind)} or related methods.
	 */
	public BaseItemizedOverlay(Drawable defaultMarker) {
		this(defaultMarker, null);
	}

	/**
	 * Constructor for BaseItemizedOverlay
	 * 
	 * @param defaultMarker Default marker to show for the items. Don't forget to bind it with
	 *            {@link #bind(Drawable, VBind, HBind)} or related methods.
	 * @param focusedMarker Marker to show for the items when they're focused/selected. Don't forget to bind it with
	 *            {@link #bind(Drawable, VBind, HBind)} or related methods.
	 */
	public BaseItemizedOverlay(Drawable defaultMarker, Drawable focusedMarker) {
		super(defaultMarker);
		m_defaultMarker = defaultMarker;
		m_focusedMarker = focusedMarker;
		setOnFocusChangeListener((OnFocusChangeListener<T>)null);
	}

	public Drawable getDefaultMarker() {
		return m_defaultMarker;
	}
	public Drawable getFocusedMarker() {
		return m_focusedMarker;
	}

	@Override
	public void setOnFocusChangeListener(final com.google.android.maps.ItemizedOverlay.OnFocusChangeListener l) {
		super.setOnFocusChangeListener(new com.google.android.maps.ItemizedOverlay.OnFocusChangeListener() {
			@SuppressWarnings("unchecked")
			public void onFocusChanged(@SuppressWarnings("rawtypes") ItemizedOverlay overlay, OverlayItem newFocus) {
				BaseItemizedOverlay.this.onFocusChanged((T)newFocus);
				if (l != null) {
					l.onFocusChanged(overlay, newFocus);
				}
			}
		});
	}
	public void setOnFocusChangeListener(final OnFocusChangeListener<T> l) {
		super.setOnFocusChangeListener(new com.google.android.maps.ItemizedOverlay.OnFocusChangeListener() {
			@SuppressWarnings("unchecked")
			public void onFocusChanged(@SuppressWarnings("rawtypes") ItemizedOverlay overlay, OverlayItem newFocus) {
				BaseItemizedOverlay.this.onFocusChanged((T)newFocus);
				if (l != null) {
					l.onFocusChanged((BaseItemizedOverlay<T>)overlay, (T)newFocus);
				}
			}
		});
	}

	public interface OnFocusChangeListener<T extends OverlayItem> {
		public void onFocusChanged(BaseItemizedOverlay<T> overlay, T newFocus);
	}

	private T m_lastFocus;
	protected void onFocusChanged(T newFocus) {
		if (getFocusedMarker() != null) {
			if (getLastFocus() != null) {
				getLastFocus().setMarker(getDefaultMarker());
			}
			if (newFocus != null) {
				newFocus.setMarker(getFocusedMarker());
			}
		}
		m_lastFocus = newFocus;
	}
	public T getLastFocus() {
		return m_lastFocus;
	}

	public void setShadow(Boolean wantShadow) {
		m_wantShadow = wantShadow;
	}
	public Boolean getShadow() {
		return m_wantShadow;
	}
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, m_wantShadow == null? shadow : m_wantShadow);
	}
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		return super.draw(canvas, mapView, m_wantShadow == null? shadow : m_wantShadow, when);
	}

	public static Drawable bind(Drawable drawable, VBind vertical, HBind horizontal) {
		Rect rect = new Rect();
		vertical.set(rect, drawable.getIntrinsicHeight());
		horizontal.set(rect, drawable.getIntrinsicWidth());
		drawable.setBounds(rect);
		return drawable;
	}

	public static Drawable bindCenter(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = -w / 2;
		int right = +w / 2;
		int top = -h / 2;
		int bottom = +h / 2;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindBottomLeft(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = 0;
		int right = w;
		int top = -h;
		int bottom = 0;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindBottomRight(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = -w;
		int right = 0;
		int top = -h;
		int bottom = 0;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindTopLeft(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = 0;
		int right = w;
		int top = 0;
		int bottom = h;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static Drawable bindTopRight(Drawable drawable) {
		final int w = drawable.getIntrinsicWidth();
		final int h = drawable.getIntrinsicHeight();
		int left = -w;
		int right = 0;
		int top = 0;
		int bottom = h;
		drawable.setBounds(left, top, right, bottom);
		return drawable;
	}

	public static void drawTriangle(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3,
			Paint paint) {
		Path path = new Path();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.close();

		canvas.drawPath(path, paint);
	}
}
