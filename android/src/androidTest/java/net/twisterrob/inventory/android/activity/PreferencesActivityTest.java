package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;
import static org.junit.Assert.*;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.*;

import static androidx.test.core.app.ApplicationProvider.*;

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
		waitForAppToBeBackgrounded();

		CharSequence name = getApplicationContext().getApplicationInfo().loadLabel(getApplicationContext().getPackageManager());
		try {
			UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
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
		waitForAppToBeBackgrounded();
		try {
			UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
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
