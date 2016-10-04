package com.bumptech.glide.load.engine;

import java.util.*;

import org.slf4j.*;

import com.bumptech.glide.request.ResourceCallback;

import static net.twisterrob.java.utils.ReflectionTools.*;

/**
 * This class is the bridge between package private stuff and the world, don't try to inline it.
 */
public class EngineIdleWatcher implements EngineExternalLifecycle.PhaseCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger("EngineIdleWatcher");
	private final Set<Runnable> idleCallbacks = new HashSet<>();
	private final EngineExternalLifecycle lifecycle;

	private boolean logEvents = false;

	public EngineIdleWatcher(Engine engine) {
		lifecycle = new EngineExternalLifecycle(engine, this);
	}

	public void setLogEvents(boolean logEvents) {
		this.logEvents = logEvents;
	}
	public void subscribe(Runnable callback) {
		idleCallbacks.add(callback);
	}

	public void unsubscribe(Runnable callback) {
		idleCallbacks.remove(callback);
	}

	public boolean isIdle() {
		Collection<EngineJob> jobs = lifecycle.getJobs();
		Collection<? extends ResourceCallback> active = lifecycle.getActive();
		//LOG.trace("{}/{}: active={}", this, lifecycle,  active.size());
		return jobs.isEmpty() && active.isEmpty();
	}

	private void tryToCallBack() {
		if (isIdle()) {
			for (Runnable callback : idleCallbacks) {
				callback.run();
			}
		}
	}

	@Override public void starting(Engine engine, EngineKey key, EngineJob job) {
		if (logEvents) {
			LOG.trace("{}.starting {}: {}", this, job, id(key));
		}
	}
	@Override public void finishing(Engine engine, EngineKey key, EngineJob job) {
		if (logEvents) {
			LOG.trace("{}.finishing {}: {}", this, job, id(key));
		}
	}
	@Override public void cancelled(Engine engine, EngineKey key, EngineJob job) {
		if (logEvents) {
			LOG.trace("{}.cancelled {}: {}", this, job, id(key));
		}
		tryToCallBack();
	}
	@Override public void loadSuccess(Engine engine, EngineKey key, EngineJob job) {
		if (logEvents) {
			LOG.trace("{}.loadSuccess {}: {}", this, job, id(key));
		}
		tryToCallBack();
	}
	@Override public void loadFailure(Engine engine, EngineKey key, EngineJob job) {
		if (logEvents) {
			LOG.trace("{}.loadFailure {}: {}", this, job, id(key));
		}
		tryToCallBack();
	}

	private Object id(EngineKey key) {
		return get(key, "id") + "[" + get(key, "width") + "x" + get(key, "height") + "]";
	}
}
