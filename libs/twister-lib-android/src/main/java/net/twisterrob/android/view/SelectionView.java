package net.twisterrob.android.view;

import static java.lang.Math.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.*;

import static android.support.v4.content.ContextCompat.*;

import net.twisterrob.android.R;
import net.twisterrob.android.utils.tools.AndroidTools;

/**
 * @see <a href="http://adblogcat.com/a-camera-preview-with-a-bounding-box-like-google-goggles/">based on</a>
 */
public class SelectionView extends View {
	private static final Logger LOG = LoggerFactory.getLogger(SelectionView.class);

	@SuppressLint("InlinedApi")
	private static final int INVALID_POINTER_ID = MotionEvent.INVALID_POINTER_ID;

	private Corners corners;
	private float mTouchDistance;
	private final CornerSelection cornerSelection = new CornerSelection();

	private Rect selection;
	private float originalRatio;
	private boolean keepAspectRatio = false;
	@SuppressWarnings("FieldCanBeLocal") // field exists just for documentation for now
	private final boolean shrinkWhenTouchesEdge = false;
	@SuppressWarnings("FieldCanBeLocal") // field exists just for documentation for now
	private final boolean stopWhenTouchesEdge = true;

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
		line.setStrokeWidth(AndroidTools.dip(context, 4));

