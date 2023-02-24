package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.content.Intent;

import androidx.annotation.StringRes;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.RoomViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.RoomViewActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Room.class})
public class RoomViewActivityTest_View {

	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<RoomViewActivity> activity
			= new InventoryActivityRule<RoomViewActivity>(RoomViewActivity.class, false, false) {
		@Override protected void setDefaults() {
			super.setDefaults();
			App.prefs().setString(R.string.pref_defaultViewPage, defaultViewPage);
			getStartIntent().putExtras(Intents.bundleFromRoom(db.create(TEST_PROPERTY, TEST_ROOM)));
		}
	};
	@Rule public final DataBaseActor db = new AppSingletonDatabaseActor();
	private final RoomViewActivityActor roomView = new RoomViewActivityActor();
	private @StringRes int defaultViewPage = R.string.pref_defaultViewPage_default;

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageAutomatic() {
		defaultViewPage = R.string.pref_defaultViewPage_auto;
		activity.launchActivity(new Intent());

		roomView.assertImageVisible();
		roomView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageImage() {
		defaultViewPage = R.string.pref_defaultViewPage_image;
		activity.launchActivity(new Intent());

		roomView.assertImageVisible();
		roomView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageDetails() {
		defaultViewPage = R.string.pref_defaultViewPage_details;
		activity.launchActivity(new Intent());

		roomView.assertDetailsVisible();
		roomView.assertDetailsText(containsString(TEST_ROOM));
	}
}
