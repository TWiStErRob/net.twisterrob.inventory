package net.twisterrob.inventory.android.activity;

import org.junit.*;

import android.support.annotation.*;
import android.support.test.espresso.*;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.idle.DrawerIdlingResource;
import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.InventoryActivityRule;

import static net.twisterrob.android.test.espresso.DrawerMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class MainActivityTest_Drawer {
	@Rule public final ActivityTestRule<MainActivity> activity = new InventoryActivityRule<>(MainActivity.class);
	@Rule public final IdlingResourceRule drawer = DrawerIdlingResource.rule();

	@Before public void startup() {
		assertHomeOpened();
	}
	@Test(expected = NoActivityResumedException.class)
	public void testHomeBack() {
		Espresso.pressBack();
	}

	@Test public void testDrawerBack() {
		onView(isDrawer()).perform(openDrawer());
		onView(isDrawerLayout()).check(matches(isAnyDrawerOpen()));
		Espresso.pressBack();
		onView(isDrawerLayout()).check(matches(areBothDrawersClosed()));
	}

	@Test public void testCategories() {
		openList(R.string.category_list, R.string.category_list);
	}
	@Test public void testProperties() {
		openList(R.string.property_list, R.string.property_list);
	}
	@Test public void testRooms() {
		openList(R.string.room_list, R.string.room_list);
	}
	@Test public void testItems() {
		openList(R.string.item_list, R.string.item_list);
	}
	@Test public void testSunburst() {
		open(R.string.sunburst_title, R.string.sunburst_title, R.id.diagram);
	}

	@Test public void testBackup() {
		intending(isInternal()).respondWith(cancelResult());
		onDrawerDescendant(withText(R.string.backup_title)).perform(click());
		intended(isInternal());
		assertHomeOpened(); // drawer navigation intent captured, so backup activity wasn't opened
	}
	@Test public void testBackupAndBack() {
		openList(R.string.property_list, R.string.property_list);
		openBackup();
		Espresso.pressBack();
		assertOpened(R.string.property_list, android.R.id.list);
	}

	@Test public void testBackNavigation_Single() {
		openList(R.string.property_list, R.string.property_list);
		Espresso.pressBack();
		assertHomeOpened();
	}
	@Test public void testBackNavigation_Multiple() {
		openList(R.string.property_list, R.string.property_list);
		openList(R.string.room_list, R.string.room_list);
		openList(R.string.item_list, R.string.item_list);
		Espresso.pressBack();
		assertOpened(R.string.room_list, android.R.id.list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}
	@Test public void testBackNavigation_BackAndForth() {
		openList(R.string.property_list, R.string.property_list);
		openList(R.string.room_list, R.string.room_list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, android.R.id.list);
		openList(R.string.item_list, R.string.item_list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}

	@Test public void testRotation() {
		openList(R.string.property_list, R.string.property_list);
		onView(isRoot()).perform(rotateActivity());
		assertOpened(R.string.property_list, android.R.id.list);
	}
	@Test public void testBackNavigation_Rotation() {
		openList(R.string.property_list, R.string.property_list);
		onView(isRoot()).perform(rotateActivity());
		assertOpened(R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}
	@Test public void testBackNavigation_RotationComplex() {
		openList(R.string.property_list, R.string.property_list);
		openList(R.string.room_list, R.string.room_list);
		onView(isRoot()).perform(rotateActivity());
		assertOpened(R.string.room_list, android.R.id.list);
		openList(R.string.item_list, R.string.item_list);
		Espresso.pressBack();
		assertOpened(R.string.room_list, android.R.id.list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}

	private void openList(int drawerItem, int actionBarTitle) {
		open(drawerItem, actionBarTitle, android.R.id.list);
	}

	private void open(@StringRes int drawerItem, @StringRes int actionBarTitle, @IdRes int checkView) {
		onDrawerDescendant(withText(drawerItem)).perform(click());
		assertOpened(actionBarTitle, checkView);
	}
	private void assertOpened(@StringRes int actionBarTitle, @IdRes int checkView) {
		onView(isDrawerLayout()).check(matches(areBothDrawersClosed()));
		onActionBarDescendant(withText(actionBarTitle)).check(matches(isDisplayed()));
		onView(withId(checkView)).check(matches(isDisplayed()));
	}
	private void assertHomeOpened() {
		assertOpened(R.string.home_title, R.id.properties);
		assertOpened(R.string.home_title, R.id.rooms);
		assertOpened(R.string.home_title, R.id.items);
		assertOpened(R.string.home_title, R.id.lists);
	}

	private void openBackup() {
		onDrawerDescendant(withText(R.string.backup_title)).perform(click());
		onView(isDrawerLayout()).check(doesNotExist());
		onActionBarDescendant(withText(R.string.backup_title)).check(matches(isDisplayed()));
		onView(withId(R.id.backups)).check(matches(isDisplayed()));
	}
}
