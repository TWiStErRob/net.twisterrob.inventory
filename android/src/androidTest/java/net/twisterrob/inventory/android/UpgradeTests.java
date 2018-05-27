package net.twisterrob.inventory.android;

import java.io.File;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.io.FileMatchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

import android.content.*;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.Espresso;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

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
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.test.activity.CompatibleLauncher;
import net.twisterrob.test.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.DrawerMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.test.hamcrest.IsMapContainsEntries.*;
import static net.twisterrob.test.hamcrest.Matchers.*;

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
	private static final Logger LOG = LoggerFactory.getLogger(UpgradeTests.class);

	private static final int MB = 1000 * 1000;
	private static final String IMPORT_FILE = "data.zip";
	public static final String LAUNCH_KEY = "upgrade";

	@Rule public final ActivityTestRule<CompatibleLauncher> activity =
			new TestPackageIntentRule<>(CompatibleLauncher.class);
	@Rule public final IdlingResourceRule drawer = DrawerIdlingResource.rule();
	private File downloads;
	private File db;

	/** @see net.twisterrob.inventory.android.test.activity.CompatibleLauncher */
	@Before public void launchMain() {
		assumeThat("Only run when instrumentation arguments contain \"" + LAUNCH_KEY + "\"",
				getArguments(), hasKey(LAUNCH_KEY));
		downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		db = getTargetContext().getDatabasePath(Database.NAME);
		assumeThat(downloads, anExistingDirectory());
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
		intended(hasComponent("net.twisterrob.inventory.android.activity.BackupActivity"));

		assertThat("empty DB", db, aFileWithSize(between(0, 0.5)));

		assertThat(new File(downloads, IMPORT_FILE), aFileWithSize(greaterThan(0L)));
		onRecyclerItem(withText(IMPORT_FILE)).perform(click());
		clickNeutralInDialog();

		assertThat("imported data size", db, aFileWithSize(between(0.5, 1.5)));

		Espresso.pressBack(); // exit Backup activity and go to Main activity

		onOpenDrawerDescendant(withText("Properties")).perform(click());
		onView(allOf(isRV(), inDrawerContents()))
				.perform(actionOnItemAtPosition(0, longClick()))
				.perform(actionOnItemAtPosition(1, longClick()))
		;
		onView(withContentDescription("Delete")).perform(click());
		clickPositiveInDialog();

		assertThat("dangling images", db, aFileWithSize(between(0.5, 1.5)));

		onOpenDrawerDescendant(withText("Backup")).perform(click());
		intended(hasComponent("net.twisterrob.inventory.android.activity.BackupActivity"), times(2));
		onRecyclerItem(withText(IMPORT_FILE)).perform(click());
		clickNeutralInDialog();

		assertThat("re-imported data with dangling images", db, aFileWithSize(between(1.5, 2.5)));
	}

	@FlakyTest(detail = "On 2.3.7 it sometimes taps on Properties instead of Backup in the drawer")
	@Test public void testVerifyVersion2() throws Throwable {
		assertThat("Second released version", BuildConfig.class, hasVersionCode(greaterThanOrEqualTo(10002111)));
		assertNoDialogIsDisplayed();
		assertThat("upgraded size without dangling images", db, aFileWithSize(between(0.5, 1.5)));

		onOpenDrawerDescendant(withText("Backup")).perform(click());
		try {
			intended(hasComponent("net.twisterrob.inventory.android.activity.BackupActivity"));
		} catch (junit.framework.AssertionFailedError ex) { // for some reason intended throws this old shit
			LOG.warn("Trying to go to backup activity again.", ex);
			onOpenDrawerDescendant(withText("Backup")).perform(click());
		}

		FolderDiff diff = new FolderDiff(downloads);
		onView(isAssignableFrom(FloatingActionButton.class)).perform(click());
		clickNeutralInDialog();
		Collection<File> addedFiles = diff.getAdded();
		assertThat(addedFiles, hasSize(1));

		final String dataXml = Paths.BACKUP_DATA_FILENAME;
		File original = new File(downloads, IMPORT_FILE);
		File exported = addedFiles.iterator().next();
		assertThat(exported, aFileWithSize(greaterThan(0L)));

		ZipDiff zip = new ZipDiff(original, exported);
		LOG.trace("Zip Diff: {}", zip);
		assertThat(zip.getRemoved(), anEmptyMap());
		assertThat(zip.getAdded(), not(hasValue(notNullValue())));
		assertThat(zip.getAdded(), not(hasValue(zipEntryWithName(endsWithIgnoringCase(".jpg")))));
		assertThat(zip.getChanged(), containsEntries(hasValue(zipEntryWithName(dataXml))));
		// TODO diff XML
		//String beforeXml = IOTools.readAll(new ZipFile(original).getInputStream(new ZipEntry(dataXml)));
		//String afterXml = IOTools.readAll(new ZipFile(exported).getInputStream(new ZipEntry(dataXml)));
	}

	private Matcher<Long> between(double lower, double upper) {
		return allOf(greaterThanOrEqualTo((long)(lower * MB)), lessThanOrEqualTo((long)(upper * MB)));
	}
}
