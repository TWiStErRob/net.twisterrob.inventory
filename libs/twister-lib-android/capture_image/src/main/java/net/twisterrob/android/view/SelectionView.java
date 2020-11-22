package net.twisterrob.android.view;

import static java.lang.Math.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Build.*;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.*;

import androidx.annotation.ColorInt;

import static androidx.core.content.ContextCompat.*;

import net.twisterrob.android.capture_image.R;
import net.twisterrob.android.utils.tools.ResourceTools;

/**
 * @see <a href="http://adblogcat.com/a-camera-preview-with-a-bounding-box-like-google-goggles/">based on</a>
 */
public class SelectionView extends View {
	private static final Logger LOG = LoggerFactory.getLogger(SelectionView.class);

	@SuppressLint("InlinedApi")
	private static final int INVALID_POINTER_ID = MotionEvent.INVALID_POINTER_ID;

	private Corners corners;
	private int mTouchDistance;
	private final CornerSelection cornerSelection = new CornerSelection();

	private Selection selection;
	private boolean keepAspectRatio = false;
	private SelectionStatus status;

	private static abstract class PendingMargin {
		protected abstract Rect produce(int width, int height);
	}

	private PendingMargin pendingMargin;
	private boolean hasSize = false;
	private boolean fadeNonSelection = true;

	private final PointF mLastTouch = new PointF();

	private Paint line;

	private int mActivePointerId = INVALID_POINTER_ID;

	public SelectionView(Context context) {
		super(context);
		init();
	}

	public SelectionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SelectionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		Context context = getContext();
		corners = new Corners(context);

		line = new Paint();
		line.setStyle(Style.STROKE);
		line.setColor(Color.CYAN);
		line.setStrokeWidth(ResourceTools.dip(context, 4));

