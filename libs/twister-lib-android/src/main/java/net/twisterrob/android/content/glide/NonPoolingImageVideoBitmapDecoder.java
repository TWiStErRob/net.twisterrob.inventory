package net.twisterrob.android.content.glide;

import android.content.Context;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.*;

/**
 * Prevent polluting the bitmap pool with large bitmaps for temporary loads.
 */
public class NonPoolingImageVideoBitmapDecoder extends ImageVideoBitmapDecoder {

	public NonPoolingImageVideoBitmapDecoder(Context context) {
		this(GlideHelpers.getDefaultFormat(context));
	}

	public NonPoolingImageVideoBitmapDecoder(DecodeFormat format) {
		super(
				new StreamBitmapDecoder(GlideHelpers.NO_POOL, format),
				new FileDescriptorBitmapDecoder(GlideHelpers.NO_POOL, format)
		);
	}
}
