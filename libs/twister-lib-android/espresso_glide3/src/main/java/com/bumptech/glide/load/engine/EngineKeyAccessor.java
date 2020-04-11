package com.bumptech.glide.load.engine;

import net.twisterrob.java.utils.ReflectionTools;

final class EngineKeyAccessor {

	static int getHeight(EngineKey key) {
		return ReflectionTools.get(key, "height");
	}

	static int getWidth(EngineKey key) {
		return ReflectionTools.get(key, "width");
	}

	static String getId(EngineKey key) {
		return ReflectionTools.get(key, "id");
	}

	static String toString(EngineKey key) {
		return getId(key) + "[" + getWidth(key) + "x" + getHeight(key) + "]";
	}
}
