package net.twisterrob.inventory.android.activity;

import org.junit.*;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.DrawerMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class MainActivityTest_Drawer {
	@Rule public final ActivityTestRule<MainActivity> activity = new InventoryActivityRule<>(MainActivity.class);
	@Rule public final IdlingResourceRule drawer = new InventoryDrawerRule(activity);

	@Test public void test() {
		clickNegativeInDialog();

		onDrawerDescendant(withText(R.string.property_list)).perform(click());
		onActionBarDescendant(withText(R.string.property_list)).check(matches(isDisplayed()));
		onView(withId(android.R.id.list)).check(matches(isDisplayed()));

		onDrawerDescendant(withText(R.string.backup_title)).perform(click());
		onActionBarDescendant(withText(R.string.backup_title)).check(matches(isDisplayed()));
		onView(withId(android.R.id.list)).check(matches(isDisplayed()));

		Espresso.pressBack();

		onDrawerDescendant(withText(R.string.room_list)).perform(click());
		onActionBarDescendant(withText(R.string.room_list)).check(matches(isDisplayed()));
		onView(withId(android.R.id.list)).check(matches(isDisplayed()));
	}
}