		mTouchDistance = (int)ResourceTools.dip(context, 32);
	}

	public Rect getSelection() {
		return selection != null? new Rect(selection.selection) : null;
	}

	// TODO @AnyThread (currently IDEA warns all over the place)
	public void setSelection(Rect selection) {
		if (selection != null) {
			Rect size = new Rect(mTouchDistance, mTouchDistance, getWidth(), getHeight());
			this.selection = new Selection(selection, size, keepAspectRatio);
		} else {
			this.selection = null;
		}
		if (Looper.myLooper() != Looper.getMainLooper()) {
			post(new Runnable() {
				@Override public void run() {
					invalidate();
				}
			});
		} else {
			invalidate();
		}
	}

	public void setKeepAspectRatio(boolean keepAspectRatio) {
		this.keepAspectRatio = keepAspectRatio;
		setSelection(getSelection());
	}
	public boolean isKeepAspectRatio() {
		return keepAspectRatio;
	}

	public void setFadeNonSelection(boolean fadeNonSelection) {
		this.fadeNonSelection = fadeNonSelection;
	}
	public boolean isFadeNonSelection() {
		return fadeNonSelection;
	}

	/**
	 * @param margin ratio of view size that should be used as margin [0 .. 0.5)
	 */
	public void setSelectionMargin(float margin) {
		setSelectionMargin(margin, margin, margin, margin);
	}

	/**
	 * @param left ratio of view size that should be used as margin [0 .. 0.5)
	 * @param top ratio of view size that should be used as margin [0 .. 0.5)
	 * @param right ratio of view size that should be used as margin [0 .. 0.5)
	 * @param bottom ratio of view size that should be used as margin [0 .. 0.5)
	 */
	public void setSelectionMargin(final float left, final float top, final float right, final float bottom) {
		pendingMargin = new PendingMargin() {
			private final RectF pendingMargin = new RectF(left, top, 1 - right, 1 - bottom);
			@Override protected Rect produce(int width, int height) {
				Rect selection = new Rect();
				selection.left = (int)(width * pendingMargin.left);
				selection.right = (int)(width * pendingMargin.right);
				selection.top = (int)(height * pendingMargin.top);
				selection.bottom = (int)(height * pendingMargin.bottom);
				return selection;
			}
			@Override public String toString() {
				return "rect: " + pendingMargin;
			}
		};
		usePendingSelection();
	}

	private void usePendingSelection() {
		if (pendingMargin == null) {
			LOG.trace("No pending margin, update skipped");
			return;
		}
		if (!hasSize) {
			LOG.trace("No size yet, pending {}", pendingMargin);
			return;
		}
		int width = getWidth();
		int height = getHeight();
		LOG.trace("Updating selection based on pending margin: {} and size {}x{}", pendingMargin, width, height);
		setSelection(pendingMargin.produce(width, height));
		pendingMargin = null;
	}

	/**
	 * @param margin ratio of view size that should be used as margin [0 .. 0.5)
	 */
	public void setSelectionMarginSquare(final float margin) {
		pendingMargin = new PendingMargin() {
			@Override protected Rect produce(int width, int height) {
				Rect selection = new Rect();
				if (width > height) {
					float squareSize = height * (1 - 2 * margin);
					selection.left = (int)((width - squareSize) / 2);
					selection.right = (int)((width - squareSize) / 2 + squareSize);
					selection.top = (int)(height * margin);
					selection.bottom = (int)(height * (1 - margin));
				} else {
					float squareSize = width * (1 - 2 * margin);
					selection.left = (int)(width * margin);
					selection.right = (int)(width * (1 - margin));
					selection.top = (int)((height - squareSize) / 2);
					selection.bottom = (int)((height - squareSize) / 2 + squareSize);
				}
				return selection;
			}
			@Override public String toString() {
				return "square: " + margin;
			}
		};
		usePendingSelection();
	}

	@Override protected void onSizeChanged(int newW, int newH, int oldW, int oldH) {
		super.onSizeChanged(newW, newH, oldW, oldH);
		hasSize = true;
		if (oldW != 0 && oldH != 0) {
			selection.setMaxSize(newW, newH);
			invalidate();
		}
		usePendingSelection();
	}

	/** @see com.android.tools.lint.checks.JavaPerformanceDetector#PAINT_ALLOC */
	private final Rect tmpDrawSelection = new Rect();
	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (selection == null) {
			return;
		}
		tmpDrawSelection.set(selection.selection);
		tmpDrawSelection.sort(); // need to order because of CCW ordering I guess

		if (fadeNonSelection) {
			fadeNonSelection(canvas);
		}

		canvas.drawRect(tmpDrawSelection, line);

		corners.draw(canvas);
	}
	@SuppressWarnings("deprecation")
	private void fadeNonSelection(Canvas canvas) {
		canvas.save();
		// the full canvas minus the selection is clipped
		if (VERSION_CODES.O <= VERSION.SDK_INT) {
			canvas.clipOutRect(tmpDrawSelection);
		} else {
			canvas.clipRect(tmpDrawSelection, Op.DIFFERENCE);
		}
		canvas.drawColor(0x88000000); // semi-transparent black shade
		canvas.restore();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override public boolean onTouchEvent(MotionEvent ev) {
		if (selection == null) {
			return false;
		}

		final int pointerIndex = ev.getActionIndex();
		final float x = ev.getX(pointerIndex);
		final float y = ev.getY(pointerIndex);

		if (ev.getActionMasked() != MotionEvent.ACTION_MOVE) {
			//LOG.debug(ev.toString());
		}

		boolean handled = true;
		switch (ev.getActionMasked()) {
			default: {
				handled = false; // let super do what it wants
				break;
			}
			case MotionEvent.ACTION_DOWN: { // first pointer down
				cornerSelection.pickCorner(selection.selection, x, y, mTouchDistance);
				mLastTouch.set(x, y); // Remember where we started
				mActivePointerId = ev.getPointerId(pointerIndex);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (ev.getPointerId(pointerIndex) != mActivePointerId) {
					handled = false;
					break;
				}

				float dx = x - mLastTouch.x;
				float dy = y - mLastTouch.y;
				selection.onMovement(cornerSelection, dx, dy);
				mLastTouch.set(x, y); // Remember this touch position for the next move event
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: { // last pointer up
				cornerSelection.clear();
				selection.correctMinSize();
				selection.correctSelection();
				mActivePointerId = INVALID_POINTER_ID;
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: { // non-last pointer up
				if (ev.getPointerId(pointerIndex) == mActivePointerId) {
					// this was our active pointer going up, choose a new one
					final int newPointerIndex = (pointerIndex + 1) % ev.getPointerCount();
					mLastTouch.set(ev.getX(newPointerIndex), ev.getY(newPointerIndex));
					mActivePointerId = ev.getPointerId(newPointerIndex);
				}
				break;
			}
		}
		if (handled) {
			invalidate(); // selection quite possibly changed, redraw
		}
		return handled || super.onTouchEvent(ev);
	}

	private static class Selection {
		/**
		 * Sides of the selection within bounds of Rect[0,0 - size.right,size.bottom]<br>
		 * <b>DO NOT MODIFY from outside, accessible for performance reasons.</b>
		 */
		final Rect selection;
		/**
		 * Keep the aspect ratio that was the original selection passed in.
		 * @see #originalRatio
		 */
		private final boolean keepAspectRatio;
		/**
		 * The aspect ratio of the original selection, at the time of construction.
		 * @see #keepAspectRatio
		 */
		private final float originalRatio;
		/**
		 * width of selection should be between size.left and size.right
		 * height of selection should be between size.top and size.bottom
		 */
		private final Rect size;

		Selection(Rect selection, Rect size, boolean keepAspectRatio) {
			this.selection = selection;
			this.originalRatio = (float)selection.width() / (float)selection.height();
			this.size = size;
			this.keepAspectRatio = keepAspectRatio;
		}

		void correctMinSize() {
			selection.sort();
			// (w|h - limit) will be negative if w|h is too small, min() constrains it to expand-only
			selection.inset(
					Math.min(0, (selection.width() - size.left * 2) / 2),
					Math.min(0, (selection.height() - size.top * 2) / 2)
			);
		}

		/**
		 * Cut current selection down to allowed size. Anything outside the range will be set to nearest value.
		 */
		void clampSelection() {
			Rect s = selection;
			s.left = clamp(s.left, 0, size.right);
			s.right = clamp(s.right, 0, size.right);
			s.top = clamp(s.top, 0, size.bottom);
			s.bottom = clamp(s.bottom, 0, size.bottom);
		}
		private static int clamp(int value, int min, int max) {
			return value < min? min : (value > max? max : value);
		}

		void correctSelection() {
			// s.sort(); // cannot sort the rectangle because it wouldn't allow natural selection to the user
			if (this.keepAspectRatio) {
				correctRatio();
			}
			// selection may not be sorted, so can't assume left < right and top < bottom,
			// which also implies negative width and height are possible -> abs and min/max are used
			correctMaxSize();
			correctSides();
		}
		void correctRatio() {
			float currentRatio = (float)selection.width() / (float)selection.height();
			if (Math.abs(currentRatio - originalRatio) > 1e-5) {
				if (currentRatio > originalRatio) {
					int dx = (int)(selection.width() - selection.height() * originalRatio);
//					LOG.trace("RatioX {}, original: {}, inset: {}", currentRatio, originalRatio, dx);
					selection.inset(dx / 2, 0);
				} else {
					int dy = (int)(selection.height() - selection.width() / originalRatio);
//					LOG.trace("RatioY {}, original: {}, inset: {}", currentRatio, originalRatio, dy);
					selection.inset(0, dy / 2);
				}
			}
		}
		void correctMaxSize() {
			int width = Math.abs(selection.width());
			if (size.right < width) {
				int dx = width - size.right;
				dx *= selection.width() < 0? -1 : +1;
//			LOG.trace("Too wide: {} > {}, inset: {}", width, getWidth(), dx / 2);
				selection.inset(dx / 2, 0);
			}

			int height = Math.abs(selection.height());
			if (size.bottom < height) {
				int dy = height - size.bottom;
				dy *= selection.height() < 0? -1 : +1;
//			LOG.trace("Too tall: {} > {}, inset: {}", height, getHeight(), dy / 2);
				selection.inset(0, dy / 2);
			}
		}
		void correctSides() {
			int left = Math.min(selection.left, selection.right);
			if (left < 0) {
//			LOG.trace("Out left: {}, offset: {}", left, -left);
				left = 0 - left; // undershoot value
				selection.offset(+left, 0);
			}

			int top = Math.min(selection.top, selection.bottom);
			if (top < 0) {
//			LOG.trace("Out top: {}, offset: {}", top, -top);
				top = 0 - top; // undershoot value
				selection.offset(0, +top);
			}

			int right = Math.max(selection.right, selection.left);
			if (size.right < right) {
//			LOG.trace("Out right: {} (>{}), offset: {}", right, getWidth(), -(right - getWidth()));
				right = right - size.right; // overshoot value
				selection.offset(-right, 0);
			}

			int bottom = Math.max(selection.bottom, selection.top);
			if (size.bottom < bottom) {
//			LOG.trace("Out bottom: {} (>{}), offset: {}", bottom, getHeight(), -(bottom - getHeight()));
				bottom = bottom - size.bottom; // overshoot value
				selection.offset(0, -bottom);
			}
		}
		void setMaxSize(int newW, int newH) {
			selection.left = Math.round((float)selection.left / size.right * newW);
			selection.right = Math.round((float)selection.right / size.right * newW);
			selection.top = Math.round((float)selection.top / size.bottom * newH);
			selection.bottom = Math.round((float)selection.bottom / size.bottom * newH);
			size.right = newW;
			size.bottom = newH;
			correctMinSize();
			correctSelection();
		}

		void onMovement(CornerSelection cornerSelection, float dx, float dy) {
			if (cornerSelection.hasPickedCorner()) {
				if (cornerSelection.isLeftTop()) {
					dx *= +1;
					dy *= +1;
				} else if (cornerSelection.isLeftBottom()) {
					dx *= +1;
					dy *= -1;
				} else if (cornerSelection.isRightBottom()) {
					dx *= -1;
					dy *= -1;
				} else if (cornerSelection.isRightTop()) {
					dx *= -1;
					dy *= +1;
				}
				selection.inset((int)dx, (int)dy);
			} else if (cornerSelection.isRelocating()) {
				selection.offset((int)dx, (int)dy);
			}
			if (!cornerSelection.isRelocating()) {
				clampSelection();
			}
			correctSides();
		}
	}

	private static class CornerSelection {
		// when one of these is true, that means it can move when onDraw is called
		private boolean mLeftTopBool = false;
		private boolean mRightTopBool = false;
		private boolean mLeftBottomBool = false;
		private boolean mRightBottomBool = false;
		private boolean mRelocating = false;

		boolean isLeftTop() {
			return mLeftTopBool;
		}
		boolean isRightTop() {
			return mRightTopBool;
		}
		boolean isLeftBottom() {
			return mLeftBottomBool;
		}
		boolean isRightBottom() {
			return mRightBottomBool;
		}
		boolean hasPickedCorner() {
			return mLeftTopBool || mLeftBottomBool || mRightBottomBool || mRightTopBool;
		}
		boolean isRelocating() {
			return mRelocating;
		}

		void clear() {
			mLeftTopBool = false;
			mRightTopBool = false;
			mLeftBottomBool = false;
			mRightBottomBool = false;
			mRelocating = false;
		}

		/**
		 * Calculate the distance closest to one of the 4 corners at a given location,
		 * so that it can get the pressed and moved later. Only 1 corner at a time can be moved.
		 */
		void pickCorner(Rect selection, float x, float y, double touchDistance) {
			double leftTop = distance(x, y, selection.left, selection.top);
			double rightTop = distance(x, y, selection.right, selection.top);
			double leftBottom = distance(x, y, selection.left, selection.bottom);
			double rightBottom = distance(x, y, selection.right, selection.bottom);

			//LOG.debug("leftTop: {}, rightTop: {}, leftBottom: {}, rightBottom: {}",
			//		leftTop, rightTop, leftBottom, rightBottom);

			if (leftTop < touchDistance) {
				mLeftTopBool = true;
				mRightTopBool = false;
				mLeftBottomBool = false;
				mRightBottomBool = false;
			} else if (rightTop < touchDistance) {
				mLeftTopBool = false;
				mRightTopBool = true;
				mLeftBottomBool = false;
				mRightBottomBool = false;
			} else if (leftBottom < touchDistance) {
				mLeftTopBool = false;
				mRightTopBool = false;
				mLeftBottomBool = true;
				mRightBottomBool = false;
			} else if (rightBottom < touchDistance) {
				mLeftTopBool = false;
				mRightTopBool = false;
				mLeftBottomBool = false;
				mRightBottomBool = true;
			} else if (selection.contains((int)x, (int)y)) {
				mRelocating = true;
			} else {
				clear();
			}
		}

		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			if (mLeftTopBool) {
				sb.append("LeftTop");
			}
			if (mLeftBottomBool) {
				sb.append("LeftBottom");
			}
			if (mRightTopBool) {
				sb.append("RightTop");
			}
			if (mRightBottomBool) {
				sb.append("RightBottom");
			}
			if (sb.length() == 0) {
				sb.append("none");
			}
			return sb.toString();
		}

		private static double distance(float x1, float y1, float x2, float y2) {
			return sqrt(pow(x1 - x2, 2) + pow(y1 - y2, 2));
		}
	}

	@Override public void invalidate() {
		super.invalidate();
		if (selection != null) {
			corners.invalidate(selection.selection);
		}
	}

	private static class Corners {
		private final Drawable mLeftTopIcon;
		private final Drawable mRightTopIcon;
		private final Drawable mLeftBotIcon;
		private final Drawable mRightBotIcon;
		private final int mCornerSize;

		Corners(Context context) {
			mLeftTopIcon = getDrawable(context, R.drawable.selection_corner);
			mRightTopIcon = getDrawable(context, R.drawable.selection_corner);
			mLeftBotIcon = getDrawable(context, R.drawable.selection_corner);
			mRightBotIcon = getDrawable(context, R.drawable.selection_corner);

			mCornerSize = ResourceTools.dipInt(context, 16);
		}

		void draw(Canvas canvas) {
			mLeftTopIcon.draw(canvas);
			mRightTopIcon.draw(canvas);
			mRightBotIcon.draw(canvas);
			mLeftBotIcon.draw(canvas);
		}

		void invalidate(Rect sel) {
			int size = mCornerSize / 2;
			mLeftTopIcon.setBounds(sel.left - size, sel.top - size, sel.left + size, sel.top + size);
			mRightTopIcon.setBounds(sel.right - size, sel.top - size, sel.right + size, sel.top + size);
			mLeftBotIcon.setBounds(sel.left - size, sel.bottom - size, sel.left + size, sel.bottom + size);
			mRightBotIcon.setBounds(sel.right - size, sel.bottom - size, sel.right + size, sel.bottom + size);
		}
	}

	public enum SelectionStatus {
		NORMAL(Color.CYAN),
		FOCUSING(Color.YELLOW),
		FOCUSED(Color.GREEN),
		BLURRY(Color.RED);

		private final @ColorInt int color;

		SelectionStatus(@ColorInt int color) {
			this.color = color;
		}

		public @ColorInt int getColor() {
			return color;
		}

		private void updatePaint(Paint paint) {
			paint.setColor(color);
		}
	}

	public void setSelectionStatus(SelectionStatus status) {
		this.status = status;
		status.updatePaint(line);
		invalidate();
	}
	public SelectionStatus getSelectionStatus() {
		return status;
	}
}
