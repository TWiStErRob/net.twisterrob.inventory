package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

import android.content.pm.PackageManager.NameNotFoundException;
import android.support.test.filters.*;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.uiautomator.UiObjectNotFoundException;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.RootMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.inventory.android.test.categories.On.Backup;

import static net.twisterrob.android.test.automators.AndroidAutomator.*;
import static net.twisterrob.android.test.automators.GoogleDriveAutomator.*;
import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;
import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Backup.Export.class, Backup.External.class})
public class BackupActivityTest_ExportExternal {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivityTest_ExportExternal.class);
	@Rule public final ActivityTestRule<BackupActivity> activity = new InventoryActivityRule<>(BackupActivity.class);
	@Rule public final TemporaryFolder temp = new TemporaryFolder();
	@Rule public final IdlingResourceRule backupService = new BackupServiceInBackupActivityIdlingRule(activity);
	@Rule public final TestName name = new TestName();

	@Before public void assertBackupActivityIsClean() {
		BackupActivityTest.assertEmptyState();
	}

	@Category({UseCase.InitialCondition.class})
	@After public void activityIsActive() {
		onView(isRoot()).perform(loopMainThreadUntilIdle()); // otherwise the assertion may fail
		assertThat(activity.getActivity(), isInStage(Stage.RESUMED));
		BackupActivityTest.assertEmptyState();
	}

	@Category({Op.Cancels.class})
	@Test public void testCancelWarning() throws Exception {
		onActionMenuView(withText(R.string.backup_export_external)).perform(click());
		onView(withId(R.id.alertTitle))
				.inRoot(isDialog())
				.check(matches(allOf(isDisplayed(), withText(R.string.backup_export_external))));

		clickNegativeInDialog();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class})
	@Test public void testCancelChooser() throws Exception {
		onActionMenuView(withText(R.string.backup_export_external)).perform(click());
		clickPositiveInDialog();

		assertThat(getChooserTitle(), isString(R.string.backup_export_external));
		pressBackExternal();
	}

	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({Op.Cancels.class})
	@Test public void testCancelDrive() throws Exception {
		assumeThat(getContext(), hasPackageInstalled(PACKAGE_GOOGLE_DRIVE));
		onActionMenuView(withText(R.string.backup_export_external)).perform(click());

		clickPositiveInDialog();
		clickOnLabel(saveToDrive());
		assertThat(getText(dialogTitle()), is(saveToDrive()));

		clickNegativeInExternalDialog();
	}

	@FlakyTest(detail = "Sometimes it doesn't find clickOnLabel(selectFolder()): UiObjectNotFoundException: UiSelector[TEXT=Select folder]")
	@SdkSuppress(minSdkVersion = UI_AUTOMATOR_VERSION)
	@Category({UseCase.Complex.class})
	@Test public void testSuccessfulFullExport() throws Exception {
		assumeThat(getContext(), hasPackageInstalled(PACKAGE_GOOGLE_DRIVE));
		onActionMenuView(withText(R.string.backup_export_external)).perform(click());

		clickPositiveInDialog();

		clickOnLabel(saveToDrive());
		{
			assertThat(activity.getActivity(), isInStage(Stage.PAUSED));
			String fileName = getText(documentTitle());
			String folder = generateFolderName();
			LOG.info("Saving {}/{}", folder, fileName);
			saveToAndroidTests(folder);
			assertThat(activity.getActivity(), isInStage(Stage.PAUSED));
			clickOnLabel(save());
		}
		onView(withText(R.string.backup_export_result_finished)).inRoot(isDialog()).check(matches(isDisplayed()));
		clickNeutralInDialog();
	}

	private void saveToAndroidTests(String folder) throws UiObjectNotFoundException, NameNotFoundException {
		assumeThat(getText(dialogTitle()), is(saveToDrive()));
		clickOn(uploadFolder());
		{
			while (!myDrive().equals(getText(dialogTitle()))) {
				shortClickOn(up());
			}
			selectTitleInList("Android Tests").click();

			clickOn(newFolder());
			{
				setText(newFolderTitle(), folder);
				clickPositiveInExternalDialog(); // OK to create folder
			}
			try {
				clickOnLabel(selectFolder());
			} catch (UiObjectNotFoundException ex) {
				LOG.warn("'{}' is flaky, try again", selectFolder(), ex);
				clickOnLabel(selectFolder());
			}
		}
	}

	private String generateFolderName() {
		return String.format(Locale.ROOT, "%s.%s@%tFT%<tH-%<tM-%<tS",
				getClass().getSimpleName(), name.getMethodName(), Calendar.getInstance());
	}
}
