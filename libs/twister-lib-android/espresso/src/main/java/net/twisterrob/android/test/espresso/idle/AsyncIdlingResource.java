package net.twisterrob.android.test.espresso.idle;

import org.slf4j.*;

import android.support.annotation.MainThread;
import android.support.test.espresso.IdlingResource;

public abstract class AsyncIdlingResource implements IdlingResource {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private ResourceCallback resourceCallback;
	private boolean verbose = false;

	protected AsyncIdlingResource() {
		//beVerbose();
	}

	public AsyncIdlingResource beVerbose() {
		verbose = true;
		return this;
	}

	protected boolean isVerbose() {
		return verbose;
	}

	@MainThread
	@Override public final boolean isIdleNow() {
		boolean idle = isIdle();
		if (verbose || !idle) {
			LOG.trace("{}.isIdleNow: {}", getName(), idle);
		}
		if (idle) {
			transitionToIdle(false);
		} else {
			waitForIdleAsync();
		}
		return idle;
	}

	//@AnyThread, don't enable yet, there are still annoying warnings
	protected void transitionToIdle() {
		transitionToIdle(true);
	}

	private void transitionToIdle(boolean log) {
		if (log) {
			LOG.trace("{}.onTransitionToIdle with {}", getName(), resourceCallback);
		}
		if (resourceCallback != null) {
			resourceCallback.onTransitionToIdle();
		}
	}

	@MainThread
	protected abstract boolean isIdle();
	@MainThread
	protected abstract void waitForIdleAsync();

	public ResourceCallback getIdleTransitionCallback() {
		return resourceCallback;
	}

	@MainThread
	@Override public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
		this.resourceCallback = resourceCallback;
	}
}
