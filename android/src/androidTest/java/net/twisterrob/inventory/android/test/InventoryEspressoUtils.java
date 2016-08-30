package net.twisterrob.inventory.android.test;

import java.io.*;

import org.hamcrest.Matcher;

import static org.junit.Assume.*;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Bitmap.*;
import android.net.Uri;
import android.text.Layout.Alignment;
import android.text.*;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;

import net.twisterrob.android.activity.CaptureImage;

public class InventoryEspressoUtils {
	public static Matcher<Intent> intendCamera(File file, String mockText) throws IOException {
		Bitmap bitmap = createMockBitmap(512, 512, mockText);
		Uri resultFile = Uri.fromFile(file);
		OutputStream output = getContext().getContentResolver().openOutputStream(resultFile);
		assumeTrue(bitmap.compress(CompressFormat.PNG, 100, output));
		Matcher<Intent> expectedIntent = hasComponent(CaptureImage.class.getName());
		intending(expectedIntent)
				.respondWith(new ActivityResult(Activity.RESULT_OK, new Intent(null, resultFile)));
		return expectedIntent;
	}

	public static Bitmap createMockBitmap(int width, int height, String text) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
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
