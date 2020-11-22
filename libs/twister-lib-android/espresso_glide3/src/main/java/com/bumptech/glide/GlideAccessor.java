package com.bumptech.glide;

import com.bumptech.glide.load.engine.Engine;

import androidx.annotation.NonNull;

public final class GlideAccessor {

	public static @NonNull Engine getEngine(@NonNull Glide glide) {
		return glide.getEngine();
	}
}
