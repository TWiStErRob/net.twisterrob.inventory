package net.twisterrob.android.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.*;
import android.view.View.*;

import androidx.annotation.*;

/**
 * A class, that can be used as a TouchListener on any view (e.g. a Button).
 * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
 * click is fired immediately, next one after the initialInterval, and subsequent
 * ones after the normalInterval.
 *
 * <p>Interval is scheduled after the onClick completes, so it has to run fast.
 * If it runs slow, it does not generate skipped onClicks. Can be rewritten to
 * achieve this.
 *
 * @see <a href="http://stackoverflow.com/a/12795551/253468">Android - Hold Button to Repeat Action</a>
 */
public class RepeatListener implements OnTouchListener {
	private final Handler handler = new Handler();
	private final Runnable handlerRunnable = new Runnable() {
		@Override public void run() {
			handler.postDelayed(this, repeatInterval);
			clickListener.onClick(downView);
		}
	};
	/**
	 * @see <a href="http://stackoverflow.com/questions/4284224/android-hold-button-to-repeat-action#comment64450786_12795551">Why this is better.</a>
	 */
	private static final OnClickListener PERFORM_CLICK = new OnClickListener() {
		@Override public void onClick(View v) {
			v.performClick();
		}
	};

	private final int initialInterval;
	private final int repeatInterval;
	private OnClickListener clickListener;

	private View downView;

	/**
	 * Creates a repeater with sensible defaults, that uses the actual click listener.
	 */
	public RepeatListener() {
		this(400, 100, PERFORM_CLICK);
	}

	/**
	 * Uses the actual click listener.
	 * @param initialInterval The interval after first click event
	 * @param repeatInterval The interval after second and subsequent click events
	 */
	public RepeatListener(@IntRange(from = 1) int initialInterval, @IntRange(from = 1) int repeatInterval) {
		this(initialInterval, repeatInterval, PERFORM_CLICK);
	}

	/**
	 * Creates a repeater with sensible defaults.
	 */
	public RepeatListener(OnClickListener clickListener) {
		this(400, 100, clickListener);
	}

	/**
	 * @param initialInterval The interval after first click event
	 * @param repeatInterval The interval after second and subsequent click events
	 * @param clickListener The OnClickListener, that will be called periodically
	 */
	public RepeatListener(
			@IntRange(from = 1) int initialInterval,
			@IntRange(from = 1) int repeatInterval,
			@NonNull OnClickListener clickListener) {
		//noinspection ConstantConditions still need to validate
		if (clickListener == null) {
			throw new IllegalArgumentException("null listener");
		}
		if (initialInterval < 0 || repeatInterval < 0) {
			throw new IllegalArgumentException(
					"need a non-negative interval: initial=" + initialInterval + "repeat=" + repeatInterval);
		}

		this.initialInterval = initialInterval;
		this.repeatInterval = repeatInterval;
		this.clickListener = clickListener;
	}

	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handler.removeCallbacks(handlerRunnable);
				handler.postDelayed(handlerRunnable, initialInterval);
				downView = view;
				downView.setPressed(true);
				clickListener.onClick(view); // the default listener is to call view.performClick()
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				handler.removeCallbacks(handlerRunnable);
				downView.setPressed(false);
				downView = null;
				return true;
		}
		return false;
	}
}
