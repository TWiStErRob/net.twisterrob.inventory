package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.ItemViewActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Item.class})
public class ItemViewActivityTest_View {
	@Rule public final ActivityTestRule<ItemViewActivity> activity
			= new InventoryActivityRule<ItemViewActivity>(ItemViewActivity.class, false, false) {
		@Override protected void setDefaults() {
			super.setDefaults();
			App.prefs().setString(R.string.pref_defaultViewPage, defaultViewPage);
			getStartIntent().putExtras(Intents.bundleFromParent(db.create(TEST_PROPERTY, TEST_ROOM, TEST_ITEM)));
		}
	};
	@Rule public final DataBaseActor db = new DataBaseActor();
	private final ItemViewActivityActor itemView = new ItemViewActivityActor();
	private @StringRes int defaultViewPage = R.string.pref_defaultViewPage_default;

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageAutomatic() {
		defaultViewPage = R.string.pref_defaultViewPage_auto;
		activity.launchActivity(new Intent());

		itemView.assertImageVisible();
		itemView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageImage() {
		defaultViewPage = R.string.pref_defaultViewPage_image;
		activity.launchActivity(new Intent());

		itemView.assertImageVisible();
		itemView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageDetails() {
		defaultViewPage = R.string.pref_defaultViewPage_details;
		activity.launchActivity(new Intent());

		itemView.assertDetailsVisible();
		itemView.assertDetailsText(containsString(TEST_ITEM));
	}
}
