package com.bumptech.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.Engine;

public final class GlideAccessor {

	public static @NonNull Engine getEngine(@NonNull Glide glide) {
		return glide.getEngine();
	}
}
