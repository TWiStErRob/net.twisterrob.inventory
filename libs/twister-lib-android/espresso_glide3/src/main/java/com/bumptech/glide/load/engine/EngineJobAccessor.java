package com.bumptech.glide.load.engine;

import java.lang.reflect.Field;
import java.util.List;

import android.support.annotation.NonNull;

import com.bumptech.glide.request.ResourceCallback;

import static net.twisterrob.java.utils.ReflectionTools.*;

final class EngineJobAccessor {
	private static final Field mCbs = getCallbacksField();

	static void setCallbacks(@NonNull EngineJob job, @NonNull List<ResourceCallback> cbs) {
		try {
			mCbs.set(job, cbs);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Cannot hack EngineJob.cbs", ex);
		}
	}

	@SuppressWarnings("unchecked")
	static @NonNull List<ResourceCallback> getCallbacks(@NonNull EngineJob job) {
		try {
			return (List<ResourceCallback>)mCbs.get(job);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Cannot hack EngineJob.cbs", ex);
		}
	}

	private static @NonNull Field getCallbacksField() {
		try {
			Field field;
			field = EngineJob.class.getDeclaredField("cbs");
			field.setAccessible(true);
			return field;
		} catch (Exception ex) {
			throw new IllegalStateException("Glide EngineJobs callbacks cannot be found", ex);
		}
	}

	static boolean hasException(@NonNull EngineJob job) {
		//noinspection ConstantConditions it's a primitive type, it won't be null
		return get(job, "hasException");
	}

	static boolean hasResource(@NonNull EngineJob job) {
		//noinspection ConstantConditions it's a primitive type, it won't be null
		return get(job, "hasResource");
	}
}
