package net.twisterrob.inventory.android.activity;

import org.hamcrest.*;
import org.junit.*;

import android.support.annotation.*;
import android.support.design.internal.NavigationMenuItemView;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.*;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.view.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.InventoryActivityRule;

import static net.twisterrob.android.test.espresso.DrawerMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class MainActivityTest_Drawer {
	@Rule public final ActivityTestRule<MainActivity> activity = new InventoryActivityRule<>(MainActivity.class);

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

	@Test public void testBackupIntent() {
		intending(isInternal()).respondWith(cancelResult());
		onOpenDrawerDescendant(withText(R.string.backup_title)).perform(click());
		intended(isInternal());
		// drawer navigation intent captured, so backup activity wasn't opened, but its drawer item was activated
		assertOpened(R.string.backup_title, R.string.home_title, R.id.properties);
	}
	@Test public void testBackupAndBack() {
		openBackup();
		Espresso.pressBack();
		assertHomeOpened();
	}
	@Test public void testBackupAndBackDeep() {
		openList(R.string.property_list, R.string.property_list);
		openBackup();
		Espresso.pressBack();
		assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
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
		assertOpened(R.string.room_list, R.string.room_list, android.R.id.list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}
	@Test public void testBackNavigation_BackAndForth() {
		openList(R.string.property_list, R.string.property_list);
		openList(R.string.room_list, R.string.room_list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
		openList(R.string.item_list, R.string.item_list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}

	@Test public void testRotation() {
		openList(R.string.property_list, R.string.property_list);
		onView(isRoot()).perform(rotateActivity());
		assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
	}
	@Test public void testBackNavigation_Rotation() {
		openList(R.string.property_list, R.string.property_list);
		onView(isRoot()).perform(rotateActivity());
		assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}
	@Test public void testBackNavigation_RotationComplex() {
		openList(R.string.property_list, R.string.property_list);
		openList(R.string.room_list, R.string.room_list);
		onView(isRoot()).perform(rotateActivity());
		assertOpened(R.string.room_list, R.string.room_list, android.R.id.list);
		openList(R.string.item_list, R.string.item_list);
		Espresso.pressBack();
		assertOpened(R.string.room_list, R.string.room_list, android.R.id.list);
		Espresso.pressBack();
		assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
		Espresso.pressBack();
		assertHomeOpened();
	}

	private void openList(int drawerItem, int actionBarTitle) {
		open(drawerItem, actionBarTitle, android.R.id.list);
	}

	private void open(@StringRes int drawerItem, @StringRes int actionBarTitle, @IdRes int checkView) {
		onOpenDrawerDescendant(withText(drawerItem)).perform(click()).check(matches(navigationItemIsHighlighted()));
		assertOpened(drawerItem, actionBarTitle, checkView);
	}
	private void assertOpened(@StringRes int drawerItem, @StringRes int actionBarTitle, @IdRes int checkView) {
		onView(isDrawerLayout()).check(matches(areBothDrawersClosed()));
		onActionBarDescendant(withText(actionBarTitle)).check(matches(isDisplayed()));
		onView(withId(checkView)).check(matches(isDisplayed()));
		onDrawerDescendant(withText(drawerItem)).check(matches(navigationItemIsHighlighted()));
	}
	private void assertHomeOpened() {
		assertOpened(R.string.home_title, R.string.home_title, R.id.properties);
		assertOpened(R.string.home_title, R.string.home_title, R.id.rooms);
		assertOpened(R.string.home_title, R.string.home_title, R.id.items);
		assertOpened(R.string.home_title, R.string.home_title, R.id.lists);
	}

	private void openBackup() {
		onOpenDrawerDescendant(withText(R.string.backup_title)).perform(click());
		onView(isDrawerLayout()).check(doesNotExist());
		onActionBarDescendant(withText(R.string.backup_title)).check(matches(isDisplayed()));
		onView(withId(R.id.backups)).check(matches(isDisplayed()));
	}

	private static Matcher<View> navigationItemIsHighlighted() {
		return new BoundedMatcher<View, View>(View.class) {
			@Override public void describeTo(Description description) {
				description.appendText("navigation item is the only checked one");
			}
			@Override protected boolean matchesSafely(View view) {
				View parent = view;
				while (!(parent instanceof NavigationMenuItemView)) {
					if (parent instanceof NavigationView) {
						throw new IllegalStateException(
								"Went too high in hierarchy, cannot find parent menu item view.");
					}
					parent = (View)parent.getParent();
				}
				MenuItem item = ((NavigationMenuItemView)parent).getItemData();
				Menu menu = ((NavigationView)parent.getParent().getParent()).getMenu();
				for (int i = 0; i < menu.size(); i++) {
					MenuItem subItem = menu.getItem(i);
					if (subItem != item && subItem.isChecked()) {
						return false;
					}
				}
				return item.isChecked();
			}
		};
	}
}
