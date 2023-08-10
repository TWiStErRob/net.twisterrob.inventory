package net.twisterrob.inventory.android.test.actors;

import android.app.Activity;

import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public abstract class ItemContainingViewActivityActor extends ViewActivityActor {
	public ItemContainingViewActivityActor(Class<? extends Activity> activityClass) {
		super(activityClass);
	}

	public void hasItem(String itemName) {
		onRecyclerItem(withText(itemName)).check(matches(isCompletelyDisplayed()));
	}

	public SelectionActor select(String itemName) {
		onRecyclerItem(withText(itemName)).perform(longClick());
		SelectionActor actor = new SelectionActor();
		actor.assertIsActive();
		return actor;
	}

	public ItemEditActivityActor addItem() {
		clickFab();
		ItemEditActivityActor actor = new ItemEditActivityActor();
		actor.assertIsInFront();
		return actor;
	}
}
