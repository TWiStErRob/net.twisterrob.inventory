package com.bumptech.glide.load.engine;

import android.support.annotation.NonNull;

import net.twisterrob.java.utils.ReflectionTools;

final class EngineKeyAccessor {

	static int getHeight(EngineKey key) {
		//noinspection ConstantConditions it's a primitive type, it won't be null
		return ReflectionTools.get(key, "height");
	}

	static int getWidth(@NonNull EngineKey key) {
		//noinspection ConstantConditions it's a primitive type, it won't be null
		return ReflectionTools.get(key, "width");
	}

	static @NonNull String getId(@NonNull EngineKey key) {
		//noinspection ConstantConditions id is meant to be non-null coming from DataFetcher.getId()
		return ReflectionTools.get(key, "id");
	}

	static @NonNull String toString(@NonNull EngineKey key) {
		return getId(key) + "[" + getWidth(key) + "x" + getHeight(key) + "]";
	}
}
