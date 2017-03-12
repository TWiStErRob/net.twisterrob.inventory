package net.twisterrob.android.content.glide;

import java.lang.reflect.Method;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.*;

public class GlideHelpers {

	public static final BitmapPool NO_POOL = new BitmapPoolAdapter();

	public static DecodeFormat getDefaultFormat(Context context) {
		try {
			Method getDecodeFormat = Glide.class.getDeclaredMethod("getDecodeFormat");
			Glide glide = Glide.get(context);
			return (DecodeFormat)getDecodeFormat.invoke(glide);
		} catch (Exception e) {
			return DecodeFormat.DEFAULT;
		}
	}
}
