package net.twisterrob.android.test;

import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.support.annotation.ColorInt;
import android.text.Layout.Alignment;
import android.text.*;

public class Helpers {
	public static Bitmap createMockBitmap(String text) {
		return createMockBitmap(Color.TRANSPARENT, text);
	}
	public static Bitmap createMockBitmap(@ColorInt int background, String text) {
		return createMockBitmap(512, 512, background, text);
	}
	public static Bitmap createMockBitmap(int width, int height, @ColorInt int background, String text) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		bitmap.eraseColor(background);
		Canvas canvas = new Canvas(bitmap);
		TextPaint paint = new TextPaint();
		paint.setTextSize(bitmap.getHeight() + 1);
		StaticLayout layout;
		do {
			paint.setTextSize(paint.getTextSize() - 1);
			layout = new StaticLayout(text, paint, canvas.getWidth(), Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
		} while (bitmap.getHeight() < layout.getHeight());
		canvas.save();
		canvas.translate((canvas.getWidth() - layout.getWidth()) / 2, (canvas.getHeight() - layout.getHeight()) / 2);
		layout.draw(canvas);
		canvas.restore();
		return bitmap;
	}
}
