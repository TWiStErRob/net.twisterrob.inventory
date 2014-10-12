package net.twisterrob.inventory.android.view.lib;

import static java.lang.Math.*;

import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;

import net.twisterrob.android.utils.tools.AndroidTools;

public class SunburstDrawable<T> extends Drawable {
	private static final int BASE_LEVEL = 0;
	private static final float RANGE_START = 0;
	private static final float RANGE_END = 1;

	public static interface TreeWalker<T> {
		String getLabel(T node);
		Iterable<T> getChildren(T node);
		int getDepth(T subTree);
		float getWeight(T subTree);
	}

	public static interface PaintStrategy<T> {
		Paint getFill(T node, int level, float start, float end);
		Paint getStroke(T node, int level, float start, float end);
		Paint getText(T node, int level, float start, float end);
	}

	private T root;
	private T highlight;
	private final TreeWalker<T> walker;
	private final PaintStrategy<? super T> paints;

	public SunburstDrawable(TreeWalker<T> walker, PaintStrategy<? super T> paints) {
		this.walker = walker;
		this.paints = paints;
	}

	public T getHighlight() {
		return highlight;
	}
	public void setHighlight(T highlight) {
		this.highlight = highlight;
		invalidateSelf();
	}

	public T getRoot() {
		return root;
	}
	public void setRoot(T root) {
		this.root = root;
		invalidateSelf();
	}

	@Override
	public void draw(Canvas canvas) {
		draw(canvas, root, BASE_LEVEL, queryThickness(), RANGE_START, RANGE_END);
	}

	private void draw(Canvas canvas, T subTree, int level, float thickness, float start, float end) {
		float fullWidth = end - start;
		float parentWeight = walker.getWeight(subTree);
		float current = 0;
		for (T child : walker.getChildren(subTree)) {
			float relativeWeight = walker.getWeight(child) / parentWeight;
			float next = current + relativeWeight;
			float currentStart = start + current * fullWidth;
			float currentEnd = start + next * fullWidth;

			draw(canvas, child, level + 1, thickness, currentStart, currentEnd);
			current = next;
		}

		drawCurrent(canvas, subTree, level, thickness, start, end);
	}

	private void drawCurrent(Canvas canvas, T subTree, int level, float thickness, float start, float end) {
		Rect bounds = getBounds();
		float cx = bounds.exactCenterX();
		float cy = bounds.exactCenterY();
		float size = Math.min(bounds.width(), bounds.height());
		float rIn = ((level) * thickness) * size / 2;
		float rOut = ((level + 1) * thickness) * size / 2;

		float startAngle = start * 360;
		float sweepAngle = (end - start) * 360;

		Paint fill = paints.getFill(subTree, level, start, end);
		Paint stroke = paints.getStroke(subTree, level, start, end);
		if (fill != null || stroke != null) {
			if (subTree.equals(highlight)) {
				fill.setColor(Color.WHITE);
			}
			AndroidTools.drawArcSegment(canvas, cx, cy, rIn, rOut, startAngle, sweepAngle, fill, stroke);
		}
		Paint text = paints.getText(subTree, level, start, end);
		String label = walker.getLabel(subTree);
		if (text != null && label != null) {
			if (subTree.equals(highlight)) {
				text.setColor(Color.BLACK);
			}
			AndroidTools.drawTextOnArc(canvas, label, cx, cy, rIn, rOut, startAngle, sweepAngle, text);
		}
	}

	public T at(float x, float y) {
		Rect bounds = getBounds();
		float cx = bounds.exactCenterX();
		float cy = bounds.exactCenterY();

		float dx = x - cx;
		float dy = y - cy;
		float r = (float)sqrt(dx * dx + dy * dy);
		float alpha = ((float)toDegrees(atan2(dy, dx)) + 360) % 360;
		return find(root, r, alpha / 360.0f, BASE_LEVEL, queryThickness(), RANGE_START, RANGE_END);
	}

