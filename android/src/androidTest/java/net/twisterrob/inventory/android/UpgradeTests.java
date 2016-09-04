package net.twisterrob.inventory.android;

import java.io.File;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;
import static org.junit.Assert.assertThat;

import android.content.*;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.contrib.RecyclerViewActions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasKey;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.idle.DrawerIdlingResource;
import net.twisterrob.android.test.junit.*;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.Database;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.DrawerMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

/**
 * <p>
 * <b>These tests need to be excluded from normal {@code gradle connectedCheck} runs.</b>
 * For reason this they require to be run with
 * {@code adb shell am instrument -e class <specific method> -e upgrade true <AndroidJUnitRunner>}.
 * If run without the {@code -e upgrade true}
 * the assumption about {@link android.support.test.InstrumentationRegistry#getArguments() getArguments()} will fail
 * and hence nothing will run any further.
 * This will be the case when no specific {@code -e class} or {@code -e package} filters are defined, which is good.
 * </p>
 *
 * <p><i><b>
 * Be careful with {@link android.support.test.InstrumentationRegistry#getArguments() getArguments()},
 * it only contains arguments passed in via {@code am instrument -e}, not the ones defined via {@code <meta-data>}.
 * Those need to be manually read via {@link android.content.pm.PackageManager#getInstrumentationInfo
 * packageManager.getInstrumentationInfo().metaData}.
 * </b></i></p>
 *
 * <p><i>Also tried to centralize the exclusion via:
 * {@code <instrumentation><meta-data android:name="notClass" android:value="....UpgradeTests" /></instrumentation>}
 * in {@code androidTest/AndroidManifest.xml}. If both {@code notClass} and {@code class} arguments are specified,
 * the {@code notClass} wins and the test suite will resolve to empty, even when instrumenting a specific method.
 * </i></p>
 *
 * @see  android.support.test.internal.runner.TestRequestBuilder#addFromRunnerArgs
 * how the negative overriding logic is implemented
 * @see android.support.test.InstrumentationRegistry#getArguments()
 */
@RunWith(AndroidJUnit4.class)
public class UpgradeTests {
	private static final int MB = 1000 * 1000;
	private static final String IMPORT_FILE = "data.zip";

	@Rule public final ActivityTestRule<CompatibleLauncher> activity =
			new TestPackageIntentRule<>(CompatibleLauncher.class);
	@Rule public final IdlingResourceRule drawer = DrawerIdlingResource.rule();

	/** @see net.twisterrob.inventory.android.activity.CompatibleLauncher */
	@Before public void launchMain() {
		assumeThat(getArguments(), hasKey("upgrade"));
		// It's a good idea anyway to not be dependent upon anything from any particular version.
		// The below intent could be a string, no need to load the Activity class to create the intent.
		activity.getActivity().startActivity(new Intent()
				.setComponent(new ComponentName(getTargetContext(), MainActivity.class))
				.setAction(Intent.ACTION_MAIN)
				.addCategory(Intent.CATEGORY_LAUNCHER)
		);
	}

	@Test public void testPrepareVersion1() throws Throwable {
		assertThat("First released version", BuildConfig.class, hasVersionCode(is(10001934)));
		clickNeutralInDialog();
		intended(hasComponent(BackupActivity.class.getName()));

		File db = getTargetContext().getDatabasePath(Database.NAME);

		assertThat("empty DB", (double)db.length(), between(0, 0.5));
		onRecyclerItem(withText(IMPORT_FILE)).perform(click());
		clickNeutralInDialog();

		assertThat("imported data size", (double)db.length(), between(0.5, 1.5));

		Espresso.pressBack(); // exit Backup activity and go to Main activity

		onDrawerDescendant(withText("Properties")).perform(click());
		onView(allOf(isAssignableFrom(RecyclerView.class), inDrawerContents()))
				.perform(actionOnItemAtPosition(0, longClick()))
				.perform(actionOnItemAtPosition(1, longClick()))
		;
		onView(withContentDescription("Delete")).perform(click());
		clickPositiveInDialog();

		assertThat("dangling images", (double)db.length(), between(0.5, 1.5));

		onDrawerDescendant(withText("Backup")).perform(click());
		onRecyclerItem(withText(IMPORT_FILE)).perform(click());
		clickNeutralInDialog();

		assertThat("re-imported data with dangling images", (double)db.length(), between(1.5, 2.5));
	}

	private Matcher<Double> between(double lower, double upper) {
		return both(greaterThanOrEqualTo(lower * MB)).and(lessThanOrEqualTo(upper * MB));
	}

	@Test public void testVerifyVersion2() throws Throwable {
		assertThat("Second released version", BuildConfig.class, hasVersionCode(greaterThanOrEqualTo(10002111)));
		assertNoDialogIsDisplayed();
		File db = getTargetContext().getDatabasePath(Database.NAME);
		assertThat("upgraded size without dangling images", (double)db.length(), between(0.5, 1.5));
	}
}
