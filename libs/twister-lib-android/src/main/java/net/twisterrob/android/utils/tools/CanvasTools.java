package net.twisterrob.android.utils.tools;

import android.graphics.*;

public class CanvasTools {
	public static void drawTriangle(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3,
			Paint paint) {
		Path path = new Path();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.close();

		canvas.drawPath(path, paint);
	}

	protected CanvasTools() {
		// static utility class
	}
}
