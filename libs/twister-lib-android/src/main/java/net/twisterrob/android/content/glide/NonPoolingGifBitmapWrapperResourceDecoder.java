package net.twisterrob.android.content.glide;

import android.content.Context;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.gif.GifResourceDecoder;
import com.bumptech.glide.load.resource.gifbitmap.GifBitmapWrapperResourceDecoder;

/**
 * Prevent polluting the bitmap pool with large bitmaps for temporary loads.
 */
public class NonPoolingGifBitmapWrapperResourceDecoder extends GifBitmapWrapperResourceDecoder {

	public NonPoolingGifBitmapWrapperResourceDecoder(Context context) {
		this(context, GlideHelpers.getDefaultFormat(context));
	}

	public NonPoolingGifBitmapWrapperResourceDecoder(Context context, DecodeFormat format) {
		super(
				new NonPoolingImageVideoBitmapDecoder(format),
				new GifResourceDecoder(context, GlideHelpers.NO_POOL),
				GlideHelpers.NO_POOL
		);
	}
}
