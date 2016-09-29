package net.twisterrob.inventory.android.test.actors;

import android.app.Activity;

public abstract class ViewActivityActor extends ActivityActor {
	public ViewActivityActor(Class<? extends Activity> activityClass) {
		super(activityClass);
	}
	public abstract void assertShowing(String name);
}
