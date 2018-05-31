package net.twisterrob.android.test.espresso.idle;

import android.os.*;
import android.support.test.annotation.Beta;
import android.support.test.espresso.Espresso;

/**
 * @see <a href="https://github.com/chiuki/espresso-samples/blob/master/idling-resource-elapsed-time/app/src/androidTest/java/com/sqisland/espresso/idling_resource/elapsed_time/ElapsedTimeIdlingResource.java">chiuki's ElapsedTimeIdlingResource.java</a>
 */
@Beta
public class ElapsedTimeIdlingResource extends AsyncIdlingResource {
	private long startTime;
	private long waitingTime;
	private final Handler handler;
	private final Runnable transitionToIdle = new Runnable() {
		@Override public void run() {
			transitionToIdle();
		}
	};

	public ElapsedTimeIdlingResource() {
		handler = new Handler(Looper.getMainLooper());
	}
	public ElapsedTimeIdlingResource(long waitingTime) {
		handler = new Handler(Looper.getMainLooper());
		startWaiting(waitingTime);
	}

	@Override public String getName() {
		return "Elapsed time " + waitingTime;
	}

	public void startWaiting(long waitingTime) {
		if (this.startTime != 0) {
			Espresso.unregisterIdlingResources(this);
		}
		this.startTime = System.currentTimeMillis();
		this.waitingTime = waitingTime;
		Espresso.registerIdlingResources(this);
		// CONSIDER backup and restore timeouts
		// Make sure Espresso does not time out
//		IdlingPolicies.setMasterPolicyTimeout(waitingTime * 2, TimeUnit.MILLISECONDS);
//		IdlingPolicies.setIdlingResourceTimeout(waitingTime * 2, TimeUnit.MILLISECONDS);
	}

	public void stopWaiting() {
		handler.removeCallbacks(transitionToIdle);
		Espresso.unregisterIdlingResources(this);
	}

	@Override protected boolean isIdle() {
		return waitingTime <= getElapsed();
	}
	@Override protected void waitForIdleAsync() {
		// TODO check if negative delay is possible and results in immediate execution
		handler.postDelayed(transitionToIdle, getRemaining());
	}

	private long getElapsed() {
		return System.currentTimeMillis() - startTime;
	}
	private long getRemaining() {
		return waitingTime - getElapsed();
	}
}
