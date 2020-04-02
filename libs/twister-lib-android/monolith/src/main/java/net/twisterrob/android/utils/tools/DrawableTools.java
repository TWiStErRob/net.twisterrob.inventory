package net.twisterrob.android.utils.tools;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

@SuppressWarnings("unused")
public /*static*/ abstract class DrawableTools {
	public static Bitmap toBitmap(int shapeID, int widthID, int heightID, Resources res) {
		Drawable shape = ResourcesCompat.getDrawable(res, shapeID, null);
		return toBitmap(shape, widthID, heightID, res);
	}
	public static Bitmap toBitmap(Drawable shape, int widthID, int heightID, Resources res) {
		int width = res.getDimensionPixelSize(widthID);
		int height = res.getDimensionPixelSize(heightID);
		return toBitmap(shape, width, height);
	}
	public static Bitmap toBitmap(Drawable shape, int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		shape.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
		shape.draw(canvas);
		return bitmap;
	}

	protected DrawableTools() {
		// static utility class
	}
}
