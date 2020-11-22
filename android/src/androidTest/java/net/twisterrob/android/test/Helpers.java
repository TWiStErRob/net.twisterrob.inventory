package net.twisterrob.android.test;

import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.os.Build.*;
import android.text.Layout.Alignment;
import android.text.*;

import androidx.annotation.*;

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
			layout = renderText(text, paint, canvas.getWidth());
		} while (bitmap.getHeight() < layout.getHeight());
		canvas.save();
		canvas.translate((canvas.getWidth() - layout.getWidth()) / 2, (canvas.getHeight() - layout.getHeight()) / 2);
		layout.draw(canvas);
		canvas.restore();
		return bitmap;
	}

	@SuppressWarnings("deprecation")
	private static @NonNull StaticLayout renderText(String text, TextPaint paint, int width) {
		if (VERSION_CODES.M < VERSION.SDK_INT) {
			return StaticLayout.Builder
					.obtain(text, 0, text.length(), paint, width)
					.setAlignment(Alignment.ALIGN_CENTER)
					.setLineSpacing(0.0f, 1.0f)
					.setIncludePad(false)
					.build();
		} else {
			return new StaticLayout(text, paint, width, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
		}
	}
}
