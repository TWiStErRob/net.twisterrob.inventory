package net.twisterrob.inventory.android.sunburst;

import android.graphics.*;
import android.text.TextPaint;

import net.twisterrob.android.graphics.SunburstDrawable.BasePaintStrategy;

class Paints extends BasePaintStrategy<Node> {
	private static final float[] TEMP_HSV = new float[] {0.0f, 0.5f, 1.0f};

	private static Paint hue(Paint paint, float start, float end) {
		TEMP_HSV[0] = (start + end) / 2 * 360;
		paint.setColor(Color.HSVToColor(TEMP_HSV));
		return paint;
	}

	@Override public Paint getFill(Node node, int level, float start, float end) {
		return hue(fill, start, end);
	}

	@Override public Paint getStroke(Node node, int level, float start, float end) {
		stroke.setARGB(128, 128, 128, 128);
		stroke.setStrokeWidth(1);
		return stroke;
	}

	@Override public TextPaint getText(Node node, int level, float start, float end) {
		text.setARGB(255, 0, 0, 0);
		return text;
	}

	@Override public void highlight(Paint fill, Paint stroke, Paint text, Object node, int level, float start,
			float end) {
		hue(stroke, start, end);
		stroke.setStrokeWidth(3);
		fill.setColor(Color.WHITE);
		text.setColor(Color.BLACK);
	}
}
