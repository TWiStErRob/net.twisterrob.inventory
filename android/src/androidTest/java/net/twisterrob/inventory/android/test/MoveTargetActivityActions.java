package net.twisterrob.inventory.android.test;

import android.annotation.SuppressLint;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class MoveTargetActivityActions {
	@SuppressLint("PrivateResource")
	public static void up() {
		onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
	}
	public static void confirm() {
		onView(withId(DialogMatchers.BUTTON_POSITIVE)).perform(click());
	}
	public static void cancel() {
		onView(withId(DialogMatchers.BUTTON_NEGATIVE)).perform(click());
	}
	public static void selectItem(String itemName) {
		onRecyclerItem(withText(itemName)).perform(click());
	}
	public static void selectRoom(String roomName) {
		onRecyclerItem(withText(roomName)).perform(click());
	}
	public static void selectProperty(String roomName) {
		onRecyclerItem(withText(roomName)).perform(click());
	}
}