	private T find(T subTree, float r, float alpha, int level, float thickness, float start, float end) {
		float fullWidth = end - start;
		float current = 0;
		float parentWeight = walker.getWeight(subTree);

		Rect bounds = getBounds();
		float size = Math.min(bounds.width(), bounds.height());
		float rIn = (level * thickness) * size / 2;
		float rOut = ((level + 1) * thickness) * size / 2;

		if (rIn <= r && r <= rOut && start <= alpha && alpha <= end) {
			return subTree; // TODO invert conditions to cut subtrees from search
		}

		for (T child : walker.getChildren(subTree)) {
			float relativeWeight = walker.getWeight(child) / parentWeight;
			float next = current + relativeWeight;
			float currentStart = start + current * fullWidth;
			float currentEnd = start + next * fullWidth;

			T result = find(child, r, alpha, level + 1, thickness, currentStart, currentEnd);
			if (result != null) {
				return result;
			}
			current = next;
		}
		return null;
	}

	private float queryThickness() {
		int levels = walker.getDepth(root);
		return 1.0f / (levels + 1); // +1 = middle gap
	}

	@Override
	public int getIntrinsicHeight() {
		return 500;
	}
	@Override
	public int getIntrinsicWidth() {
		return 500;
	}

	@Override
	public void setAlpha(int alpha) {
		setAlpha(paints.getFill(root, 0, 0, 1), alpha);
		setAlpha(paints.getStroke(root, 0, 0, 1), alpha);
		setAlpha(paints.getText(root, 0, 0, 1), alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		setColorFilter(paints.getFill(root, 0, 0, 1), cf);
		setColorFilter(paints.getStroke(root, 0, 0, 1), cf);
		setColorFilter(paints.getText(root, 0, 0, 1), cf);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return 1;
	}

	private static void setAlpha(Paint paint, int alpha) {
		if (paint != null) {
			paint.setAlpha(alpha);
		}
	}

	private static void setColorFilter(Paint paint, ColorFilter cf) {
		if (paint != null) {
			paint.setColorFilter(cf);
		}
	}

	public static abstract class BasePaintStrategy<T> implements PaintStrategy<T> {
		protected final Paint fill = new Paint();
		protected final Paint stroke = new Paint();
		protected final Paint text = new Paint();

		public BasePaintStrategy() {
			fill.setStyle(Paint.Style.FILL);
			fill.setAntiAlias(true);
			stroke.setStyle(Paint.Style.STROKE);
			stroke.setAntiAlias(true);
			stroke.setStrokeWidth(1);
			text.setTextAlign(Align.CENTER);
		}
	}

	public static class RandomColorPaints extends BasePaintStrategy<Object> {
		private final float[] color = new float[]{0, 0.5f, 1.0f};
		private final float[] textColor = new float[]{0, 1.0f, 0.5f};

		private static Paint randomize(Paint paint, float[] color) {
			color[0] = (float)(Math.random() * 360);
			paint.setColor(Color.HSVToColor(color));
			return paint;
		}

		public Paint getFill(Object node, int level, float start, float end) {
			return randomize(fill, color);
		}

		public Paint getStroke(Object node, int level, float start, float end) {
			return randomize(stroke, color);
		}

		public Paint getText(Object node, int level, float start, float end) {
			return randomize(text, textColor);
		}
	}

	public static class HueVaryingPaints extends BasePaintStrategy<Object> {
		private final float[] hsv = new float[]{0.0f, 0.5f, 1.0f};

		private Paint hue(Paint paint, float start, float end, boolean opposite) {
			hsv[0] = (start + end) / 2 * 360;
			if (opposite) {
				hsv[0] = (hsv[0] + 180) % 360;
			}
			paint.setColor(Color.HSVToColor(hsv));
			return paint;
		}

		public Paint getFill(Object node, int level, float start, float end) {
			return hue(fill, start, end, false);
		}

		public Paint getStroke(Object node, int level, float start, float end) {
			return hue(stroke, start, end, false);
		}

		public Paint getText(Object node, int level, float start, float end) {
			return hue(text, start, end, true);
		}
	}
}
