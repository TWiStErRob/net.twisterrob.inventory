package net.twisterrob.android.graphics;

import static java.lang.Math.*;

import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import net.twisterrob.android.utils.tools.AndroidTools;

public class SunburstDrawable<T> extends Drawable {
	private static final int BASE_LEVEL = 0;
	private static final float RANGE_START = 0;
	private static final float RANGE_END = 1;

	public interface TreeWalker<T> {
		String getLabel(T node, int level);
		Iterable<T> getChildren(T node, int level);
		int getDepth(T subTree);
		float getWeight(T subTree, int level);
	}

	public interface PaintStrategy<T> {
		Paint getFill(T node, int level, float start, float end);
		Paint getStroke(T node, int level, float start, float end);
		TextPaint getText(T node, int level, float start, float end);
		void highlight(Paint fill, Paint stroke, Paint text, Object node, int level, float start, float end);
	}

	private T root;
	private T highlighted;
	private final TreeWalker<T> walker;
	private final PaintStrategy<? super T> paints;

	public SunburstDrawable(TreeWalker<T> walker, PaintStrategy<? super T> paints) {
		this.walker = walker;
		this.paints = paints;
	}

	public T getHighlighted() {
		return highlighted;
	}
	public void setHighlighted(T highlight) {
		this.highlighted = highlight;
		invalidateSelf();
	}

	public T getRoot() {
		return root;
	}
	public void setRoot(T root) {
		this.root = root;
		invalidateSelf();
	}

	@Override public void draw(Canvas canvas) {
		draw(canvas, root, BASE_LEVEL, queryThickness(), RANGE_START, RANGE_END);
	}

	private void draw(Canvas canvas, T subTree, int level, float thickness, float start, float end) {
		float fullWidth = end - start;
		float parentWeight = walker.getWeight(subTree, level);
		float current = 0;
		for (T child : walker.getChildren(subTree, level)) {
			float relativeWeight = walker.getWeight(child, level + 1) / parentWeight;
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
		Paint text = paints.getText(subTree, level, start, end);
		if (subTree.equals(highlighted)) {
			paints.highlight(fill, stroke, text, subTree, level, start, end);
		}

		if (fill != null && stroke != null) {
			if (4 < sweepAngle) {
				AndroidTools.drawArcSegment(canvas, cx, cy, rIn, rOut, startAngle, sweepAngle, fill, stroke);
			} else {
				float midAngle = (float)toRadians(startAngle + sweepAngle / 2);
				float inX = cx + rIn * (float)cos(midAngle);
				float inY = cy + rIn * (float)sin(midAngle);
				float outX = cx + rOut * (float)cos(midAngle);
				float outY = cy + rOut * (float)sin(midAngle);
				float oldWidth = fill.getStrokeWidth();
				Paint.Style oldStyle = fill.getStyle();
				float width = (rIn + rOut) / 2 * (float)toRadians(sweepAngle);
				fill.setStrokeWidth(width);
				fill.setStyle(Paint.Style.STROKE);
				canvas.drawLine(inX, inY, outX, outY, fill);
				fill.setStrokeWidth(oldWidth);
				fill.setStyle(oldStyle);
			}
		}

		String label = walker.getLabel(subTree, level);
		if (text != null && label != null) {
			if (text.measureText(label) / 2 < toRadians(sweepAngle) * (rIn + rOut) / 2) {
				AndroidTools.drawTextOnArc(canvas, label, cx, cy, rIn, rOut, startAngle, sweepAngle, text);
			}
		}
	}

	public T at(float x, float y) {
		if (root == null) {
			return null;
		}
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
		float parentWeight = walker.getWeight(subTree, level);

		Rect bounds = getBounds();
		float size = Math.min(bounds.width(), bounds.height());
		float rIn = (level * thickness) * size / 2;
		float rOut = ((level + 1) * thickness) * size / 2;

		if (rIn <= r && r <= rOut && start <= alpha && alpha <= end) {
			return subTree; // TODO invert conditions to cut subtrees from search
		}

		for (T child : walker.getChildren(subTree, level)) {
			float relativeWeight = walker.getWeight(child, level) / parentWeight;
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

	@Override public int getIntrinsicHeight() {
		return 500;
	}
	@Override public int getIntrinsicWidth() {
		return 500;
	}

	@Override public void setAlpha(int alpha) {
		setAlpha(paints.getFill(root, 0, 0, 1), alpha);
		setAlpha(paints.getStroke(root, 0, 0, 1), alpha);
		setAlpha(paints.getText(root, 0, 0, 1), alpha);
		invalidateSelf();
	}

	@Override public void setColorFilter(ColorFilter cf) {
		setColorFilter(paints.getFill(root, 0, 0, 1), cf);
		setColorFilter(paints.getStroke(root, 0, 0, 1), cf);
		setColorFilter(paints.getText(root, 0, 0, 1), cf);
		invalidateSelf();
	}

	@SuppressWarnings("deprecation") // Unused from API 29, but lower still uses it.
	@Override public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
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
		protected final TextPaint text = new TextPaint();

		public BasePaintStrategy() {
			fill.setAntiAlias(true);
			fill.setStyle(Paint.Style.FILL);

			stroke.setAntiAlias(true);
			stroke.setStyle(Paint.Style.STROKE);
			stroke.setStrokeWidth(1);

			text.setTextAlign(Align.CENTER);
			text.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		}
	}

	@SuppressWarnings("unused")
	public static class RandomColorPaints extends BasePaintStrategy<Object> {
		private final float[] color = new float[] {0, 0.5f, 1.0f};
		private final float[] textColor = new float[] {0, 1.0f, 0.5f};

		private static <P extends Paint> P randomize(P paint, float... color) {
			color[0] = (float)(Math.random() * 360);
			paint.setColor(Color.HSVToColor(color));
			return paint;
		}

		@Override public Paint getFill(Object node, int level, float start, float end) {
			return randomize(fill, color);
		}

		@Override public Paint getStroke(Object node, int level, float start, float end) {
			return randomize(stroke, color);
		}

		@Override public TextPaint getText(Object node, int level, float start, float end) {
			return randomize(text, textColor);
		}

		@Override public void highlight(Paint fill, Paint stroke, Paint text, Object node, int level, float start,
				float end) {
			randomize(fill, color);
			randomize(stroke, color);
			randomize(text, textColor);
		}
	}

	@SuppressWarnings("unused")
	public static class HueVaryingPaints extends BasePaintStrategy<Object> {
		private final float[] hsv = new float[] {0.0f, 0.5f, 1.0f};

		private <P extends Paint> P hue(P paint, float start, float end, boolean opposite) {
			hsv[0] = (start + end) / 2 * 360;
			if (opposite) {
				hsv[0] = (hsv[0] + 180) % 360;
			}
			paint.setColor(Color.HSVToColor(hsv));
			return paint;
		}

		@Override public Paint getFill(Object node, int level, float start, float end) {
			return hue(fill, start, end, false);
		}

		@Override public Paint getStroke(Object node, int level, float start, float end) {
			return hue(stroke, start, end, false);
		}

		@Override public TextPaint getText(Object node, int level, float start, float end) {
			return hue(text, start, end, true);
		}

		@Override public void highlight(Paint fill, Paint stroke, Paint text, Object node, int level, float start,
				float end) {
			fill.setColor(Color.WHITE);
			stroke.setColor(Color.BLACK);
			text.setColor(Color.BLACK);
		}
	}
}
