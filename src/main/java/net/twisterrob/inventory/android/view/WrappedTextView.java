package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

public class WrappedTextView extends TextView {
	public WrappedTextView(Context context) {
		super(context);
	}
	public WrappedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public WrappedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private static final Logger LOG = LoggerFactory.getLogger(WrappedTextView.class);

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LOG.debug("onMeasure({}, {}", MeasureSpec.toString(widthMeasureSpec), MeasureSpec.toString(heightMeasureSpec));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int maxLineWidth = (int)Math.ceil(getMaxLineWidth());
		int width = getCompoundPaddingLeft() + maxLineWidth + getCompoundPaddingRight();

		if (width < getMeasuredWidth()) {
			int mode = MeasureSpec.getMode(widthMeasureSpec);
			super.onMeasure(MeasureSpec.makeMeasureSpec(width, mode), heightMeasureSpec);
		}
	}

	private float getMaxLineWidth() {
		Layout layout = getLayout();
		float max_width = 0.0f;
		int lines = layout.getLineCount();
		for (int i = 0; i < lines; i++) {
			if (layout.getLineWidth(i) > max_width) {
				max_width = layout.getLineWidth(i);
			}
		}
		return max_width;
	}
}
