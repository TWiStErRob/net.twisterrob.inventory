package net.twisterrob.inventory.android.view;

import static java.lang.Math.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.*;
import android.view.*;

import net.twisterrob.inventory.R;

public class SelectionView extends View {
	private static final int MAX_DISTANCE = 50;

	private static final Logger LOG = LoggerFactory.getLogger(SelectionView.class);

	private Drawable mLeftTopIcon;
	private Drawable mRightTopIcon;
	private Drawable mLeftBotIcon;
	private Drawable mRightBotIcon;

	private boolean mLeftTopBool = false;
	private boolean mRightTopBool = false;
	private boolean mLeftBottomBool = false;
	private boolean mRightBottomBool = false;

	// Starting positions of the bounding box
	private Rect selection;

	private float mLastTouchX;
	private float mLastTouchY;

	private Paint line;

	private Rect ignoredRectangle;

	@SuppressLint("InlinedApi")
	private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;

	public SelectionView(Context context) {
		super(context);
		init(context);
	}

	public SelectionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SelectionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		line = new Paint();
		line.setStyle(Style.STROKE);
		line.setColor(Color.CYAN);
		line.setStrokeWidth(15);

		mLeftTopIcon = context.getResources().getDrawable(R.drawable.corner);
		mRightTopIcon = context.getResources().getDrawable(R.drawable.corner);
		mLeftBotIcon = context.getResources().getDrawable(R.drawable.corner);
		mRightBotIcon = context.getResources().getDrawable(R.drawable.corner);
	}

	public Rect getSelection() {
		return selection != null? new Rect(selection) : null;
	}

	public void setSelection(Rect selection) {
		this.selection = selection != null? new Rect(selection) : null;
		invalidate();
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
			//LOG.debug("{}", ev);
		}

		boolean handled = true;
		switch (ev.getActionMasked()) {
			default: {
				handled = false; // let super do what it wants
				break;
			}
			case MotionEvent.ACTION_DOWN: { // first pointer down

				if (ignoredRectangle != null && ignoredRectangle.contains((int)x, (int)y)) {
					handled = false;
					break;
				}

				euclidianDistance(x, y);

				// Remember where we started
				mLastTouchX = x;
				mLastTouchY = y;
				mActivePointerId = ev.getPointerId(pointerIndex);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (ev.getPointerId(pointerIndex) != mActivePointerId) {
					handled = false;
					break;
				}

				float dx = x - mLastTouchX;
				float dy = y - mLastTouchY;

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
				} else {
					selection.offset((int)dx, (int)dy);
				}

				// Remember this touch position for the next move event
				mLastTouchX = x;
				mLastTouchY = y;

				invalidate(); // selection changed, redraw
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: { // last pointer up
				resetPickedCorner();
				selection.sort();
				mActivePointerId = MotionEvent.INVALID_POINTER_ID;
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: { // non-last pointer up
				if (ev.getPointerId(pointerIndex) == mActivePointerId) {
					// this was our active pointer going up, choose a new one
					final int newPointerIndex = (pointerIndex + 1) % ev.getPointerCount();
					mLastTouchX = ev.getX(newPointerIndex);
					mLastTouchY = ev.getY(newPointerIndex);
					mActivePointerId = ev.getPointerId(newPointerIndex);
				}
				break;
			}
		}
		return handled || super.onTouchEvent(ev);
	}

	private boolean hasPickedCorner() {
		return mLeftTopBool || mLeftBottomBool || mRightBottomBool || mRightTopBool;
	}

	private void resetPickedCorner() {
		// when one of these is true, that means it can move when onDraw is called
		mLeftTopBool = false;
		mRightTopBool = false;
		mLeftBottomBool = false;
		mRightBottomBool = false;
	}

	/** Where the screen is pressed, calculate the distance closest to one of the 4 corners
	  * so that it can get the pressed and moved. Only 1 at a time can be moved.
	  */
	private void euclidianDistance(float x, float y) {
		double leftTop = sqrt(pow((abs(x - selection.left)), 2) + pow((abs(y - selection.top)), 2));
		double rightTop = sqrt(pow((abs(x - selection.right)), 2) + pow((abs(y - selection.top)), 2));
		double leftBottom = sqrt(pow((abs(x - selection.left)), 2) + pow((abs(y - selection.bottom)), 2));
		double rightBottom = sqrt(pow((abs(x - selection.right)), 2) + pow((abs(y - selection.bottom)), 2));

		LOG.debug("leftTop: {}, rightTop: {}, leftBottom: {}, rightBottom: {}", //
				leftTop, rightTop, leftBottom, rightBottom);

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
		} else {
			resetPickedCorner();
		}

	}
	public void setIgnoredRectangle(Rect rec) {
		this.ignoredRectangle = rec;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if (selection != null) {
			int size = (int)(Math.min(getWidth(), getHeight()) * 0.025);
			Rect sel = selection;
			mLeftTopIcon.setBounds(sel.left - size, sel.top - size, sel.left + size, sel.top + size);
			mRightTopIcon.setBounds(sel.right - size, sel.top - size, sel.right + size, sel.top + size);
			mLeftBotIcon.setBounds(sel.left - size, sel.bottom - size, sel.left + size, sel.bottom + size);
			mRightBotIcon.setBounds(sel.right - size, sel.bottom - size, sel.right + size, sel.bottom + size);
		}
	}
}
