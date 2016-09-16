package com.bumptech.glide.load.engine;

import java.lang.reflect.Field;
import java.util.*;

import org.slf4j.*;

import android.support.annotation.*;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.Engine.EngineJobFactory;
import com.bumptech.glide.request.ResourceCallback;

import static net.twisterrob.java.utils.ReflectionTools.*;

public class EngineWatcher {
	private static final Logger LOG = LoggerFactory.getLogger(EngineWatcher.class.getSimpleName());
	private static final Field mJobs = trySetAccessible(tryFindDeclaredField(Engine.class, "jobs"));
	private static final Field mEngineResource =
			trySetAccessible(tryFindDeclaredField(EngineJob.class, "engineResource"));
	private static final Field mHasResource =
			trySetAccessible(tryFindDeclaredField(EngineJob.class, "hasResource"));
	private static final Field mException =
			trySetAccessible(tryFindDeclaredField(EngineJob.class, "exception"));
	private static final Field mHasException =
			trySetAccessible(tryFindDeclaredField(EngineJob.class, "hasException"));

	public final @Nullable Engine engine;
	private final Map<Runnable, Caller> callbacks = new HashMap<>();
	private final Map<Caller, Set<EngineJob>> subscriptions = new HashMap<>();
	public EngineWatcher(@Nullable Engine engine) {
		this.engine = engine;
	}

	public @NonNull Map<Key, EngineJob> getJobs() {
		if (mJobs == null || engine == null) {
			return Collections.emptyMap();
		}
		try {
			@SuppressWarnings("unchecked") Map<Key, EngineJob> jobs = (Map<Key, EngineJob>)mJobs.get(engine);
			if (jobs == null) {
				jobs = Collections.emptyMap();
			}
			return jobs;
		} catch (IllegalAccessException ex) {
			LOG.warn("Cannot get jobs for {}", engine, ex);
			return Collections.emptyMap();
		}
	}

	public void subscribe(Runnable callback) {
		Caller cb = new Caller(callback);
		Caller put = callbacks.put(callback, cb);
		//LOG.trace("Subscribing {} ({}) in {} to {} (replacing {})", callback, cb, this, engine, put);
		if (put != null) {
			unsubscribe(put);
		}
		subscribe(cb);
	}
	private void subscribe(Caller cb) {
		Set<EngineJob> jobs = new HashSet<>(getJobs().values());
		//LOG.trace("Subscribing {} in {} to {} for {}", cb, this, engine, jobs);
		subscriptions.put(cb, jobs);
		for (EngineJob job : jobs) {
			job.addCallback(cb);
		}
	}
	public void unsubscribe(Runnable callback) {
		Caller cb = callbacks.remove(callback);
		//LOG.trace("Unsubscribing {} ({}) in {} from {}", callback, cb, this, engine);
		if (cb != null) {
			unsubscribe(cb);
		}
	}
	private void unsubscribe(Caller cb) {
		Set<EngineJob> jobs = subscriptions.get(cb);
		//LOG.trace("Unsubscribing {} in {} from {} for {}", cb, this, engine, jobs);
		for (EngineJob job : jobs) {
			job.removeCallback(cb);
		}
	}

	private class Caller implements ResourceCallback {
		private final Runnable callback;
		public Caller(Runnable callback) {
			this.callback = callback;
		}
		@Override public void onResourceReady(Resource<?> resource) {
			//LOG.trace("onResourceReady({}) for {} ({})", resource, callback, this);
			unsubscribe(this, resource);
			callback.run();
			// we cheated by adding a callback so let's forget we ever existed,
			// because there's no other way, since we're not a real request/target/listener. 
			((EngineResource<?>)resource).release();
		}
		@Override public void onException(Exception e) {
			//LOG.trace("onException({}) for {} ({})", e, callback, this);
			unsubscribe(this, e);
			// no release needed, because there was no acquire
			callback.run();
		}
	}
	private void unsubscribe(Caller caller, Resource<?> resource) {
		for (Iterator<EngineJob> it = subscriptions.get(caller).iterator(); it.hasNext(); ) {
			EngineJob job = it.next();
			if (hasEngineResource(job, resource)) {
				//LOG.trace("Unsubscribing {} in {} from {} for {}", caller, this, engine, job);
				job.removeCallback(caller);
				it.remove();
			}
		}
	}
	private void unsubscribe(Caller caller, Exception ex) {
		for (Iterator<EngineJob> it = subscriptions.get(caller).iterator(); it.hasNext(); ) {
			EngineJob job = it.next();
			if (hasException(job, ex)) {
				//LOG.trace("Unsubscribing {} in {} from {} for {}", caller, this, engine, job);
				job.removeCallback(caller);
				it.remove();
			}
		}
	}
	private boolean hasException(EngineJob job, Exception ex) {
		try {
			return Boolean.TRUE.equals(mHasException.get(job)) && mException.get(job) == ex;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean hasEngineResource(EngineJob job, Resource<?> resource) {
		try {
			return Boolean.TRUE.equals(mHasResource.get(job)) && mEngineResource.get(job) == resource;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
	}

	// TODO write a version of idling resource that replaces the factory when registered to have smoother control 
	private static class ReplacementEngineJobFactory extends EngineJobFactory {
		private static final Field mEngineJobFactory =
				trySetAccessible(tryFindDeclaredField(Engine.class, "engineJobFactory"));
		private static final Field mListener =
				trySetAccessible(tryFindDeclaredField(EngineJob.class, "listener"));
		private final EngineJobListener listener;
		private final EngineJobFactory originalFactory;
		public ReplacementEngineJobFactory(EngineWatcher engine, EngineJobListener listener) {
			super(null, null, null);
			this.listener = listener;
			try {
				this.originalFactory = (EngineJobFactory)mEngineJobFactory.get(engine.engine);
				mEngineJobFactory.set(engine.engine, this);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Cannot replace engine job factory", ex);
			}
			for (EngineJob job : engine.getJobs().values()) {
				replaceListener(job);
			}
		}
		@Override public EngineJob build(Key key, boolean isMemoryCacheable) {
			return replaceListener(originalFactory.build(key, isMemoryCacheable));
		}
		private EngineJob replaceListener(EngineJob job) {
			try {
				EngineJobListener listener = (EngineJobListener)mListener.get(job);
				if (!(listener instanceof WrappingListener)) {
					mListener.set(job, new WrappingListener(listener, this.listener));
				}
				return job;
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Cannot replace engine job listener", ex);
			}
		}

		private static class WrappingListener implements EngineJobListener {
			private final EngineJobListener listener1;
			private final EngineJobListener listener2;
			public WrappingListener(EngineJobListener listener1, EngineJobListener listener2) {
				this.listener1 = listener1;
				this.listener2 = listener2;
			}
			@Override public void onEngineJobComplete(Key key, EngineResource<?> resource) {
				listener1.onEngineJobComplete(key, resource);
				listener2.onEngineJobComplete(key, resource);
			}
			@Override public void onEngineJobCancelled(EngineJob engineJob, Key key) {
				listener1.onEngineJobCancelled(engineJob, key);
				listener2.onEngineJobCancelled(engineJob, key);
			}
		}
	}
}
