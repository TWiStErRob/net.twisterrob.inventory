package net.twisterrob.android.content.glide;

import java.io.*;

import org.slf4j.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.*;

public class SvgBitmapDecoder implements ResourceDecoder<InputStream, Bitmap> {
	private static final Logger LOG = LoggerFactory.getLogger(SvgBitmapDecoder.class);
	private final BitmapPool bitmapPool;
	private final SvgManipulator manipulator;

	public SvgBitmapDecoder(Context context, SvgManipulator manipulator) {
		this(Glide.get(context).getBitmapPool(), manipulator);
	}

	public SvgBitmapDecoder(BitmapPool bitmapPool, SvgManipulator manipulator) {
		this.bitmapPool = bitmapPool;
		this.manipulator = manipulator;
	}

	public Resource<Bitmap> decode(InputStream source, int width, int height) throws IOException {
		try {
			SVG svg = SVGWorkarounds.getFromInputStream(source);
			if (manipulator != null) {
				svg = manipulator.manipulate(svg);
			}
			if (width == Target.SIZE_ORIGINAL && height == Target.SIZE_ORIGINAL) {
				width = (int)svg.getDocumentWidth();
				height = (int)svg.getDocumentHeight();
				if (width <= 0 || height <= 0) {
					RectF viewBox = svg.getDocumentViewBox();
					width = (int)viewBox.width();
					height = (int)viewBox.height();
				}
			} else {
				if (width == Target.SIZE_ORIGINAL) {
					width = (int)(height * svg.getDocumentAspectRatio());
				}
				if (height == Target.SIZE_ORIGINAL) {
					height = (int)(width / svg.getDocumentAspectRatio());
				}
			}
			if (width <= 0 || height <= 0) {
				throw new IllegalArgumentException(
						"Either the Target or the SVG document must declare a size.");
			}

			Bitmap bitmap = findBitmap(width, height);
			Canvas canvas = new Canvas(bitmap);
			svg.renderToCanvas(canvas);
			return BitmapResource.obtain(bitmap, bitmapPool);
		} catch (SVGParseException ex) {
			LOG.warn("Cannot load SVG from stream", ex);
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

	@Override public String getId() {
		return getClass().getSimpleName() + "!" + manipulator.getId();
	}

	public interface SvgManipulator {
		@NonNull SVG manipulate(@NonNull SVG svg);
		@NonNull String getId();
	}
}
