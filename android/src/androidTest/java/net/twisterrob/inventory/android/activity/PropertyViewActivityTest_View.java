package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

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
			propertyID = db.createProperty(TEST_PROPERTY);
			getStartIntent().putExtras(Intents.bundleFromProperty(propertyID));
		}
	};
	@Rule public final DataBaseActor db = new DataBaseActor();
	private long propertyID;
	private final PropertyViewActivityActor propertyView = new PropertyViewActivityActor();

	@Before public void preconditionsForViewingAProperty() {
		db.assertHasProperty(TEST_PROPERTY);
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageAutomatic() {
		App.prefs().setString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_auto);
		activity.launchActivity(null);

		propertyView.assertImageVisible();
		propertyView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageImage() {
		App.prefs().setString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_image);
		activity.launchActivity(null);

		propertyView.assertImageVisible();
		propertyView.assertTypeVisible();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testViewPageDetails() {
		App.prefs().setString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_details);
		activity.launchActivity(null);

		propertyView.assertDetailsVisible();
		propertyView.assertDetailsText(containsString(TEST_PROPERTY));
	}
}
