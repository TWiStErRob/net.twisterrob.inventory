package net.twisterrob.android.map;

import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.google.android.maps.*;
import com.google.android.maps.ItemizedOverlay.OnFocusChangeListener;

public class PopupOverlayAdapter extends Overlay implements OnFocusChangeListener {
	public static final Paint DEFAULT_FONT = createDefaultFont();
	public static final Paint DEFAULT_FILL = createDefaultFill();
	public static final Paint DEFAULT_STROKE = createDefaultStroke();

	public interface OnClickListener {
		void onClick(MapView mapView, OverlayItem item);
	}

	protected final BaseItemizedOverlay<? extends OverlayItem> m_overlay;
	protected OverlayItem m_item;
	protected OnFocusChangeListener m_onFocusChangeListener;
	protected OnClickListener m_onClickListener;
	private Paint m_font = DEFAULT_FONT;
	private Paint m_fill = DEFAULT_FILL;
	private Paint m_stroke = DEFAULT_STROKE;
	private RectF m_lastDrawnPopupRect;
	private float m_vPadding = 8;
	private float m_hPadding = 10;
	private float m_popupDistance = 12;

	public PopupOverlayAdapter(BaseItemizedOverlay<? extends OverlayItem> overlay) {
		if (overlay == null) {
			throw new IllegalArgumentException("Wrapped overlay must be non-null.");
		}
		m_overlay = overlay;
		m_overlay.setOnFocusChangeListener(this);
	}

	public OnFocusChangeListener getOnFocusChangeListener() {
		return m_onFocusChangeListener;
	}
	public void setOnFocusChangeListener(OnFocusChangeListener l) {
		m_onFocusChangeListener = l;
	}

	public OnClickListener getOnClickListener() {
		return m_onClickListener;
	}
	public void setOnClickListener(OnClickListener l) {
		m_onClickListener = l;
	}

	public Paint getFont() {
		return m_font;
	}
	public void setFont(Paint font) {
		if (font == null) {
			throw new IllegalArgumentException("Popup text font must be non-null.");
		}
		m_font = font;
	}
	public Paint getFill() {
		return m_fill;
	}
	public void setFill(Paint fill) {
		m_fill = fill;
	}
	public Paint getStroke() {
		return m_stroke;
	}
	public void setStroke(Paint stroke) {
		m_stroke = stroke;
	}

	public float getHorizontalPadding() {
		return m_hPadding;
	}
	public void setHorizontalPadding(float hPadding) {
		m_hPadding = hPadding;
	}
	public float getVerticalPadding() {
		return m_vPadding;
	}
	public void setVerticalPadding(float vPadding) {
		m_vPadding = vPadding;
	}
	public float getPopupDistance() {
		return m_popupDistance;
	}
	public void setPopupDistance(float popupDistance) {
		m_popupDistance = popupDistance;
	}

	public BaseItemizedOverlay<? extends OverlayItem> getWrappedOverlay() {
		return m_overlay;
	}

	public void onFocusChanged(ItemizedOverlay overlay, OverlayItem newFocus) {
		m_item = newFocus; // may be null
		m_lastDrawnPopupRect = null;
		if (m_onFocusChangeListener != null) {
			m_onFocusChangeListener.onFocusChanged(overlay, newFocus);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		if (m_lastDrawnPopupRect != null && e.getAction() == MotionEvent.ACTION_DOWN
				&& m_lastDrawnPopupRect.contains(e.getX(), e.getY())) {
			if (m_onClickListener != null) {
				m_onClickListener.onClick(mapView, m_item);
			}
			return false;
		}
		return super.onTouchEvent(e, mapView);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if (m_item != null) {
			Rect bounds = getDrawnItemBounds();
			Point pixels = mapView.getProjection().toPixels(m_item.getPoint(), null);
			pixels.offset(bounds.centerX(), bounds.top); // pixels is now top-center

			Paint font = m_font;
			String title = m_item.getTitle();
			float titleTextWidth = font.measureText(title);
			float titleTextHeight = font.getTextSize();
			float titleWidth = titleTextWidth + 2 * m_hPadding;
			float titleHeight = titleTextHeight + 2 * m_vPadding;

			m_lastDrawnPopupRect = new RectF(// used for onClick
					pixels.x - titleWidth / 2, // left
					pixels.y - m_popupDistance - titleHeight, // top
					pixels.x + titleWidth / 2, // right
					pixels.y - m_popupDistance); // bottom

			if (m_fill != null) {
				canvas.drawRoundRect(m_lastDrawnPopupRect, m_vPadding / 2, m_hPadding / 2, m_fill); // inner
			}
			if (m_stroke != null) {
				final int arrowWidth = 6;
				BaseItemizedOverlay.drawTriangle(canvas, // arrow to popup
						pixels.x, pixels.y, // p1
						m_lastDrawnPopupRect.centerX() + arrowWidth, m_lastDrawnPopupRect.bottom, // p2
						m_lastDrawnPopupRect.centerX() - arrowWidth, m_lastDrawnPopupRect.bottom, // p3
						getStroke());
				canvas.drawRoundRect(m_lastDrawnPopupRect, m_vPadding / 2, m_hPadding / 2, m_stroke); // outer
			}
			canvas.drawText(title, // text
					pixels.x - titleTextWidth / 2, // x: align center horizontally
					pixels.y - m_popupDistance - m_vPadding - font.getFontMetrics().descent, // y
					font);
		}
	}
	protected Rect getDrawnItemBounds() {
		Drawable marker = m_item.getMarker(0);
		if (marker == null) {
			marker = m_overlay.getDefaultMarker();
		}
		Rect bounds = marker.copyBounds();
		return bounds;
	}

	private static Paint createDefaultFont() {
		Paint font = new Paint();
		font.setTextSize(24);
		font.setFakeBoldText(true);
		return font;
	}
	private static Paint createDefaultFill() {
		Paint fill = new Paint();
		fill.setStyle(Style.FILL);
		fill.setColor(Color.argb(0x66, 0x99, 0xcc, 0xff));
		return fill;
	}
	private static Paint createDefaultStroke() {
		Paint stroke = new Paint();
		stroke.setStrokeWidth(4);
		stroke.setStyle(Style.STROKE);
		stroke.setColor(Color.argb(0x99, 0x66, 0x99, 0xff));
		return stroke;
	}
}
