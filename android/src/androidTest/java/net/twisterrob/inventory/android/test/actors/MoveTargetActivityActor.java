package net.twisterrob.inventory.android.test.actors;

import static org.hamcrest.Matchers.*;

import android.annotation.SuppressLint;

import androidx.annotation.*;

import static androidx.test.core.app.ApplicationProvider.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.android.utils.tools.ResourceTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class MoveTargetActivityActor extends ActivityActor {
	public MoveTargetActivityActor() {
		super(MoveTargetActivity.class);
	}
	public void upToProperties() {
		assertTypeOfBelonging(R.plurals.property);
		up();
		fromProperties();
	}
	public void upToProperty(String propertyName) {
		assertTypeOfBelonging(R.plurals.room);
		up();
		assertShowingProperty(propertyName);
	}
	public void upToRoom(String roomName) {
		assertTypeOfBelonging(R.plurals.item);
		up();
		assertShowingRoom(roomName);
	}
	public void upToItem(String itemName) {
		assertTypeOfBelonging(R.plurals.item);
		up();
		assertShowingItem(itemName);
	}

	@CheckResult
	public MoveResultActor confirmSelection() {
		onView(withId(DialogMatchers.BUTTON_POSITIVE)).perform(click());
		return new MoveResultActor();
	}
	public void cancel() {
		onView(withId(DialogMatchers.BUTTON_NEGATIVE)).perform(click());
	}
	public void selectItem(String itemName) {
		onRecyclerItem(withText(itemName)).perform(click());
		assertShowingItem(itemName);
	}
	public void selectRoom(String roomName) {
		onRecyclerItem(withText(roomName)).perform(click());
		assertShowingRoom(roomName);
	}
	public void selectProperty(String propertyName) {
		fromProperties();
		onRecyclerItem(withText(propertyName)).perform(click());
		assertShowingProperty(propertyName);
	}
	public void fromProperties() {
		assertSelection(getApplicationContext().getString(R.string.property_list));
	}
	public void fromProperty(String propertyName) {
		assertShowingProperty(propertyName);
	}
	public void fromRoom(String roomName) {
		assertShowingRoom(roomName);
	}
	public void fromItem(String itemName) {
		assertShowingItem(itemName);
	}

	public void assertShowingProperty(String propertyName) {
		assertTypeOfBelonging(R.plurals.property);
		assertSelection(propertyName);
	}
	public void assertShowingRoom(String roomName) {
		assertTypeOfBelonging(R.plurals.room);
		assertSelection(roomName);
	}
	public void assertShowingItem(String itemName) {
		assertTypeOfBelonging(R.plurals.item);
		assertSelection(itemName);
	}

	@SuppressLint("PrivateResource")
	private void up() {
		onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
	}
	private void assertTypeOfBelonging(@PluralsRes int plural) {
		String singular = getApplicationContext().getResources().getQuantityString(plural, 1);
		onView(isToolbarSubTitle()).check(matches(withText(containsString(singular))));
	}
	private void assertSelection(String propertyName) {
		onView(withId(R.id.selection))
				.check(matches(isCompletelyDisplayed()))
				.check(matches(withText(containsString(propertyName))))
		;
	}

	public void assertUsableItemSizes() {
		int px = ResourceTools.dipInt(getApplicationContext(), 100);
		onRecyclerItemExact(withParentIndex(0)).check(matches(withSize(greaterThan(px))));
	}
}
