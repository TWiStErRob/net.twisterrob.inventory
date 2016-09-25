package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
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
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

@RunWith(AndroidJUnit4.class)
public class ItemViewActivityTest_Move {
	@Rule public final ActivityTestRule<ItemViewActivity> activity
			= new InventoryActivityRule<ItemViewActivity>(ItemViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			propertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY, NO_DESCRIPTION);
			roomID = App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
			itemID = App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);
			getStartIntent().putExtras(Intents.bundleFromParent(itemID));
		}
	};
	@Rule public final TestDatabaseRule db = new TestDatabaseRule();
	private long propertyID;
	private long roomID;
	private long itemID;

	@Before public void preconditions() {
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));
		assertThat(db.get(), both(hasInvRoom(TEST_ROOM)).and(hasInvRoomInProperty(TEST_PROPERTY, TEST_ROOM)));
		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemInRoom(TEST_ROOM, TEST_ITEM)));
	}
	@After public void closeDialog() {
		attemptCloseDialog();
	}

	@Test public void testMoveCancel() throws IOException {
		assertThat(db.get(), hasInvItem(TEST_ITEM));

		onActionBarDescendant(withId(R.id.action_item_move)).perform(click());
		{
			MoveTargetActivityActions.cancel();
		}

		assertThat(db.get(), hasInvItem(TEST_ITEM));
	}

	@Test public void testMoveNoMove() throws IOException {
		assertThat(db.get(), hasInvItem(TEST_ITEM));

		onActionBarDescendant(withId(R.id.action_item_move)).perform(click());
		{
			MoveTargetActivityActions.confirm();
		}

		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemInRoom(TEST_ROOM, TEST_ITEM)));
	}

	@Test public void testMoveAlreadyExists() throws IOException {
		long targetID = App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM_OTHER, NO_DESCRIPTION);
		long duplicateID = App.db().createItem(targetID, Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvItem(TEST_ITEM_OTHER)).and(hasInvItemInRoom(TEST_ROOM, TEST_ITEM_OTHER)));
		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemIn(TEST_ITEM_OTHER, TEST_ITEM)));

		onActionBarDescendant(withId(R.id.action_item_move)).perform(click());
		{
			MoveTargetActivityActions.selectItem(TEST_ITEM_OTHER);
			MoveTargetActivityActions.confirm();
		}

		assertThat(db.get(), hasInvItem(TEST_ITEM));
		assertThat(db.get(), hasInvItemInRoom(TEST_ROOM, TEST_ITEM));
		assertThat(db.get(), hasInvItemIn(TEST_ITEM_OTHER, TEST_ITEM));

		onView(isDialogMessage()).inRoot(isToast()).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_ITEM)),
				withText(containsStringRes(R.string.generic_error_unique_name)),
				withText(containsString(TEST_ITEM_OTHER))
		)));
	}

	@Test public void testMoveToAnotherItem() throws IOException {
		long targetID = App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvItem(TEST_ITEM_OTHER)).and(hasInvItemInRoom(TEST_ROOM, TEST_ITEM_OTHER)));

		onActionBarDescendant(withId(R.id.action_item_move)).perform(click());
		{
			MoveTargetActivityActions.selectItem(TEST_ITEM_OTHER);
			MoveTargetActivityActions.confirm();
		}

		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemIn(TEST_ITEM_OTHER, TEST_ITEM)));
		assertItemOpenedWithVisibleItem(targetID, TEST_ITEM);
	}
	@Test public void testMoveToAnotherItemWithContents() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));

		testMoveToAnotherItem();

		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));
	}

	@Test public void testMoveToOtherRoom() throws IOException {
		long targetID = App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), hasInvRoom(TEST_ROOM_OTHER));
		assertThat(db.get(), hasInvRoomInProperty(TEST_PROPERTY, TEST_ROOM_OTHER));

		onActionBarDescendant(withId(R.id.action_item_move)).perform(click());
		{
			MoveTargetActivityActions.up();
			MoveTargetActivityActions.selectRoom(TEST_ROOM_OTHER);
			MoveTargetActivityActions.confirm();
		}

		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemInRoom(TEST_ROOM_OTHER, TEST_ITEM)));
		assertRoomOpenedWithVisibleItem(targetID, TEST_ITEM);
	}
	@Test public void testMoveToRoomWithContents() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));

		testMoveToOtherRoom();

		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));
	}

	@Test public void testMoveToItemInAnotherProperty() throws IOException {
		long otherPropertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY_OTHER, NO_DESCRIPTION);
		long otherRoomID = App.db().createRoom(otherPropertyID, RoomType.DEFAULT, TEST_ROOM_OTHER, NO_DESCRIPTION);
		long targetID =
				App.db().createItem(getRoot(otherRoomID), Category.DEFAULT, TEST_ITEM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY_OTHER));
		assertThat(db.get(), both(hasInvRoom(TEST_ROOM_OTHER))
				.and(hasInvRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM_OTHER)));
		assertThat(db.get(), both(hasInvItem(TEST_ITEM_OTHER))
				.and(hasInvItemInRoom(TEST_ROOM_OTHER, TEST_ITEM_OTHER)));

		onActionBarDescendant(withId(R.id.action_item_move)).perform(click());
		{
			MoveTargetActivityActions.up();
			MoveTargetActivityActions.up();
			MoveTargetActivityActions.selectProperty(TEST_PROPERTY_OTHER);
			MoveTargetActivityActions.selectRoom(TEST_ROOM_OTHER);
			MoveTargetActivityActions.selectItem(TEST_ITEM_OTHER);
			MoveTargetActivityActions.confirm();
		}

		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemIn(TEST_ITEM_OTHER, TEST_ITEM)));
		assertItemOpenedWithVisibleItem(targetID, TEST_ITEM);
	}
	@Test public void testMoveToItemInAnotherPropertyWithContents() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));

		testMoveToItemInAnotherProperty();

		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));
	}

	@Test public void testMoveToRoomInAnotherProperty() throws IOException {
		long otherPropertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY_OTHER, NO_DESCRIPTION);
		long targetID = App.db().createRoom(otherPropertyID, RoomType.DEFAULT, TEST_ROOM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY_OTHER));
		assertThat(db.get(), both(hasInvRoom(TEST_ROOM_OTHER))
				.and(hasInvRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM_OTHER)));

		onActionBarDescendant(withId(R.id.action_item_move)).perform(click());
		{
			MoveTargetActivityActions.up();
			MoveTargetActivityActions.up();
			MoveTargetActivityActions.selectProperty(TEST_PROPERTY_OTHER);
			MoveTargetActivityActions.selectRoom(TEST_ROOM_OTHER);
			MoveTargetActivityActions.confirm();
		}

		assertThat(db.get(), both(hasInvItem(TEST_ITEM)).and(hasInvItemInRoom(TEST_ROOM_OTHER, TEST_ITEM)));
		assertRoomOpenedWithVisibleItem(targetID, TEST_ITEM);
	}
	@Test public void testMoveToRoomInAnotherPropertyWithContents() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));

		testMoveToRoomInAnotherProperty();

		assertThat(db.get(), both(hasInvItem(TEST_SUBITEM)).and(hasInvItemIn(TEST_ITEM, TEST_SUBITEM)));
	}

	private void assertItemOpenedWithVisibleItem(long itemID, String itemName) {
		intended(allOf(hasComponent(ItemViewActivity.class.getName()), hasExtra(Extras.PARENT_ID, itemID)));
		assertThat(activity.getActivity(), isFinishing());
		onRecyclerItem(withText(itemName)).check(matches(isDisplayed()));
	}
	private void assertRoomOpenedWithVisibleItem(long roomID, String itemName) {
		intended(allOf(hasComponent(RoomViewActivity.class.getName()), hasExtra(Extras.ROOM_ID, roomID)));
		assertThat(activity.getActivity(), isFinishing());
		onRecyclerItem(withText(itemName)).check(matches(isDisplayed()));
	}
}
