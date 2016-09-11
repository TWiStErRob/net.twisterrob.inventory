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
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
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
public class ItemViewActivityTest_Delete {
	@Rule public final ActivityTestRule<ItemViewActivity> activity
			= new InventoryActivityRule<ItemViewActivity>(ItemViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY, NO_DESCRIPTION);
			long roomID = App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
			itemID = App.db().createItem(getRoot(roomID), Category.DEFAULT, TEST_ITEM, NO_DESCRIPTION);
			getStartIntent().putExtras(Intents.bundleFromParent(itemID));
		}
	};
	@Rule public final TestDatabaseRule db = new TestDatabaseRule();
	private long itemID;

	@Before public void preconditions() {
		assertThat(db.get(), hasInvProperty(TEST_PROPERTY));
		assertThat(db.get(), hasInvRoom(TEST_ROOM));
		assertThat(db.get(), hasInvItem(TEST_ITEM));
	}
	@After public void closeDialog() {
		attemptCloseDialog();
	}

	@Test public void testDeleteCancel() throws IOException {
		assertThat(db.get(), hasInvItem(TEST_ITEM));

		onActionMenuView(withText(R.string.item_delete)).perform(click());
		clickNegativeInDialog();

		assertThat(db.get(), hasInvItem(TEST_ITEM));
	}

	@Test public void testDeleteMessage() throws IOException {
		onActionMenuView(withText(R.string.item_delete)).perform(click());

		onView(withText(matchesPattern("%\\d"))).check(doesNotExist());
		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_ITEM))
		)));
	}
	@Test public void testDeleteMessageWithContents() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);

		testDeleteMessage();

		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_SUBITEM))
		)));
	}
	@Test public void testDeleteMessageWithContentsMultiple() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM_OTHER, NO_DESCRIPTION);

		testDeleteMessage();

		onView(withId(android.R.id.message)).check(matches(allOf(
				isDisplayed(),
				withText(containsString(TEST_SUBITEM)),
				withText(containsString(TEST_SUBITEM_OTHER))
		)));
	}

	@Test public void testDeleteConfirm() throws IOException {
		assertThat(db.get(), hasInvItem(TEST_ITEM));

		onActionMenuView(withText(R.string.item_delete)).perform(click());
		clickPositiveInDialog();

		assertThat(db.get(), not(hasInvItem(TEST_ITEM)));
		assertThat(activity.getActivity(), isFinishing());
	}
	@Test public void testDeleteConfirmWithContents() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		assertThat(db.get(), hasInvItem(TEST_SUBITEM));

		testDeleteConfirm();

		assertThat(db.get(), not(hasInvItem(TEST_SUBITEM)));
	}
	@Test public void testDeleteConfirmWithContentsMultiple() throws IOException {
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM, NO_DESCRIPTION);
		App.db().createItem(itemID, Category.DEFAULT, TEST_SUBITEM_OTHER, NO_DESCRIPTION);
		assertThat(db.get(), hasInvItem(TEST_SUBITEM));
		assertThat(db.get(), hasInvItem(TEST_SUBITEM_OTHER));

		testDeleteConfirm();

		assertThat(db.get(), not(hasInvItem(TEST_SUBITEM)));
		assertThat(db.get(), not(hasInvItem(TEST_SUBITEM_OTHER)));
	}
}
