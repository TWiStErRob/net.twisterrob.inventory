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
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.containsString;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

@RunWith(AndroidJUnit4.class)
public class RoomViewActivityTest_Move {
	@Rule public final ActivityTestRule<RoomViewActivity> activity
			= new InventoryActivityRule<RoomViewActivity>(RoomViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY, NO_DESCRIPTION);
			roomID = App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
			getStartIntent().putExtras(Intents.bundleFromRoom(roomID));
			targetID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY_OTHER, NO_DESCRIPTION);
		}
	};
	@Rule public final TestDatabaseRule db = new TestDatabaseRule();
	private long roomID;
	private long targetID;

	@Before public void preconditions() {
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));
		assertThat(db.get(), both(hasInvRoom(TEST_ROOM)).and(hasInvRoomInProperty(TEST_PROPERTY, TEST_ROOM)));
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY_OTHER));
		assertThat(db.get(), not(hasInvRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM)));
	}
	@After public void closeDialog() {
		ensureDialogClosed();
	}

	@Test public void testMoveCancel() throws IOException {
		assertThat(db.get(), hasInvRoom(TEST_ROOM));

		onActionMenuView(withText(R.string.room_move)).perform(click());
		{
			MoveTargetActivityActions.cancel();
		}

		assertThat(db.get(), hasInvRoom(TEST_ROOM));
	}

	@Test public void testMoveConfirmMessage() throws IOException {
		onActionMenuView(withText(R.string.room_move)).perform(click());
		{
			MoveTargetActivityActions.selectProperty(TEST_PROPERTY_OTHER);
			MoveTargetActivityActions.confirm();
		}

		onView(isRoot()).check(matches(not(hasDescendant(withText(matchesPattern("%\\d"))))));
		onView(isRoot()).check(matches(hasDescendant(withText(containsString(TEST_ROOM)))));
		onView(isRoot()).check(matches(hasDescendant(withText(containsString(TEST_PROPERTY)))));
	}

	@Test public void testMoveConfirmCancel() throws IOException {
		onActionMenuView(withText(R.string.room_move)).perform(click());
		{
			MoveTargetActivityActions.selectProperty(TEST_PROPERTY_OTHER);
			MoveTargetActivityActions.confirm();
		}
		clickNegativeInDialog();

		assertThat(db.get(), both(hasInvRoom(TEST_ROOM)).and(hasInvRoomInProperty(TEST_PROPERTY, TEST_ROOM)));
		assertRecyclerItemDoesNotExists(withText(TEST_ROOM));
	}

	@Test public void testMove() throws IOException {
		onActionMenuView(withText(R.string.room_move)).perform(click());
		{
			MoveTargetActivityActions.selectProperty(TEST_PROPERTY_OTHER);
			MoveTargetActivityActions.confirm();
		}
		clickPositiveInDialog();

		assertThat(db.get(), both(hasInvRoom(TEST_ROOM)).and(hasInvRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM)));
		assertPropertyOpenedWithVisibleRoom(targetID, TEST_ROOM);
	}
	@Test public void testMoveWithContents() throws IOException {
		long itemID = App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemInRoom(TEST_ROOM, TEST_ITEM)));
		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));

		testMove();

		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemInRoom(TEST_ROOM, TEST_ITEM)));
		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));
	}

	@Test public void testMoveAlreadyExists() throws IOException {
		long duplicateID = App.db().createRoom(targetID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvRoom(TEST_ROOM)).and(hasInvRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM)));

		onActionMenuView(withText(R.string.room_move)).perform(click());
		{
			MoveTargetActivityActions.selectProperty(TEST_PROPERTY_OTHER);
			MoveTargetActivityActions.confirm();
		}
		clickPositiveInDialog();

		assertThat(db.get(), hasInvRoom(TEST_ROOM));
		assertThat(db.get(), hasInvRoomInProperty(TEST_PROPERTY, TEST_ROOM));
		assertThat(db.get(), hasInvRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM));

		onView(isRoot())
				.inRoot(isToast())
				.check(matches(hasDescendant(withText(containsString(TEST_ROOM)))))
				.check(matches(hasDescendant(withText(containsString(R.string.generic_error_unique_name)))))
				.check(matches(hasDescendant(withText(containsString(TEST_PROPERTY_OTHER)))))
		;
	}

	private void assertPropertyOpenedWithVisibleRoom(long propertyID, String roomName) {
		intended(allOf(hasComponent(PropertyViewActivity.class.getName()), hasExtra(Extras.PROPERTY_ID, propertyID)));
		assertThat(activity.getActivity(), isFinishing());
		onRecyclerItem(withText(roomName)).check(matches(isDisplayed()));
	}
}
