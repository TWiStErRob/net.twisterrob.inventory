package net.twisterrob.android.content.glide;

import java.io.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.*;

public class SvgBitmapDecoder implements ResourceDecoder<InputStream, Bitmap> {
	private final BitmapPool bitmapPool;

	public SvgBitmapDecoder(Context context) {
		this(Glide.get(context).getBitmapPool());
	}

	public SvgBitmapDecoder(BitmapPool bitmapPool) {
		this.bitmapPool = bitmapPool;
	}

	public Resource<Bitmap> decode(InputStream source, int width, int height) throws IOException {
		try {
			SVG svg = SVG.getFromInputStream(source);
			if (width == Target.SIZE_ORIGINAL) {
				width = (int)(height * svg.getDocumentAspectRatio());
			}
			if (height == Target.SIZE_ORIGINAL) {
				height = (int)(width / svg.getDocumentAspectRatio());
			}

			Bitmap bitmap = findBitmap(width, height);
			Canvas canvas = new Canvas(bitmap);
			svg.renderToCanvas(canvas);
			return BitmapResource.obtain(bitmap, bitmapPool);
		} catch (SVGParseException ex) {
			throw new IOException("Cannot load SVG from stream", ex);
		}
	}

	private Bitmap findBitmap(int width, int height) {
		Bitmap bitmap = bitmapPool.get(width, height, Config.ARGB_8888);
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		}
		return bitmap;
	}

	@Override
	public String getId() {
		return "";
	}
}
