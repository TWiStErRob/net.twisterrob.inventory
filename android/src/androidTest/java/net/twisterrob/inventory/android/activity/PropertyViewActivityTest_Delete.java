package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.PropertyViewActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

@RunWith(AndroidJUnit4.class)
public class PropertyViewActivityTest_Delete {
	@Rule public final ActivityTestRule<PropertyViewActivity> activity
			= new InventoryActivityRule<PropertyViewActivity>(PropertyViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			propertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY, NO_DESCRIPTION);
			getStartIntent().putExtras(Intents.bundleFromProperty(propertyID));
		}
	};
	@Rule public final TestDatabaseRule db = new TestDatabaseRule();
	private long propertyID;

	@Before public void preconditions() {
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));
	}
	@After public void closeDialog() {
		attemptCloseDialog();
	}

	@Test public void testDeleteCancel() throws IOException {
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));

		onActionMenuView(withText(R.string.property_delete)).perform(click());
		clickNegativeInDialog();

		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));
	}

	@Test public void testDeleteMessage() throws IOException {
		onActionMenuView(withText(R.string.property_delete)).perform(click());

		onView(withText(matchesPattern("%\\d"))).check(doesNotExist());
		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_PROPERTY))
		)));
	}
	@Test public void testDeleteMessageWithContents() throws IOException {
		App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);

		testDeleteMessage();

		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_ROOM))
		)));
	}
	@Test public void testDeleteMessageWithContentsMultiple() throws IOException {
		App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
		App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), hasInvRoom(TEST_ROOM));
		assertThat(db.get(), hasInvRoom(TEST_ROOM_OTHER));

		testDeleteMessage();

		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_ROOM)),
				withText(containsString(TEST_ROOM_OTHER))
		)));
	}

	@Test public void testDeleteConfirm() throws IOException {
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));

		onActionMenuView(withText(R.string.property_delete)).perform(click());
		clickPositiveInDialog();

		assertThat(db.get(), not(hasInvProperty(TEST_PROPERTY)));
		assertThat(activity.getActivity(), isFinishing());
	}
	@Test public void testDeleteConfirmWithContents() throws IOException {
		App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
		assertThat(db.get(), hasInvRoom(TEST_ROOM));

		testDeleteConfirm();

		assertThat(db.get(), not(hasInvRoom(TEST_ROOM)));
	}
	@Test public void testDeleteConfirmWithContentsMultiple() throws IOException {
		App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
		App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), hasInvRoom(TEST_ROOM));
		assertThat(db.get(), hasInvRoom(TEST_ROOM_OTHER));

		testDeleteConfirm();

		assertThat(db.get(), not(hasInvRoom(TEST_ROOM)));
		assertThat(db.get(), not(hasInvRoom(TEST_ROOM_OTHER)));
	}
}
