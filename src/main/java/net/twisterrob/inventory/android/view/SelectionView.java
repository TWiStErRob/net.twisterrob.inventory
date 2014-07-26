package net.twisterrob.inventory.android.view;

import static java.lang.Math.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.*;
import android.view.*;

import net.twisterrob.inventory.R;

/**
 * @see <a href="http://adblogcat.com/a-camera-preview-with-a-bounding-box-like-google-goggles/">based on</a>
 */
public class SelectionView extends View {
	private static final Logger LOG = LoggerFactory.getLogger(SelectionView.class);

	private static final double CORNER_SIZE_PERCENT = 0.05;
	private static final int MAX_DISTANCE = 50;

	private Drawable mLeftTopIcon;
	private Drawable mRightTopIcon;
	private Drawable mLeftBotIcon;
	private Drawable mRightBotIcon;

	private boolean mLeftTopBool = false;
	private boolean mRightTopBool = false;
	private boolean mLeftBottomBool = false;
	private boolean mRightBottomBool = false;
	private boolean mRelocating = false;

	private Rect selection;
	private float originalRatio;
	private boolean keepAspectRatio = false;
	private boolean fadeNonSelection = true;

	private PointF mLastTouch = new PointF();

	private Paint line;

	@SuppressLint("InlinedApi")
	private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;

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
		line = new Paint();
		line.setStyle(Style.STROKE);
		line.setColor(Color.CYAN);
		line.setStrokeWidth(15);

		Resources resources = getContext().getResources();
		mLeftTopIcon = resources.getDrawable(R.drawable.corner);
		mRightTopIcon = resources.getDrawable(R.drawable.corner);
		mLeftBotIcon = resources.getDrawable(R.drawable.corner);
		mRightBotIcon = resources.getDrawable(R.drawable.corner);
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

	public void setSelectionMargin(float margin) {
		setSelectionMargin(margin, margin, margin, margin);
	}

	public void setSelectionMargin(float left, float top, float right, float bottom) {
		WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(displaymetrics);
		int screenHeight = displaymetrics.heightPixels;
		int screenWidth = displaymetrics.widthPixels;
		Rect selection = new Rect();
		selection.left = (int)(screenWidth * left);
		selection.right = (int)(screenWidth * (1 - right));
		selection.top = (int)(screenHeight * top);
		selection.bottom = (int)(screenHeight * (1 - bottom));
		setSelection(selection);
	}

	public void setSelectionMarginSquare(float margin) {
		WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(displaymetrics);
		int screenHeight = displaymetrics.heightPixels;
		int screenWidth = displaymetrics.widthPixels;

		Rect selection = new Rect();
		if (screenWidth > screenHeight) {
			float squareSize = screenHeight * (1 - 2 * margin);
			selection.left = (int)((screenWidth - squareSize) / 2);
			selection.right = (int)((screenWidth - squareSize) / 2 + squareSize);
			selection.top = (int)(screenHeight * margin);
			selection.bottom = (int)(screenHeight * (1 - margin));
		} else {
			float squareSize = screenWidth * (1 - 2 * margin);
			selection.left = (int)(screenWidth * margin);
			selection.right = (int)(screenWidth * (1 - margin));
			selection.top = (int)((screenHeight - squareSize) / 2);
			selection.bottom = (int)((screenHeight - squareSize) / 2 + squareSize);
		}
		setSelection(selection);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

	/** @see @SuppressLint("DrawAllocation") */
	private final Rect tmpDrawSelection = new Rect();
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (selection == null) {
			return;
		}
		tmpDrawSelection.set(selection);
		tmpDrawSelection.sort(); // need to order because of CCW ordering I guess

		if (fadeNonSelection) {
			canvas.save();
			canvas.clipRect(tmpDrawSelection, Op.DIFFERENCE); // the full canvas minus the selection is clipped
			canvas.drawARGB(0x88, 0x00, 0x00, 0x00); // transparent black shade
			canvas.restore();
		}

		canvas.drawRect(tmpDrawSelection, line);

		mLeftTopIcon.draw(canvas);
		mRightTopIcon.draw(canvas);
		mRightBotIcon.draw(canvas);
		mLeftBotIcon.draw(canvas);
	}

	@SuppressLint({"ClickableViewAccessibility", "InlinedApi"})
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
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
				calculateSelectionAction(x, y);

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

				if (hasPickedCorner()) {
					if (mLeftTopBool) {
						dx *= 1;
						dy *= 1;
					} else if (mLeftBottomBool) {
						dx *= 1;
						dy *= -1;
					} else if (mRightBottomBool) {
						dx *= -1;
						dy *= -1;
					} else if (mRightTopBool) {
						dx *= -1;
						dy *= 1;
					}
					selection.inset((int)dx, (int)dy);
				} else if (mRelocating) {
					selection.offset((int)dx, (int)dy);
				}
				correctSelection();

				mLastTouch.set(x, y); // Remember this touch position for the next move event

				invalidate(); // selection changed, redraw
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: { // last pointer up
				resetPickedAction();
				selection.sort();
				mActivePointerId = MotionEvent.INVALID_POINTER_ID;
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
		return handled || super.onTouchEvent(ev);
	}

