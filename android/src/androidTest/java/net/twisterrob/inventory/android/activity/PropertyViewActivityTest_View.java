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
import net.twisterrob.inventory.android.activity.data.PropertyViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.PropertyViewActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Property.class})
public class PropertyViewActivityTest_View {
	@Rule public final ActivityTestRule<PropertyViewActivity> activity
			= new InventoryActivityRule<PropertyViewActivity>(PropertyViewActivity.class, false, false) {
		@Override protected void setDefaults() {
			super.setDefaults();
			App.prefs().setString(R.string.pref_defaultViewPage, defaultViewPage);
			getStartIntent().putExtras(Intents.bundleFromProperty(db.createProperty(TEST_PROPERTY)));
		}
	};
	@Rule public final DataBaseActor db = new AppSingletonDatabaseActor();
	private final PropertyViewActivityActor propertyView = new PropertyViewActivityActor();
	private @StringRes int defaultViewPage = R.string.pref_defaultViewPage_default;

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageAutomatic() {
		defaultViewPage = R.string.pref_defaultViewPage_auto;
		activity.launchActivity(new Intent());

		propertyView.assertImageVisible();
		propertyView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageImage() {
		defaultViewPage = R.string.pref_defaultViewPage_image;
		activity.launchActivity(new Intent());

		propertyView.assertImageVisible();
		propertyView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageDetails() {
		defaultViewPage = R.string.pref_defaultViewPage_details;
		activity.launchActivity(new Intent());

		propertyView.assertDetailsVisible();
		propertyView.assertDetailsText(containsString(TEST_PROPERTY));
	}
}
