package net.twisterrob.android.content.glide;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.*;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

/**
 * Copy of {@link com.bumptech.glide.load.resource.transcode.GlideBitmapDrawableTranscoder}
 * which adds a {@link android.graphics.ColorFilter} before wrapping the drawable in the resource.
 * This will propagate to all drawables obtained from the returned {@link com.bumptech.glide.load.engine.Resource}.
 */
public class FilteredGlideBitmapDrawableTranscoder implements ResourceTranscoder<Bitmap, GlideBitmapDrawable> {
	private final Resources resources;
	private final BitmapPool bitmapPool;
	private final ColorFilter filter;

	public FilteredGlideBitmapDrawableTranscoder(Context context, ColorFilter filter) {
		this(context.getResources(), Glide.get(context).getBitmapPool(), filter);
	}

	public FilteredGlideBitmapDrawableTranscoder(Resources resources, BitmapPool bitmapPool, ColorFilter filter) {
		this.resources = resources;
		this.bitmapPool = bitmapPool;
		this.filter = filter;
	}

	@Override public String getId() {
		return FilteredGlideBitmapDrawableTranscoder.class.getSimpleName();
	}

	@Override public Resource<GlideBitmapDrawable> transcode(Resource<Bitmap> toTranscode) {
		GlideBitmapDrawable drawable = new GlideBitmapDrawable(resources, toTranscode.get());
		drawable.setColorFilter(filter);
		return new GlideBitmapDrawableResource(drawable, bitmapPool);
	}
}