	/**
	 * TODO widening the selection with left going out left and right going towards right double speed
	 */
	private void correctSelection() {
		Rect s = selection;
		if (keepAspectRatio) {
			float currentRatio = (float)s.width() / (float)s.height();
			if (Math.abs(currentRatio - originalRatio) > 1e-5) {
				if (currentRatio > originalRatio) {
					int dx = (int)(s.width() - s.height() * originalRatio);
					//LOG.debug("RatioX {}, original: {}, inset: {}", currentRatio, originalRatio, dx);
					s.inset(dx / 2, 0);
				} else {
					int dy = (int)(s.height() - s.width() / originalRatio);
					//LOG.debug("RatioY {}, original: {}, inset: {}", currentRatio, originalRatio, dy);
					s.inset(0, dy / 2);
				}
			}
		}

		int width = Math.abs(s.width());
		if (width > getWidth()) {
			int dx = s.width() < 0? getWidth() - width : width - getWidth();
			//LOG.debug("Too wide: {} > {}, inset: {}", width, getWidth(), dx / 2);
			s.inset(dx / 2, 0);
		}

		int height = Math.abs(s.height());
		if (height > getHeight()) {
			int dy = s.height() < 0? getHeight() - height : height - getHeight();
			//LOG.debug("Too tall: {} > {}, inset: {}", height, getHeight(), dy / 2);
			s.inset(0, dy / 2);
		}

		int left = Math.min(s.left, s.right);
		if (left < 0) {
			//LOG.debug("Out left: {}, offset: {}", left, -left);
			s.offset(-left, 0);
		}

		int top = Math.min(s.top, s.bottom);
		if (top < 0) {
			//LOG.debug("Out top: {}, offset: {}", top, -top);
			s.offset(0, -top);
		}

		int right = Math.max(s.right, s.left);
		if (right > getWidth()) {
			//LOG.debug("Out right: {} (>{}), offset: {}", right, getWidth(), -(right - getWidth()));
			s.offset(-(right - getWidth()), 0);
		}

		int bottom = Math.max(s.bottom, s.top);
		if (bottom > getHeight()) {
			//LOG.debug("Out bottom: {} (>{}), offset: {}", bottom, getHeight(), -(bottom - getHeight()));
			s.offset(0, -(bottom - getHeight()));
		}
	}

	private boolean hasPickedCorner() {
		return mLeftTopBool || mLeftBottomBool || mRightBottomBool || mRightTopBool;
	}

	private void resetPickedAction() {
		// when one of these is true, that means it can move when onDraw is called
		mLeftTopBool = false;
		mRightTopBool = false;
		mLeftBottomBool = false;
		mRightBottomBool = false;
		mRelocating = false;
	}

	/** Where the screen is pressed, calculate the distance closest to one of the 4 corners
	  * so that it can get the pressed and moved. Only 1 at a time can be moved.
	  */
	private void calculateSelectionAction(float x, float y) {
		double leftTop = distance(x, y, selection.left, selection.top);
		double rightTop = distance(x, y, selection.right, selection.top);
		double leftBottom = distance(x, y, selection.left, selection.bottom);
		double rightBottom = distance(x, y, selection.right, selection.bottom);

		//LOG.debug("leftTop: {}, rightTop: {}, leftBottom: {}, rightBottom: {}", //
		//		leftTop, rightTop, leftBottom, rightBottom);

		if (leftTop < MAX_DISTANCE) {
			mLeftTopBool = true;
			mRightTopBool = false;
			mLeftBottomBool = false;
			mRightBottomBool = false;
		} else if (rightTop < MAX_DISTANCE) {
			mLeftTopBool = false;
			mRightTopBool = true;
			mLeftBottomBool = false;
			mRightBottomBool = false;
		} else if (leftBottom < MAX_DISTANCE) {
			mLeftTopBool = false;
			mRightTopBool = false;
			mLeftBottomBool = true;
			mRightBottomBool = false;
		} else if (rightBottom < MAX_DISTANCE) {
			mLeftTopBool = false;
			mRightTopBool = false;
			mLeftBottomBool = false;
			mRightBottomBool = true;
		} else if (selection.contains((int)x, (int)y)) {
			mRelocating = true;
		} else {
			resetPickedAction();
		}
	}

	protected String getSelectedCorner() {
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

	@Override
	public void invalidate() {
		super.invalidate();

		if (selection != null) {
			int size = (int)(Math.min(getWidth(), getHeight()) * CORNER_SIZE_PERCENT / 2);
			Rect sel = selection;
			mLeftTopIcon.setBounds(sel.left - size, sel.top - size, sel.left + size, sel.top + size);
			mRightTopIcon.setBounds(sel.right - size, sel.top - size, sel.right + size, sel.top + size);
			mLeftBotIcon.setBounds(sel.left - size, sel.bottom - size, sel.left + size, sel.bottom + size);
			mRightBotIcon.setBounds(sel.right - size, sel.bottom - size, sel.right + size, sel.bottom + size);
		}
	}

	public enum SelectionStatus {
		NORMAL(Color.CYAN),
		FOCUSED(Color.GREEN),
		BLURRY(Color.RED);
		private final int color;

		private SelectionStatus(int color) {
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
