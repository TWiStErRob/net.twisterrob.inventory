package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION_CODES;
import android.support.test.filters.SdkSuppress;
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

import static net.twisterrob.android.test.automators.AndroidAutomator.*;
import static net.twisterrob.android.test.automators.GoogleDriveAutomator.*;
import static net.twisterrob.android.test.automators.UiAutomatorExtensions.*;
import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.junit.SensibleActivityTestRule.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.activity.BackupActivityTest.*;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = VERSION_CODES.JELLY_BEAN_MR2)
public class BackupActivityTest_ExportExternal {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivityTest_ExportExternal.class);
	@Rule public final ActivityTestRule<BackupActivity> activity = new InventoryActivityRule<>(BackupActivity.class);
	@Rule public final TemporaryFolder temp = new TemporaryFolder();
	@Rule public final IdlingResourceRule backupService = new BackupServiceInBackupActivityIdlingRule(activity);
	@Rule public final TestName name = new TestName();

	@Before public void cleanStartExportExternal() {
		assertEmptyState();
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText(R.string.backup_export_external)).perform(click());
	}

	@After public void cleanState() {
		assertEmptyState();
	}

	@Test public void testCancelWarning() throws Exception {
		onView(withId(R.id.alertTitle))
				.inRoot(isDialog())
				.check(matches(allOf(isDisplayed(), withText(R.string.backup_export_external))));

		clickNegativeInDialog();
	}

	@Test public void testCancelChooser() throws Exception {
		clickPositiveInDialog();

		assertThat(getChooserTitle(), isString(R.string.backup_export_external));
		pressBackExternal();
	}

	@Test public void testCancelDrive() throws Exception {
		assumeThat(getContext(), hasPackageInstalled(PACKAGE_GOOGLE_DRIVE));

		clickPositiveInDialog();
		clickOnLabel(saveToDrive());
		assertThat(getText(dialogTitle()), is(saveToDrive()));

		pressBackExternal();
	}

	@Test public void testSuccessfulFullExport() throws Exception {
		assumeThat(getContext(), hasPackageInstalled(PACKAGE_GOOGLE_DRIVE));

		clickPositiveInDialog();

		clickOnLabel(saveToDrive());
		{
			assertThat(getActivityStage(activity), is(Stage.PAUSED));
			String fileName = getText(documentTitle());
			String folder = generateFolderName();
			LOG.info("Saving {}/{}", folder, fileName);
			saveToAndroidTests(folder);
			assertThat(getActivityStage(activity), is(Stage.PAUSED));
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
			clickOnLabel(selectFolder());
		}
	}

	private String generateFolderName() {
		return String.format(Locale.ROOT, "%s.%s@%tFT%<tH-%<tM-%<tS",
				getClass().getSimpleName(), name.getMethodName(), Calendar.getInstance());
	}
}
