package com.bumptech.glide.load.engine;

import java.lang.reflect.Field;
import java.util.Map;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Key;

final class EngineAccessor {
	private static final Field mJobs = getJobsField();

	@SuppressWarnings("unchecked")
	static @NonNull Map<Key, EngineJob> getJobs(@NonNull Engine engine) {
		try {
			return (Map<Key, EngineJob>)mJobs.get(engine);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Cannot hack Engine.jobs", ex);
		}
	}

	static void setJobs(@NonNull Engine engine, @NonNull Map<Key, EngineJob> jobs) {
		try {
			mJobs.set(engine, jobs);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Cannot hack Engine.jobs", ex);
		}
	}

	private static @NonNull Field getJobsField() {
		try {
			Field field;
			field = Engine.class.getDeclaredField("jobs");
			field.setAccessible(true);
			return field;
		} catch (Exception ex) {
			throw new IllegalStateException("Glide Engine jobs cannot be found", ex);
		}
	}
}
