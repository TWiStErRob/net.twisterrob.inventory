package net.twisterrob.inventory.android.content.io;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.RawRes;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.*;

import net.twisterrob.android.content.glide.SvgBitmapDecoder;
import net.twisterrob.android.content.glide.SvgBitmapDecoder.SvgManipulator;
import net.twisterrob.android.utils.tools.AndroidTools;

public class RawResourceResolver extends SVGExternalFileResolver implements SvgManipulator {
	private static final String SVG_SUFFIX = ".svg";
	private final Context context;
	private final BitmapPool bitmapPool;

	public RawResourceResolver(Context context, BitmapPool bitmapPool) {
		this.context = context;
		this.bitmapPool = bitmapPool;
	}

	@Override public SVG manipulate(SVG svg) {
		svg.registerExternalFileResolver(this);
		return svg;
	}

	@Override public Bitmap resolveImage(String filename) {
		try {
			String resName = filename;
			if (filename.endsWith(SVG_SUFFIX)) {
				resName = filename.substring(0, filename.length() - SVG_SUFFIX.length());
			}
			@RawRes int resId = AndroidTools.getRawResourceID(context, resName);
			InputStream resStream = context.getResources().openRawResource(resId);
			SvgBitmapDecoder decoder = new SvgBitmapDecoder(bitmapPool, RawResourceResolver.this);
			return decoder.decode(resStream, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override public String toString() {
		return getClass().getSimpleName();
	}
}