		mTouchDistance = AndroidTools.dip(context, 32);
	}

	public Rect getSelection() {
		return selection != null? new Rect(selection) : null;
	}

	public void setSelection(Rect selection) {
		this.selection = selection != null? new Rect(selection) : null;
		if (selection != null) {
			this.originalRatio = (float)selection.width() / (float)selection.height();
		}
		invalidate();
	}

	public void setKeepAspectRatio(boolean keepAspectRatio) {
		this.keepAspectRatio = keepAspectRatio;
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
		updatePendingSelection();
	}

	private void updatePendingSelection() {
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
		updatePendingSelection();
	}

	@Override protected void onSizeChanged(int newW, int newH, int oldW, int oldH) {
		super.onSizeChanged(newW, newH, oldW, oldH);
		hasSize = true;
		resizeSelectionRelatively(newW, newH, oldW, oldH);
		updatePendingSelection();
	}

	private void resizeSelectionRelatively(int newW, int newH, int oldW, int oldH) {
		if (selection == null || (oldW == 0 && oldH == 0)) {
			return;
		}
		Rect s = selection;
		s.left = Math.round((float)s.left / oldW * newW);
		s.right = Math.round((float)s.right / oldW * newW);
		s.top = Math.round((float)s.top / oldH * newH);
		s.bottom = Math.round((float)s.bottom / oldH * newH);
		limitSize();
		correctSelection();
		invalidate();
	}

	/** @see @SuppressLint("DrawAllocation") */
	private final Rect tmpDrawSelection = new Rect();
	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (selection == null) {
			return;
		}
		tmpDrawSelection.set(selection);
		tmpDrawSelection.sort(); // need to order because of CCW ordering I guess

		if (fadeNonSelection) {
			canvas.save();
			canvas.clipRect(tmpDrawSelection, Op.DIFFERENCE); // the full canvas minus the selection is clipped
			canvas.drawColor(0x88000000); // semi-transparent black shade
			canvas.restore();
		}

		canvas.drawRect(tmpDrawSelection, line);

		corners.draw(canvas);
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
				cornerSelection.pickCorner(selection, x, y, mTouchDistance);
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
				cornerSelection.onMovement(selection, dx, dy);

				correctSelection();
				mLastTouch.set(x, y); // Remember this touch position for the next move event
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: { // last pointer up
				cornerSelection.clear();
				limitSize();
				correctSelection();
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
	private void limitSize() {
		selection.sort();
		int limit = (int)(mTouchDistance * 2);
		// (w/h - limit) will be negative if w/h is too small, min() constrains it to expand-only
		selection.inset(
				Math.min(0, (selection.width() - limit) / 2),
				Math.min(0, (selection.height() - limit) / 2)
		);
	}

	// FIXME widening the selection with left going out left and right going towards right double speed
	@SuppressWarnings("ConstantConditions") // *WhenTouchesEdge is constant for now
	private void correctSelection() {
		Rect s = selection;
		// s.sort(); // cannot sort the rectangle because it wouldn't allow natural selection to the user
		if (keepAspectRatio) {
			float currentRatio = (float)s.width() / (float)s.height();
			if (Math.abs(currentRatio - originalRatio) > 1e-5) {
				if (currentRatio > originalRatio) {
					int dx = (int)(s.width() - s.height() * originalRatio);
//					LOG.trace("RatioX {}, original: {}, inset: {}", currentRatio, originalRatio, dx);
					s.inset(dx / 2, 0);
				} else {
					int dy = (int)(s.height() - s.width() / originalRatio);
//					LOG.trace("RatioY {}, original: {}, inset: {}", currentRatio, originalRatio, dy);
					s.inset(0, dy / 2);
				}
			}
		}

		// selection may not be sorted, so can't assume left < right and top < bottom,
		// which also implies negative width and height are possible -> abs and min/max are used

		int width = Math.abs(s.width());
		if (getWidth() < width) {
			int dx = width - getWidth();
			dx *= s.width() < 0? -1 : +1;
//			LOG.trace("Too wide: {} > {}, inset: {}", width, getWidth(), dx / 2);
			s.inset(dx / 2, 0);
		}

		int height = Math.abs(s.height());
		if (getHeight() < height) {
			int dy = height - getHeight();
			dy *= s.height() < 0? -1 : +1;
//			LOG.trace("Too tall: {} > {}, inset: {}", height, getHeight(), dy / 2);
			s.inset(0, dy / 2);
		}

		int left = Math.min(s.left, s.right);
		if (left < 0) {
//			LOG.trace("Out left: {}, offset: {}", left, -left);
			left = 0 - left; // undershoot value
			if (stopWhenTouchesEdge) {
				s.offset(+left, 0);
				if (shrinkWhenTouchesEdge) {
					if (s.left < s.right) {
						s.right -= left;
					} else {
						s.left -= left;
					}
				}
			}
		}

		int top = Math.min(s.top, s.bottom);
		if (top < 0) {
//			LOG.trace("Out top: {}, offset: {}", top, -top);
			top = 0 - top; // undershoot value
			if (stopWhenTouchesEdge) {
				s.offset(0, +top);
				if (shrinkWhenTouchesEdge) {
					if (s.top < s.bottom) {
						s.bottom -= top;
					} else {
						s.top -= top;
					}
				}
			}
		}

		int right = Math.max(s.right, s.left);
		if (getWidth() < right) {
//			LOG.trace("Out right: {} (>{}), offset: {}", right, getWidth(), -(right - getWidth()));
			right = right - getWidth(); // overshoot value
			if (stopWhenTouchesEdge) {
				s.offset(-right, 0);
				if (shrinkWhenTouchesEdge) {
					if (s.left < s.right) {
						s.left += right;
					} else {
						s.right += right;
					}
				}
			}
		}

		int bottom = Math.max(s.bottom, s.top);
		if (getHeight() < bottom) {
//			LOG.trace("Out bottom: {} (>{}), offset: {}", bottom, getHeight(), -(bottom - getHeight()));
			bottom = bottom - getHeight(); // overshoot value
			if (stopWhenTouchesEdge) {
				s.offset(0, -bottom);
				if (shrinkWhenTouchesEdge) {
					if (s.top < s.bottom) {
						s.top += bottom;
					} else {
						s.bottom += bottom;
					}
				}
			}
		}
	}

	private static class CornerSelection {
		// when one of these is true, that means it can move when onDraw is called
		private boolean mLeftTopBool = false;
		private boolean mRightTopBool = false;
		private boolean mLeftBottomBool = false;
		private boolean mRightBottomBool = false;
		private boolean mRelocating = false;

		private boolean hasPickedCorner() {
			return mLeftTopBool || mLeftBottomBool || mRightBottomBool || mRightTopBool;
		}
		private boolean isRelocating() {
			return mRelocating;
		}

		public void onMovement(Rect selection, float dx, float dy) {
			if (hasPickedCorner()) {
				if (mLeftTopBool) {
					dx *= +1;
					dy *= +1;
				} else if (mLeftBottomBool) {
					dx *= +1;
					dy *= -1;
				} else if (mRightBottomBool) {
					dx *= -1;
					dy *= -1;
				} else if (mRightTopBool) {
					dx *= -1;
					dy *= +1;
				}
				selection.inset((int)dx, (int)dy);
			} else if (isRelocating()) {
				selection.offset((int)dx, (int)dy);
			}
		}

		public void clear() {
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
		public void pickCorner(Rect selection, float x, float y, double touchDistance) {
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
				sb.append("mLeftTopBool");
			}
			if (mLeftBottomBool) {
				sb.append("mLeftBottomBool");
			}
			if (mRightTopBool) {
				sb.append("mRightTopBool");
			}
			if (mRightBottomBool) {
				sb.append("mRightBottomBool");
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
			corners.invalidate(selection);
		}
	}

	private static class Corners {
		private final Drawable mLeftTopIcon;
		private final Drawable mRightTopIcon;
		private final Drawable mLeftBotIcon;
		private final Drawable mRightBotIcon;
		private final int mCornerSize;

		public Corners(Context context) {
			mLeftTopIcon = getDrawable(context, R.drawable.selection_corner);
			mRightTopIcon = getDrawable(context, R.drawable.selection_corner);
			mLeftBotIcon = getDrawable(context, R.drawable.selection_corner);
			mRightBotIcon = getDrawable(context, R.drawable.selection_corner);

			mCornerSize = AndroidTools.dipInt(context, 16);
		}

		public void draw(Canvas canvas) {
			mLeftTopIcon.draw(canvas);
			mRightTopIcon.draw(canvas);
			mRightBotIcon.draw(canvas);
			mLeftBotIcon.draw(canvas);
		}

		public void invalidate(Rect sel) {
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

		private final int color;

		SelectionStatus(int color) {
			this.color = color;
		}

		public int getColor() {
			return color;
		}

		private void updatePaint(Paint paint) {
			paint.setColor(color);
		}
	}

	public void setSelectionStatus(SelectionStatus status) {
		status.updatePaint(line);
		invalidate();
	}
}
