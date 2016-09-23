package com.bumptech.glide.load.engine;

import java.lang.reflect.Field;
import java.util.*;

import android.support.annotation.Nullable;

import com.bumptech.glide.load.Key;

public class EngineIdleWatcher {
	private static final Field mJobs = tryGetJobsField();

	private final Set<Runnable> callbacks = new HashSet<>();
	private final SelfAwareJobs replacementJobs = new SelfAwareJobs();

	public void subscribe(Runnable callback) {
		callbacks.add(callback);
	}

	public void unsubscribe(Runnable callback) {
		callbacks.remove(callback);
	}

	public boolean isIdle() {
		return replacementJobs.isEmpty();
	}

	private void tryToCallBack() {
		if (isIdle()) {
			for (Runnable callback : callbacks) {
				callback.run();
			}
		}
	}

	public void associateWith(@Nullable Engine engine) {
		replacementJobs.setEngine(engine);
	}

	private class SelfAwareJobs extends HashMap<Key, EngineJob> {
		private Engine currentEngine;

		@Override public EngineJob remove(Object key) {
			EngineJob removed = super.remove(key);
			tryToCallBack();
			return removed;
		}

		public void setEngine(@Nullable Engine engine) {
			if (currentEngine == engine) {
				return;
			}
			try {
				this.clear();
				if (mJobs != null && engine != null) {
					@SuppressWarnings("unchecked")
					Map<Key, EngineJob> original = (Map<Key, EngineJob>)mJobs.get(engine);
					this.putAll(original);
					mJobs.set(engine, this);
				} else {
					// just ignore, reflection went haywire, isIdle will be true from here on
				}
			} catch (Exception ex) {
				throw new IllegalStateException("Cannot hack Engine.jobs", ex);
			}
			currentEngine = engine;
		}
	}

	private static Field tryGetJobsField() {
		try {
			Field field;
			field = Engine.class.getDeclaredField("jobs");
			field.setAccessible(true);
			return field;
		} catch (Exception ex) {
			return null;
		}
	}
}
