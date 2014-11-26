package net.twisterrob.inventory.android.activity;

import java.util.*;

import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.FloatMath;

/**
 * <pre><code>
 * &lt;View
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:tools="http://schemas.android.com/tools"
 *     android:id="@+id/overlay"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:focusable="false"
 *     android:focusableInTouchMode="false"
 *     tools:background="@color/toolsBG"
 * /&gt;
 * </code></pre>
 * <pre><code>
 * View overlay = findViewById(R.id.overlay);
 * if (overlay != null) {
 *     SnowDrawable snow = new SnowDrawable(30);
 *     overlay.setBackgroundDrawable(snow);
 *     snow.start();
 * }
 * </code></pre>
 */
public class SnowDrawable extends Drawable {
	private final List<Flake> flakes = new ArrayList<>();
	private final Paint fillPaint = new Paint();
	private final Paint strokePaint = new Paint();

	public SnowDrawable(int numFlakes) {
		fillPaint.setStyle(Style.FILL);
		fillPaint.setColor(0xEEEEEEEE);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setColor(0x88888888);

		for (int i = 0; i < numFlakes; ++i) {
			flakes.add(new Flake());
		}
	}

	public void start() {
		new FlakeUpdater(flakes).start();
	}

	@Override public void draw(Canvas canvas) {
		for (Flake flake : flakes) {
			flake.draw(canvas);
		}
	}

	@Override protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		for (Flake flake : flakes) {
			flake.reset(bounds);
		}
	}

	@Override public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override public void setAlpha(int alpha) {
		fillPaint.setAlpha(alpha);
		strokePaint.setAlpha(alpha);
	}

	@Override public void setColorFilter(ColorFilter cf) {
		fillPaint.setColorFilter(cf);
		strokePaint.setColorFilter(cf);
	}

	private class Flake {
		public static final int MIN_SIZE = 10;
		public static final int MAX_SIZE = 70;
		public static final float PI = (float)Math.PI;

		private final float scale = MIN_SIZE + (float)Math.random() * (MAX_SIZE - MIN_SIZE);
		private final float fallingSpeed = 1 + (float)Math.random() * 2; // [1, 3] pixels
		private final float periodSpeed = -PI / 100 + (float)Math.random() * PI / 100; // [-PI, PI]/100 radians

		private final RectF pos = new RectF();
		private float period = 0;

		public void reset(Rect bounds) {
			pos.left = randomLeft(bounds);
			pos.top = randomTop(bounds);
			pos.right = pos.left + scale;
			pos.bottom = pos.top + scale;
		}
		private float randomTop(Rect bounds) {
			return -scale + (float)Math.random() * bounds.height();
		}
		private float randomLeft(Rect bounds) {
			return -scale + (float)Math.random() * bounds.width();
		}

		public void update() {
			period += periodSpeed;
			pos.offset(FloatMath.sin(period), fallingSpeed); // wiggle and fall

			Rect bounds = getBounds();
			if (bounds.height() <= pos.top) { // out at bottom
				pos.offsetTo(pos.left, -scale); // horizontal position, move to top
			}
			if (pos.left <= 0 - pos.width() || bounds.width() <= pos.left) { // fully out at sides
				pos.offsetTo(randomLeft(bounds), -scale); // get it in view, move to top
			}
		}

		public void draw(Canvas canvas) {
			canvas.drawOval(pos, fillPaint);
			canvas.drawOval(pos, strokePaint);
		}
	}

	private class FlakeUpdater implements Runnable {
		private static final int FRAME_DELAY = (int)(1000 / 30.0);
		private final Handler handler = new Handler(Looper.getMainLooper());
		private final List<Flake> flakes;

		public FlakeUpdater(List<Flake> flakes) {
			this.flakes = flakes;
		}

		@Override public void run() {
			for (Flake flake : flakes) {
				flake.update();
			}
			SnowDrawable.this.invalidateSelf();
			start();
		}

		public void start() {
			handler.postDelayed(this, FRAME_DELAY);
		}
	}
}
