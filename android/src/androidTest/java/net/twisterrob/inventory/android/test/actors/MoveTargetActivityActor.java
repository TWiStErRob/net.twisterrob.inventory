package net.twisterrob.inventory.android.test.actors;

import android.annotation.SuppressLint;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class MoveTargetActivityActor {
	public void upToProperties() {
		up();
	}
	public void upToProperty(String propertyName) {
		up();
	}
	public void upToRoom(String roomName) {
		up();
	}
	public void upToItem(String roomName) {
		up();
	}

	@Deprecated
	@SuppressLint("PrivateResource")
	public void up() {
		onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
	}
	public MoveResultActor confirmSelection() {
		onView(withId(DialogMatchers.BUTTON_POSITIVE)).perform(click());
		return new MoveResultActor();
	}
	public void cancel() {
		onView(withId(DialogMatchers.BUTTON_NEGATIVE)).perform(click());
	}
	public void selectItem(String itemName) {
		onRecyclerItem(withText(itemName)).perform(click());
	}
	public void selectRoom(String roomName) {
		onRecyclerItem(withText(roomName)).perform(click());
	}
	public void selectProperty(String roomName) {
		onRecyclerItem(withText(roomName)).perform(click());
	}
	public void fromRoom(String roomName) {
		// STOPSHIP
	}
}
