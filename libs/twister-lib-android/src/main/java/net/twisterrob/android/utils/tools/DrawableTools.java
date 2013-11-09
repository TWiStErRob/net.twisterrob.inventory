package net.twisterrob.android.utils.tools;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;

public class DrawableTools {
	public static Bitmap toBitmap(int shapeID, int widthID, int heightID, Resources res) {
		Drawable shape = res.getDrawable(shapeID);
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
}
