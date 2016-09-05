package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;
import static net.twisterrob.inventory.android.test.InventoryEspressoUtils.*;

@RunWith(AndroidJUnit4.class)
public class ItemActivityTest_Image {
	public static final String TEST_ITEM_NAME = "Test Item";
	@Rule public TemporaryFolder temp = new TemporaryFolder();
	@Rule public TestName name = new TestName();

	@Rule public final ActivityTestRule<MainActivity> activity
			= new InventoryActivityRule<MainActivity>(MainActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = App.db().createProperty(PropertyType.DEFAULT, "Test Property", null);
			long roomID = App.db().createRoom(propertyID, RoomType.DEFAULT, "Test Room", null);
		}
	};
	private final Database db = new Database(getTargetContext(), getContext().getResources());

	@Before public void goToRoom() {
		onRecyclerItem(withText("Test Room"))
				.inAdapterView(withId(R.id.rooms))
				.onChildView(withId(R.id.details))
				.check(matches(withText(containsString("Test Property"))))
				.perform(click())
		;
	}

	@LargeTest
	@Test public void testImageDeletedWithItem() throws IOException {
		onView(withId(R.id.fab)).perform(click());
		onView(allOf(withId(R.id.title), isAssignableFrom(EditText.class))).perform(typeText(TEST_ITEM_NAME));
		onView(withId(R.id.btn_save)).perform(click());

		assertThat(db, hasInvItem(TEST_ITEM_NAME));
		assertThat(db, countImages(is(0L)));

		onRecyclerItem(withText(TEST_ITEM_NAME)).perform(click());
		onView(withId(R.id.action_item_edit)).perform(click());

		Matcher<Intent> cameraIntent = intendCamera(temp.newFile(), TEST_ITEM_NAME + "\nin " + name.getMethodName());
		onView(withId(R.id.action_picture_get)).perform(click());
		intended(cameraIntent);
		onView(withId(R.id.btn_save)).perform(click());

		assertThat(db, hasInvItem(TEST_ITEM_NAME));
		assertThat(db, countImages(is(1L)));

		onActionMenuView(withText(R.string.item_delete)).perform(click());
		clickPositiveInDialog();

		assertThat(db, not(hasInvItem(TEST_ITEM_NAME)));
		assertThat(db, countImages(is(0L)));
	}
}
