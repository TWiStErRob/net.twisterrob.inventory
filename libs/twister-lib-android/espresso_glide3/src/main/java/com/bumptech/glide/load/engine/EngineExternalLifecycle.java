package com.bumptech.glide.load.engine;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.request.ResourceCallback;
import com.bumptech.glide.util.Util;

import androidx.annotation.*;

// CONSIDER get rid of twisterrob dependency? and share on Github Glide issues

/**
 * This class cannot be moved out of this package because it uses package private classes.
 * @see Engine
 * @see EngineJob
 */
public class EngineExternalLifecycle {

	private final PhaseCallbacks callback;
	private final Engine engine;
	private final EngineJobsReplacement replacementJobs = new EngineJobsReplacement();
	private final Collection<LoadEndListener> endListeners = new HashSet<>();

	public EngineExternalLifecycle(@NonNull Engine engine, @NonNull PhaseCallbacks callback) {
		this.callback = callback;
		this.engine = engine;
		associate();
	}

	public Collection<EngineJob> getJobs() {
		return Collections.unmodifiableCollection(replacementJobs.values());
	}
	public Collection<ResourceCallback> getActive() {
		return Collections.<ResourceCallback>unmodifiableCollection(endListeners);
	}

	private void starting(EngineKey key, EngineJob job) {
		LoadEndListener endListener = setSignUp(key, job);
		assertTrue(endListeners.add(endListener));
		assertThat(replacementJobs, hasEntry((Key)key, job));
		callback.starting(engine, key, job);
		if (job.isCancelled()
				|| EngineJobAccessor.hasResource(job)
				|| EngineJobAccessor.hasException(job)) {
			// catch up in case they're already done when we're created
			finishing(key, job);
			// call the corresponding callback method
			job.addCallback(endListener);
		}
	}

	private void finishing(EngineKey key, EngineJob job) {
		assertThat(replacementJobs, not(hasKey((Key)key)));
		assertThat(replacementJobs, not(hasValue(job)));
		assertThat(endListeners, hasItem(getSignUp(job)));
		if (job.isCancelled()) {
			assertTrue(endListeners.remove(getSignUp(job)));
			callback.cancelled(engine, key, job);
		} else {
			callback.finishing(engine, key, job);
		}
	}

	private void loadSuccess(LoadEndListener signup) {
		assertTrue(endListeners.remove(signup));
		callback.loadSuccess(engine, signup.key, signup.job);
	}

	private void loadFailure(LoadEndListener signup) {
		assertTrue(endListeners.remove(signup));
		callback.loadFailure(engine, signup.key, signup.job);
	}

	/** {@code Engine.jobs = new EngineJobsReplacement(Engine.jobs)} */
	private void associate() {
		Map<Key, EngineJob> original = EngineAccessor.getJobs(engine);
		if (original instanceof EngineJobsReplacement) {
			EngineJobsReplacement replacement = (EngineJobsReplacement)original;
			throw new IllegalStateException(
					engine + " already has an external lifecycle: " + replacement.getAssociation());
		}
		assertThat(
				"freshly associating, there should be nothing yet",
				replacementJobs, is(anEmptyMap())
		);
		for (Map.Entry<Key, EngineJob> entry : original.entrySet()) {
			replacementJobs.put(entry.getKey(), entry.getValue());
		}
		EngineAccessor.setJobs(engine, replacementJobs);
		assertThat(
				"same items exist in both maps after association",
				replacementJobs, equalTo(original)
		);
	}

	/** {@code job.cbs += new LoadEndListener()} */
	private LoadEndListener setSignUp(EngineKey key, EngineJob job) {
		Util.assertMainThread();
		List<ResourceCallback> cbs = EngineJobAccessor.getCallbacks(job);
		if (cbs instanceof ExtraItemList) {
			throw new IllegalStateException(job + " already being listened to by " + cbs);
		}
		LoadEndListener extra = new LoadEndListener(key, job);
		cbs = new ExtraItemList(cbs, extra);
		EngineJobAccessor.setCallbacks(job, cbs);
		return extra;
	}

	/** {@code job.cbs.iterator().last()} */
	private LoadEndListener getSignUp(EngineJob job) {
		Util.assertMainThread();
		List<ResourceCallback> cbs = EngineJobAccessor.getCallbacks(job);
		if (cbs instanceof ExtraItemList) {
			return (LoadEndListener)((ExtraItemList)cbs).extra;
		} else {
			throw new IllegalStateException(job + " doesn't have an end listener (" + cbs + ")");
		}
	}

	@Override public @NonNull String toString() {
		return String.format(Locale.ROOT, "%s jobs=%d, listeners=%d, callback=%s",
				engine, replacementJobs.size(), endListeners.size(), callback);
	}

