package net.twisterrob.android.content.glide;

import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class PaddingTransformation extends BitmapTransformation {
	private final int padding;

	public PaddingTransformation(Context context, int padding) {
		super(context);
		this.padding = padding;
	}

	@Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth,
			int outHeight) {
		Bitmap result = pool.get(outWidth, outHeight, Config.ARGB_8888);
		if (result == null) {
			result = Bitmap.createBitmap(outWidth, outHeight, Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(result);
		Rect dst = new Rect(0, 0, outWidth, outHeight);
		dst.inset(padding, padding);
		canvas.drawBitmap(toTransform, null, dst, null);
		return result;
	}
	@Override public String getId() {
		return getClass().getSimpleName() + padding;
	}
}
