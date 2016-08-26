package net.twisterrob.android.test;

import org.slf4j.*;

import android.support.test.espresso.IdlingResource;

public abstract class PollingIdlingResource implements IdlingResource {
	private static final Logger LOG = LoggerFactory.getLogger(PollingIdlingResource.class);

	private ResourceCallback resourceCallback;

	@Override public final boolean isIdleNow() {
		boolean idle = isIdle();
		LOG.trace("isIdleNow: {}, callback: {}", idle, resourceCallback);
		if (idle && resourceCallback != null) {
			resourceCallback.onTransitionToIdle();
		}
		return idle;
	}

	protected abstract boolean isIdle();

	@Override public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
		this.resourceCallback = resourceCallback;
	}
}
