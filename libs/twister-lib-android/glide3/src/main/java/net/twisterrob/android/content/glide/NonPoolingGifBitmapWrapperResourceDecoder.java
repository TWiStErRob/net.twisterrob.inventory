package net.twisterrob.android.content.glide;

import android.content.Context;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.gif.GifResourceDecoder;
import com.bumptech.glide.load.resource.gifbitmap.GifBitmapWrapperResourceDecoder;

import androidx.annotation.NonNull;

/**
 * Prevent polluting the bitmap pool with large bitmaps for temporary loads.
 */
public class NonPoolingGifBitmapWrapperResourceDecoder extends GifBitmapWrapperResourceDecoder {

	public NonPoolingGifBitmapWrapperResourceDecoder(@NonNull Context context) {
		this(context, GlideHelpers.getDefaultFormat(context));
	}

	public NonPoolingGifBitmapWrapperResourceDecoder(@NonNull Context context, @NonNull DecodeFormat format) {
		super(
				new NonPoolingImageVideoBitmapDecoder(format),
				new GifResourceDecoder(context, GlideHelpers.NO_POOL),
				GlideHelpers.NO_POOL
		);
	}
}
