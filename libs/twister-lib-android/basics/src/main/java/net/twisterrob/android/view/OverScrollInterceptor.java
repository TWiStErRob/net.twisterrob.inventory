package net.twisterrob.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.*;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Intercepts vertical scroll events for the inside view and notifies the attached listener about over-scroll.
 * When used with SwipeRefreshLayout wrap that in this one because SRL steals events and this is pass-through.
 */
public class OverScrollInterceptor extends ViewGroup {
	private OnOverScrollListener mScrollListener;

	public interface OnOverScrollListener {
		void overScroll(OverScrollInterceptor view, float overScrollY);
		void reset(OverScrollInterceptor view);
	}

	private View mTarget;
	private int mActivePointerId;
	private float mInitialMotionY;

	public OverScrollInterceptor(Context context) {
		this(context, null);
	}
	public OverScrollInterceptor(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnOverScrollListener(OnOverScrollListener listener) {
		mScrollListener = listener;
	}

	private void ensureTarget() {
		if (mTarget == null) {
			mTarget = getChildAt(0);
		}
	}

	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (getChildCount() != 0) {
			ensureTarget();
			if (mTarget != null) {
				View child = mTarget;
				int childLeft = getPaddingLeft();
				int childTop = getPaddingTop();
				int childWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
				int childHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
				child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
			}
		}
	}

	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		ensureTarget();
		if (mTarget != null) {
			measureChild(mTarget, widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(mTarget.getMeasuredWidth(), mTarget.getMeasuredHeight());
		}
	}

	private boolean canChildScrollUp() {
		ensureTarget();
		if (mTarget instanceof SwipeRefreshLayout) {
			return ((SwipeRefreshLayout)mTarget).canChildScrollUp();
		}
		return mTarget.canScrollVertically(-1);
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = ev.getPointerId(0);
				mInitialMotionY = ev.getY();
				break;
			case MotionEvent.ACTION_POINTER_DOWN: {
				float oldY = ev.getY(ev.findPointerIndex(mActivePointerId));
				mActivePointerId = ev.getPointerId(ev.getActionIndex());
				float newY = ev.getY(ev.getActionIndex());
				mInitialMotionY += newY - oldY;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				float y = ev.getY(ev.findPointerIndex(mActivePointerId));
				if (canChildScrollUp()) { // this will result in saving the last y when the child was moving
					mInitialMotionY = y;
					mScrollListener.reset(this);
					break;
				}
				float overScrollTop = (y - mInitialMotionY);
				if (0 <= overScrollTop && mScrollListener != null) {
					mScrollListener.overScroll(this, overScrollTop);
				}
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: {
				int pointerIndex = ev.getActionIndex();
				int pointerId = ev.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId) {
					int newPointerIndex = pointerIndex == 0? 1 : 0;
					mActivePointerId = ev.getPointerId(newPointerIndex);
				}
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				mActivePointerId = -1;
				mScrollListener.reset(this);
				break;
			}
			case MotionEvent.ACTION_OUTSIDE:
			default:
				break;
		}

		return false;
	}
}

/*
setOnScrollListener(new OnOverScrollListener() {
	int originalHeight = Integer.MIN_VALUE;
	@Override public void overScroll(OverScrollInterceptor view, float overScrollY) {
		ensureOriginal();
		setHeight((int)Math.min(originalHeight + overScrollY, originalHeight * 2));
	}
	@Override public void reset(OverScrollInterceptor view) {
		ensureOriginal();
		setHeight(originalHeight);
	}
	private void ensureOriginal() {
		if (originalHeight == Integer.MIN_VALUE) {
			originalHeight = header.getContainer().getLayoutParams().height;
			//Layout anim is very laggy
			//LayoutTransition transition = new LayoutTransition();
			//transition.enableTransitionType(LayoutTransition.CHANGING);
			//header.getContainer().setLayoutTransition(transition);
		}
	}
	private void setHeight(int originalHeight) {
		LayoutParams params = header.getContainer().getLayoutParams();
		params.height = originalHeight;
		header.getContainer().setLayoutParams(params);
	}
});
 */