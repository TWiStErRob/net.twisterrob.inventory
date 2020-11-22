package net.twisterrob.android.content.glide;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.*;

import androidx.annotation.*;

import net.twisterrob.android.utils.tools.ResourceTools;

public class RawResourceSVGExternalFileResolver
		extends SVGExternalFileResolver
		implements SvgBitmapDecoder.SvgManipulator {

	private static final String SVG_SUFFIX = ".svg";

	private final @NonNull Context context;
	private final @NonNull BitmapPool bitmapPool;

	public RawResourceSVGExternalFileResolver(
			@NonNull Context context, @NonNull BitmapPool bitmapPool) {
		this.context = context;
		this.bitmapPool = bitmapPool;
	}

	@Override public @NonNull SVG manipulate(@NonNull SVG svg) {
		svg.registerExternalFileResolver(this);
		return svg;
	}

	@Override public @Nullable Bitmap resolveImage(@NonNull String filename) {
		try {
			@RawRes int resId = resolveRawResourceId(filename);
			InputStream resStream = context.getResources().openRawResource(resId);
			ResourceDecoder<InputStream, Bitmap> decoder =
					new SvgBitmapDecoder(bitmapPool, RawResourceSVGExternalFileResolver.this);
			return decoder.decode(resStream, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected @RawRes int resolveRawResourceId(@NonNull String filename) {
		String resName = filename;
		if (filename.endsWith(SVG_SUFFIX)) {
			resName = filename.substring(0, filename.length() - SVG_SUFFIX.length());
		}
		return ResourceTools.getRawResourceID(context, resName);
	}

	@Override public @NonNull String getId() {
		return getClass().getSimpleName();
	}
}
