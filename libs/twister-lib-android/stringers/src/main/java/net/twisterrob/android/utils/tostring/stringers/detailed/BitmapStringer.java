package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build.*;

import androidx.annotation.NonNull;

import net.twisterrob.android.annotation.Density;
import net.twisterrob.java.utils.tostring.*;

public class BitmapStringer extends Stringer<Bitmap> {
	@TargetApi(VERSION_CODES.KITKAT)
	@Override public void toString(@NonNull ToStringAppender append, Bitmap object) {
		append.beginPropertyGroup("size");
		append.measuredProperty("width", "px", object.getWidth());
		append.measuredProperty("height", "px", object.getHeight());
		append.rawProperty("pixels", object.getWidth() * object.getHeight());
		append.endPropertyGroup();
		append.beginPropertyGroup("memory");
		if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
			append.rawProperty("allocationBytes", object.getAllocationByteCount());
		}
		append.rawProperty("rowBytes", object.getRowBytes());
		if (VERSION_CODES.HONEYCOMB_MR1 <= VERSION.SDK_INT) {
			append.rawProperty("pixelBytes", object.getByteCount());
		}
		if (VERSION_CODES.HONEYCOMB_MR1 <= VERSION.SDK_INT) {
			append.rawProperty("generation", object.getGenerationId());
		}
		append.endPropertyGroup();
		append.beginPropertyGroup("details");
		append.rawProperty("config", object.getConfig());
		append.rawProperty("bpp", getBitsPerPixel(object.getConfig()));
		//noinspection WrongConstant
		append.rawProperty("density", Density.Converter.toString(object.getDensity()));
		append.booleanProperty(object.hasAlpha(), "has alpha", "no alpha");
		if (VERSION_CODES.JELLY_BEAN_MR1 <= VERSION.SDK_INT) {
			append.booleanProperty(object.isPremultiplied(), "premultiplied");
		}
		if (VERSION_CODES.JELLY_BEAN_MR1 <= VERSION.SDK_INT) {
			append.booleanProperty(object.hasMipMap(), "has mipmap", "no mipmap");
		}
		append.endPropertyGroup();
		append.beginPropertyGroup("state");
		append.booleanProperty(object.isMutable(), "mutable");
		append.booleanProperty(object.isRecycled(), "recycled");
		append.endPropertyGroup();
	}

	private static int getBitsPerPixel(Bitmap.Config config) {
		if (config == null) {
			return 0;
		}
		switch (config) {
			case ALPHA_8:
				return 8;
			case ARGB_4444:
				return 16;
			case ARGB_8888:
				return 32;
			case RGB_565:
				return 16;
			default:
				throw new IllegalArgumentException("Unknown bitmap config: " + config);
		}
	}
}
