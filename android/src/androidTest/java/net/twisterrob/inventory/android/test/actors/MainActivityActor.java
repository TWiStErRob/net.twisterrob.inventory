package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.*;

import android.view.*;

import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.*;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.runner.lifecycle.Stage;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.contrib.NavigationViewActions.*;
import static androidx.test.espresso.matcher.RootMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.idle.ActivityStageIdlingResource;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.MainActivity;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.DrawerMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.junit.InstrumentationExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.java.utils.CollectionTools.*;

public class MainActivityActor extends ActivityActor {
	public MainActivityActor() {
		super(MainActivity.class);
	}

	public Navigator assertHomeScreen() {
		Navigator navigator = new Navigator();
		navigator.checkOpened();
		return navigator;
	}

	public WelcomeDialogActor assertWelcomeShown() {
		onView(withText(R.string.welcome_title))
				.inRoot(isDialog())
				.check(matches(isCompletelyDisplayed()));
		return new WelcomeDialogActor();
	}

	public PropertiesNavigator openProperties() {
		return open(new PropertiesNavigator());
	}
	public RoomsNavigator openRooms() {
		return open(new RoomsNavigator());
	}
	public ItemsNavigator openItems() {
		return open(new ItemsNavigator());
	}
	public CategoriesNavigator openCategories() {
		return open(new CategoriesNavigator());
	}
	public SunburstNavigator openSunburst() {
		return open(new SunburstNavigator());
	}
	public BackupNavigator openBackup() {
		return open(new BackupNavigator());
	}
	public SettingsNavigator openSettings() {
		return open(new SettingsNavigator());
	}
	public AboutNavigator openAbout() {
		return open(new AboutNavigator());
	}
	public SearchActor openSearch() {
		SearchActor actor = new SearchActor();
		actor.open();
		return actor;
	}

	private <T extends DrawerNavigator> T open(T actor) {
		actor.open();
		actor.checkOpened();
		return actor;
	}
	public void openMenu() {
		onView(isDrawer()).perform(openDrawer());
		onView(isDrawerLayout()).check(matches(isAnyDrawerOpen()));
	}
	public void assertMenuClosed() {
		onView(isDrawerLayout()).check(matches(areBothDrawersClosed()));
	}

	public static class WelcomeDialogActor {
		public void dontPopulateDemo() {
			clickNegativeInDialog();
			checkDismissed();
		}
		public void populateDemo() {
			clickPositiveInDialog();
			checkDismissed();
		}
		public BackupActivityActor invokeBackup() {
			clickNeutralInDialog();
			checkDismissed();
			return new BackupActivityActor();
		}
		private void checkDismissed() {
			assertNoDialogIsDisplayed();
		}
	}

