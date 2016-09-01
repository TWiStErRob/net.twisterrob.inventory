package net.twisterrob.android.test.espresso.idle;

import org.slf4j.*;

import android.support.test.espresso.IdlingResource;

public abstract class AsyncIdlingResource implements IdlingResource {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private ResourceCallback resourceCallback;

	@Override public final boolean isIdleNow() {
		boolean idle = isIdle();
		LOG.trace("isIdleNow: {}, callback: {}", idle, resourceCallback);
		if (idle) {
			onTransitionToIdle();
		} else {
			waitForIdleAsync();
		}
		return idle;
	}

	protected void onTransitionToIdle() {
		if (resourceCallback != null) {
			resourceCallback.onTransitionToIdle();
		}
	}

	protected abstract boolean isIdle();
	protected abstract void waitForIdleAsync();

	@Override public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
		this.resourceCallback = resourceCallback;
	}
}
