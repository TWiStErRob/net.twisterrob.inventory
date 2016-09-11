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
import net.twisterrob.inventory.android.activity.data.RoomViewActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

@RunWith(AndroidJUnit4.class)
public class RoomViewActivityTest_Delete {
	@Rule public final ActivityTestRule<RoomViewActivity> activity
			= new InventoryActivityRule<RoomViewActivity>(RoomViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY, NO_DESCRIPTION);
			roomID = App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
			getStartIntent().putExtras(Intents.bundleFromRoom(roomID));
		}
	};
	@Rule public final TestDatabaseRule db = new TestDatabaseRule();
	private long roomID;

	@Before public void preconditions() {
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));
		assertThat(db.get(), hasInvRoom(TEST_ROOM));
	}
	@After public void closeDialog() {
		attemptCloseDialog();
	}

	@Test public void testDeleteCancel() throws IOException {
		assertThat(db.get(), hasInvRoom(TEST_ROOM));

		onActionMenuView(withText(R.string.room_delete)).perform(click());
		clickNegativeInDialog();

		assertThat(db.get(), hasInvRoom(TEST_ROOM));
	}

	@Test public void testDeleteMessage() throws IOException {
		onActionMenuView(withText(R.string.room_delete)).perform(click());

		onView(withText(matchesPattern("%\\d"))).check(doesNotExist());
		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_ROOM))
		)));
	}
	@Test public void testDeleteMessageWithContents() throws IOException {
		App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);

		testDeleteMessage();

		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_ITEM))
		)));
	}
	@Test public void testDeleteMessageWithContentsMultiple() throws IOException {
		App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);
		App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM_OTHER, NO_DESCRIPTION);

		testDeleteMessage();

		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_ITEM)),
				withText(containsString(TEST_ITEM_OTHER))
		)));
	}

	@Test public void testDeleteConfirm() throws IOException {
		assertThat(db.get(), hasInvRoom(TEST_ROOM));

		onActionMenuView(withText(R.string.room_delete)).perform(click());
		clickPositiveInDialog();

		assertThat(db.get(), not(hasInvRoom(TEST_ROOM)));
		assertThat(activity.getActivity(), isFinishing());
	}
	@Test public void testDeleteConfirmWithContents() throws IOException {
		App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);
		assertThat(db.get(), hasInvItem(TEST_ITEM));

		testDeleteConfirm();

		assertThat(db.get(), not(hasInvItem(TEST_ITEM)));
	}
	@Test public void testDeleteConfirmWithContentsMultiple() throws IOException {
		App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);
		App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), hasInvItem(TEST_ITEM));
		assertThat(db.get(), hasInvItem(TEST_ITEM_OTHER));

		testDeleteConfirm();

		assertThat(db.get(), not(hasInvItem(TEST_ITEM)));
		assertThat(db.get(), not(hasInvItem(TEST_ITEM_OTHER)));
	}
}