	/**
	 * Appends and extra item to the end of the list, but only when iterating. Size and other queries won't report it.
	 * This is only useful with {@link EngineJob}s, because we know that only {@link #add}, {@link #remove},
	 * {@link #isEmpty} and {@link #iterator} are going to be called.
	 */
	@SuppressWarnings("serial") // won't be serialized
	private static class ExtraItemList extends ArrayList<ResourceCallback> {
		private final ResourceCallback extra;
		public ExtraItemList(Collection<ResourceCallback> callbacks, ResourceCallback extra) {
			super(callbacks);
			this.extra = extra;
		}
		@Override public @NonNull Iterator<ResourceCallback> iterator() {
			Iterator<ResourceCallback> extraIt =
					androidx.test.espresso.core.internal.deps.guava.collect.Iterators.singletonIterator(extra);
			return com.google.common.collect.Iterators.concat(super.iterator(), extraIt);
		}
	}

	/**
	 * Callbacks whenever jobs are added or removed. This helps to "modify" the code of Engine externally.
	 */
	@SuppressWarnings("serial") // won't be serialized
	private class EngineJobsReplacement extends HashMap<Key, EngineJob> {
		@Override public void putAll(@NonNull Map<? extends Key, ? extends EngineJob> m) {
			throw new UnsupportedOperationException(
					"Use put to make sure lifecycle handled correctly.");
		}

		@Override public EngineJob put(Key key, EngineJob value) {
			assertNull(
					"key shouldn't exist yet, a job is started only once",
					super.put(key, value)
			);
			starting((EngineKey)key, value);
			return null;
		}

		@Override public EngineJob remove(Object key) {
			EngineJob removed = super.remove(key);
			finishing((EngineKey)key, removed);
			return removed;
		}

		public EngineExternalLifecycle getAssociation() {
			return EngineExternalLifecycle.this;
		}
	}

	/**
	 * Called back at the end of a job when all other resources are notified.
	 */
	private class LoadEndListener implements ResourceCallback {
		private final EngineKey key;
		private final EngineJob job;
		public LoadEndListener(EngineKey key, EngineJob job) {
			this.key = key;
			this.job = job;
		}

		@Override public void onResourceReady(Resource<?> resource) {
			// this "target" won't ever be cleared, so let's clean up real quick after ourselves
			((EngineResource<?>)resource).release();
			loadSuccess(this);
		}

		@Override public void onException(Exception e) {
			loadFailure(this);
		}

		@Override public @NonNull String toString() {
			return job + ": " + EngineKeyAccessor.toString(key);
		}
	}

	@UiThread
	public interface PhaseCallbacks {
		/**
		 * Job created, but no callbacks are added yet, and the job will be started right after this.
		 * @see Engine#load
		 */
		//EngineJob engineJob = engineJobFactory.build(key, isMemoryCacheable);
		//jobs.put(key, engineJob); // starting
		//engineJob.addCallback(cb);
		//engineJob.start(runnable);
		void starting(Engine engine, EngineKey key, EngineJob job);

		/**
		 * Job is finishing, it already has a resource or an exception,
		 * but the result is not broadcast yet to the callbacks.
		 * Either {@link #loadSuccess} or {@link #loadFailure} will be called right after this.
		 * @see Engine#onEngineJobComplete
		 * @see EngineJob#handleResultOnMainThread()
		 * @see EngineJob#handleExceptionOnMainThread()
		 */
		//hasResource = true; or hasException = true;
		//listener.onEngineJobComplete(key, ?); -> jobs.remove(key);
		void finishing(Engine engine, EngineKey key, EngineJob job);

		/**
		 * Job is cancelled, it has no resource nor exception.
		 * No more interaction are expected with this job after this.
		 * @see EngineJob#cancel()
		 * @see Engine#onEngineJobCancelled
		 */
		//isCancelled = true;
		//listener.onEngineJobCancelled(this, key); -> jobs.remove(key);
		void cancelled(Engine engine, EngineKey key, EngineJob job);

		/**
		 * All the callbacks have been notified for {@link ResourceCallback#onResourceReady}.
		 * Load is considered fully finished, resources are delivered to targets.
		 * No further interaction will, not even when be clearing the target,
		 * because the job already has resource or exception and removeCallback won't call cancel.
		 * @see EngineJob#handleResultOnMainThread()
		 */
		//hasResource = true;
		//engineResource.acquire();
		//listener.onEngineJobComplete(key, engineResource); -> jobs.remove(key);
		//for (ResourceCallback cb : cbs) {
		//	engineResource.acquire(); // this is released in LoadEndListener
		//	cb.onResourceReady(engineResource);
		//}
		//engineResource.release();
		void loadSuccess(Engine engine, EngineKey key, EngineJob job);

		/**
		 * All the callbacks have been notified for {@link ResourceCallback#onException}.
		 * Load is considered fully finished, exception is delivered to targets.
		 * No further interaction will, not even when be clearing the target,
		 * because the job already has resource or exception and removeCallback won't call cancel.
		 * @see EngineJob#handleExceptionOnMainThread()
		 */
		//hasException = true;
		//listener.onEngineJobComplete(key, null); -> jobs.remove(key);
		//for (ResourceCallback cb : cbs) cb.onException(exception);
		void loadFailure(Engine engine, EngineKey key, EngineJob job);
	}
}