	public static class Navigator extends DrawerNavigator {
		@Override public void checkOpened() {
			assertOpened(R.string.home_title, R.string.home_title, R.id.properties);
			assertOpened(R.string.home_title, R.string.home_title, R.id.rooms);
			assertOpened(R.string.home_title, R.string.home_title, R.id.items);
			assertOpened(R.string.home_title, R.string.home_title, R.id.lists);
		}
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_home, false);
		}

		public HomeRoomsActor rooms() {
			return new HomeRoomsActor();
		}
		public static class HomeRoomsActor {
			public RoomViewActivityActor open(String roomName) {
				onRecyclerItem(withText(roomName))
						.inAdapterView(withId(R.id.rooms))
						.perform(click());
				RoomViewActivityActor actor = new RoomViewActivityActor();
				actor.assertIsInFront();
				return actor;
			}
			public void assertExists(String roomName) {
				onRecyclerItem(withText(roomName))
						.inAdapterView(withId(R.id.rooms))
						.onChildView(withId(R.id.image))
						.check(matches(hasImage()));
			}
		}
	}

	public static class PropertiesNavigator extends DrawerNavigator {
		@Override public void checkOpened() {
			assertOpened(R.string.property_list, R.string.property_list, android.R.id.list);
		}
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_properties, false);
		}
		public PropertyEditActivityActor addProperty() {
			onView(withId(R.id.fab)).perform(click());
			return new PropertyEditActivityActor();
		}
		public void hasProperty(String propertyName) {
			onRecyclerItem(withText(propertyName))
					.inAdapterView(withId(android.R.id.list))
					.check(matches(isCompletelyDisplayed()));
		}
		public void hasNoProperties() {
			onView(withId(android.R.id.list)).check(itemDoesNotExists(anyView()));
		}
	}

	public static class RoomsNavigator extends DrawerNavigator {
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_rooms, false);
		}
		@Override public void checkOpened() {
			assertOpened(R.string.room_list, R.string.room_list, android.R.id.list);
		}
	}

	public static class ItemsNavigator extends DrawerNavigator {
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_items, false);
		}
		@Override public void checkOpened() {
			assertOpened(R.string.item_list, R.string.item_list, android.R.id.list);
		}
	}

	public static class CategoriesNavigator extends DrawerNavigator {
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_categories, false);
		}
		@Override public void checkOpened() {
			assertOpened(R.string.category_list, R.string.category_list, android.R.id.list);
		}
	}

	public static class SunburstNavigator extends DrawerNavigator {
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_sunburst, false);
		}
		@Override public void checkOpened() {
			assertOpened(R.string.sunburst_title, R.string.sunburst_title, R.id.diagram);
		}
		public SunburstActivityActor asActor() {
			return new SunburstActivityActor();
		}
	}

	public static class BackupNavigator extends DrawerNavigator {
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_backup, true);
		}
		@Override public void checkOpened() {
			onView(isDrawerLayout()).check(doesNotExist());
			onView(isActionBarTitle())
					.check(matches(isCompletelyDisplayed()))
					.check(matches(withText(R.string.backup_title)))
			;
			onView(withId(R.id.backups)).check(matches(isCompletelyDisplayed()));
		}
		public BackupActivityActor asActor() {
			return new BackupActivityActor();
		}
	}

	public static class SettingsNavigator extends DrawerNavigator {
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_preferences, true);
		}
		@Override public void checkOpened() {
			onView(isDrawerLayout()).check(doesNotExist());
			onView(isActionBarTitle()).check(doesNotExist());
			onView(withId(android.R.id.list)).check(matches(isCompletelyDisplayed()));
		}
		public PreferencesActivityActor asActor() {
			return new PreferencesActivityActor();
		}
	}

	public static class AboutNavigator extends DrawerNavigator {
		@Override protected void open() {
			selectDrawerItem(R.id.action_drawer_about, true);
		}
		@Override public void checkOpened() {
			onView(isDrawerLayout()).check(doesNotExist());
			onView(isActionBarTitle()).check(doesNotExist());
			onView(withId(R.id.about_name)).check(matches(withText(R.string.app_name)));
		}
		public AboutActivityActor asActor() {
			return new AboutActivityActor();
		}
	}

	private static abstract class DrawerNavigator {
		public void tryClose() {
			Espresso.pressBack();
		}
		protected abstract void open();
		public abstract void checkOpened();

		protected void selectDrawerItem(@IdRes int drawerItem, boolean externalActivity) {
			MainActivity activity =
					only(getActivitiesByTypeInStage(MainActivity.class, Stage.RESUMED));
			onView(isNavigationDrawer())
					.perform(openContainingDrawer())
					.perform(navigateTo(drawerItem));
			if (externalActivity) {
				ActivityStageIdlingResource.waitForAtLeast(activity, Stage.STOPPED);
			}
		}

		protected void assertOpened(@StringRes int drawerItem, @StringRes int actionBarTitle, @IdRes int checkView) {
			onView(isDrawerLayout()).check(matches(areBothDrawersClosed()));
			onView(isActionBarTitle())
					.check(matches(isCompletelyDisplayed()))
					.check(matches(withText(actionBarTitle)))
			;
			onView(withId(checkView)).check(matches(isCompletelyDisplayed()));
			onDrawerDescendant(withText(drawerItem)).check(matches(navigationItemIsHighlighted()));
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
}
