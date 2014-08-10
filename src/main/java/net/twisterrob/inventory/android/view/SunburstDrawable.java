package net.twisterrob.inventory.android.view;

import static java.lang.Math.*;

import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;

import net.twisterrob.android.utils.tools.AndroidTools;

public class SunburstDrawable<T> extends Drawable {
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

	@Override
	public void draw(Canvas canvas) {
		if (walker.getChildren(root).iterator().hasNext()) {
			draw(canvas, root, 1, queryThickness(), 0, 1);
		} else {
			Rect bounds = getBounds();
			float size = Math.min(bounds.width(), bounds.height());
			float cx = bounds.exactCenterX();
			float cy = bounds.exactCenterY();
			Paint fill = paints.getFill(root, 0, 0, 1);
			Paint text = paints.getText(root, 0, 0, 1);
			canvas.drawCircle(cx, cy, size / 2, fill);
			canvas.drawText(walker.getLabel(root), cx, cy, text);
		}
	}

	private void draw(Canvas canvas, T subTree, int level, float thickness, float start, float end) {
		float fullWidth = end - start;
		float current = 0;
		float parentWeight = walker.getWeight(subTree);

		Rect bounds = getBounds();
		float cx = bounds.exactCenterX();
		float cy = bounds.exactCenterY();
		float size = Math.min(bounds.width(), bounds.height());
		float rIn = (level * thickness) * size / 2;
		float rOut = ((level + 1) * thickness) * size / 2;

		for (T child: walker.getChildren(subTree)) {
			float relativeWeight = walker.getWeight(child) / parentWeight;
			float next = current + relativeWeight;
			float currentStart = start + current * fullWidth;
			float currentEnd = start + next * fullWidth;

			float startAngle = -currentStart * 360;
			float sweepAngle = -(currentEnd - currentStart) * 360;

			float myIn = rIn;
			float myOut = rOut;

			Paint fill = paints.getFill(child, level, currentStart, currentEnd);
			Paint stroke = paints.getStroke(child, level, currentStart, currentEnd);
			if (fill != null || stroke != null) {
				if (child.equals(highlight)) {
					//					myIn *= 0.95;
					//					myOut *= 1.10;
					//					sweepAngle += (startAngle * 0.20f); // + 2 * 10%
					//					startAngle *= 0.90;
					fill.setColor(Color.WHITE);
				}
				AndroidTools.drawArcSegment(canvas, cx, cy, myIn, myOut, startAngle, sweepAngle, fill, stroke);
			}
			Paint text = paints.getText(child, level, currentStart, currentEnd);
			if (text != null) {
				AndroidTools.drawTextOnArc(canvas, walker.getLabel(child), cx, cy, myIn, myOut, startAngle, sweepAngle,
						text);
				if (child.equals(highlight)) {
					text.setColor(Color.BLACK);
				}
			}

			draw(canvas, child, level + 1, thickness, currentStart, currentEnd);
			current = next;
		}
	}

	public T at(float x, float y) {
		Rect bounds = getBounds();
		float cx = bounds.exactCenterX();
		float cy = bounds.exactCenterY();

		float r = (float)sqrt(pow(cx - x, 2) + pow(cy - y, 2));
		float alpha = 180 - (float)toDegrees(atan2(cy - y, cx - x));
		return find(root, r, alpha / 360.0f, 0, queryThickness(), 0, 1);
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

		for (T child: walker.getChildren(subTree)) {
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
		float thick = 1.0f / (levels + 1); // +1 = middle gap
		return thick;
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

		private Paint randomize(Paint paint, float[] color) {
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
