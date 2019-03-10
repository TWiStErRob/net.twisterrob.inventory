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
			LOG.trace("{}.isIdleNow: {}\n{}", getName(), idle, this);
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
			LOG.trace("{}.onTransitionToIdle with {}\n{}", getName(), resourceCallback, this);
		}
		if (resourceCallback != null) {
			resourceCallback.onTransitionToIdle();
		}
	}

	/**
	 * Synchronously calculate if the resource is idle yet.
	 * If the resource is non-existent at this point, return {@code true}.
	 */
	@MainThread
	protected abstract boolean isIdle();

	/**
	 * Add a "listener" to the resource that will be called asynchronously.
	 * When the listener triggers and completes whatever it is doing:
	 * <ul>
	 * <li>remove the listener from the resource, to make sure no more notifications will be received</li>
	 * <li>call {@link #transitionToIdle()}, to let Espresso know as fast as possible we're ready</li>
	 * </ul>
	 * Alternatively the listener can be removed in {@link #transitionToIdle()} as well,
	 * in case it's possible to be called from multiple sources.
	 */
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
