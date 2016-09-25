package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.activity.CaptureImageActivityActor;
import net.twisterrob.android.test.Helpers;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.RoomViewActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

@RunWith(AndroidJUnit4.class)
public class ItemActivityTest_Image {
	@Rule public TemporaryFolder temp = new TemporaryFolder();
	@Rule public TestName name = new TestName();

	@Rule public final ActivityTestRule<RoomViewActivity> activity
			= new InventoryActivityRule<RoomViewActivity>(RoomViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY, NO_DESCRIPTION);
			long roomID = App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
			getStartIntent().putExtras(Intents.bundleFromRoom(roomID));
		}
	};
	@Rule public final TestDatabaseRule db = new TestDatabaseRule();
	private final CaptureImageActivityActor capture = new CaptureImageActivityActor();

	@LargeTest
	@Test public void testImageDeletedWithItem() throws IOException {
		onView(withId(R.id.fab)).perform(click());
		onView(allOf(withId(R.id.title), isAssignableFrom(EditText.class))).perform(typeText(TEST_ITEM));
		onView(withId(R.id.btn_save)).perform(click());

		assertThat(db.get(), hasInvItem(TEST_ITEM));
		assertThat(db.get(), countImages(is(0L)));

		onRecyclerItem(withText(TEST_ITEM)).perform(click());
		onView(withId(R.id.action_item_edit)).perform(click());

		Bitmap bitmap = Helpers.createMockBitmap(TEST_ITEM + "\n" + name.getMethodName());
		Matcher<Intent> cameraIntent = capture.intendCamera(temp.newFile(), bitmap);
		onView(withId(R.id.action_picture_get)).perform(click());
		intended(cameraIntent);
		onView(withId(R.id.btn_save)).perform(click());

		assertThat(db.get(), hasInvItem(TEST_ITEM));
		assertThat(db.get(), countImages(is(1L)));

		onActionMenuView(withText(R.string.item_delete)).perform(click());
		clickPositiveInDialog();

		assertThat(db.get(), not(hasInvItem(TEST_ITEM)));
		assertThat(db.get(), countImages(is(0L)));
	}
}
