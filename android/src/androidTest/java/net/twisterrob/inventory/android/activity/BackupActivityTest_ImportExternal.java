package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.Collections;
import java.util.zip.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.net.Uri;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.InventoryActivityRule;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.inventory.android.activity.BackupActivityTest.*;

@RunWith(AndroidJUnit4.class)
public class BackupActivityTest_ImportExternal {
	@Rule public final ActivityTestRule<BackupActivity> activity = new InventoryActivityRule<>(BackupActivity.class);
	@Rule public final TemporaryFolder temp = new TemporaryFolder();
	@Rule public final IdlingResourceRule backupService = new BackupServiceInBackupActivityIdlingRule(activity);

	@Before public void assertBackupActivityIsClean() {
		assertEmptyState();
	}

	@Test public void testImportCalled() throws Exception {
		File inventory = temp.newFile();
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(inventory));
		zip.putNextEntry(new ZipEntry(Paths.BACKUP_DATA_FILENAME));
		IOTools.copyStream(activity.getActivity().getAssets().open("demo.xml"), zip);
		Intent mockIntent = new Intent().setData(Uri.fromFile(inventory));
		intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, mockIntent));
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		// FIXME lookup by id with custom matcher: MenuView.ItemView.getItemData().getId()
		onView(withText(R.string.backup_import_external)).perform(click());
		intended(chooser(allOf(
				hasAction(Intent.ACTION_GET_CONTENT),
				hasCategories(Collections.singleton(Intent.CATEGORY_OPENABLE)),
				hasType("*/*")
		)));

		clickNeutralInDialog();
	}
}
