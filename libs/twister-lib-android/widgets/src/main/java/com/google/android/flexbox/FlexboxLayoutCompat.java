package com.google.android.flexbox;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.ViewGroup;

@RequiresApi(VERSION_CODES.BASE)
@TargetApi(VERSION_CODES.O_MR1)
@SuppressWarnings("DefaultAnnotationParam")
public class FlexboxLayoutCompat extends FlexboxLayout {

	public FlexboxLayoutCompat(Context context) {
		super(context);
	}
	public FlexboxLayoutCompat(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public FlexboxLayoutCompat(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public int getPaddingStart() {
		return 0; //ViewCompat.getPaddingStart(this); // STOPSHIP
	}

	@Override
	public int getPaddingEnd() {
		return 0; //ViewCompat.getPaddingEnd(this); // STOPSHIP
	}

	@Override
	public FlexboxLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new FlexboxLayoutCompat.LayoutParams(getContext(), attrs);
	}

	public static class LayoutParams extends FlexboxLayout.LayoutParams {

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		public LayoutParams(FlexboxLayout.LayoutParams source) {
			super(source);
		}
		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
		public LayoutParams(int width, int height) {
			super(width, height);
		}
		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}
		protected LayoutParams(Parcel in) {
			super(in);
		}

		@Override public int getMarginStart() {
			return 0; //MarginLayoutParamsCompat.getMarginStart(this); // STOPSHIP
		}

		@Override public int getMarginEnd() {
			return 0; //MarginLayoutParamsCompat.getMarginEnd(this); // STOPSHIP
		}
	}
}
