package net.twisterrob.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RigidImageView extends ImageView {
	//private static final Logger LOG = LoggerFactory.getLogger(RigidImageView.class);

	public RigidImageView(Context context) {
		super(context);
	}
	public RigidImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public RigidImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private boolean eatRequestLayout = false;

	@Override public void setImageDrawable(Drawable drawable) {
		eatRequestLayout = true;
		//LOG.trace("Setting drawable from {} to {}", getDrawable(), drawable);
		super.setImageDrawable(drawable);
		eatRequestLayout = false;
	}

	@Override public void requestLayout() {
		if (eatRequestLayout) {
			//LOG.trace("Consumed requestLayout().");
		} else {
			//LOG.warn("Passing through to super.requestLayout().");
			super.requestLayout();
		}
	}

	// TODEL EmptyMethod: https://youtrack.jetbrains.com/issue/IDEA-154073
	@SuppressWarnings("EmptyMethod")
	@Override public void setImageBitmap(Bitmap bm) {
		// Calls through to setImageDrawable since Eclair
		super.setImageBitmap(bm);
	}

	@Override public void setImageResource(int resId) {
		throw new UnsupportedOperationException("Use setImageDrawable");
	}

	@Override public void setImageURI(Uri uri) {
		throw new UnsupportedOperationException("Use setImageDrawable");
	}
}
