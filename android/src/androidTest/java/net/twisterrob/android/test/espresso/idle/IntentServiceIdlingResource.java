package net.twisterrob.android.test.espresso.idle;

import android.app.*;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

/**
 * @see <a href="http://blog.sqisland.com/2015/04/espresso-custom-idling-resource.html">Refactred from</a>
 */
public class IntentServiceIdlingResource extends AsyncIdlingResource {
	private final Context context;
	private final Class<? extends Service> serviceClass;

	public IntentServiceIdlingResource(Class<? extends Service> serviceClass) {
		this(InstrumentationRegistry.getTargetContext(), serviceClass);
	}
	public IntentServiceIdlingResource(Context context, Class<? extends Service> serviceClass) {
		this.context = context;
		this.serviceClass = serviceClass;
	}

	@Override public String getName() {
		return IntentServiceIdlingResource.class.getSimpleName() + " for " + serviceClass.getName();
	}

	@Override protected boolean isIdle() {
		return !isIntentServiceRunning();
	}
	@Override protected void waitForIdleAsync() {
		// NO OP since can't subscribe to IntentService lifecycle changes, override if you can
	}

	private boolean isIntentServiceRunning() {
		ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (context.getPackageName().equals(info.service.getPackageName())
					&& serviceClass.getName().equals(info.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
