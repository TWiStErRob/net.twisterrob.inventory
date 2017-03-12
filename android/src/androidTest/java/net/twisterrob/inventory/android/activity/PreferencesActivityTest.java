package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.PreferencesActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

@RunWith(AndroidJUnit4.class)
public class PreferencesActivityTest {
	@Rule public final ActivityTestRule<PreferencesActivity> activity
			= new InventoryActivityRule<>(PreferencesActivity.class);

	private final PreferencesActivityActor prefs = new PreferencesActivityActor();

	@Category({UseCase.InitialCondition.class})
	@Test public void testOpen() {
		prefs.assertIsInFront();
	}

	@Category({UseCase.InitialCondition.class})
	@Test public void testInfoAbout() {
		prefs.openAbout().close();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({UseCase.InitialCondition.class, On.External.class})
	@Test public void testInfoSettings() {
		prefs.openAppInfoInSettings();
		CharSequence name = getTargetContext().getApplicationInfo().loadLabel(getTargetContext().getPackageManager());

		UiDevice device = UiDevice.getInstance(getInstrumentation());
		device.waitForIdle();
		try {
			UiObject object = device.findObject(new UiSelector().text(name.toString()));
			assertTrue(object.exists());
		} finally {
			pressBackExternal();
		}
		prefs.assertIsInFront();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({UseCase.InitialCondition.class, On.External.class})
	@Test public void testInfoStore() {
		assumeThat(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=")),
				canBeResolvedTo(notNullValue(ResolveInfo.class)));

		prefs.openAppInfoInMarket();

		UiDevice device = UiDevice.getInstance(getInstrumentation());
		device.waitForIdle();
		try {
			assertThat(device.getCurrentPackageName(), is("com.android.vending"));
		} finally {
			pressBackExternal();
		}
		prefs.assertIsInFront();
	}

	@Category({UseCase.Prefs.class})
	@Test public void testDefaultViewPageChange() {
		String preValue = App.prefs().getString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_default);
		assumeThat(preValue, not(isString(R.string.pref_defaultViewPage_image)));

		prefs.setDetailsPage(R.string.pref_defaultViewPage_image_title);

		String postValue = App.prefs().getString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_default);
		assertThat(postValue, isString(R.string.pref_defaultViewPage_image));
	}
}
