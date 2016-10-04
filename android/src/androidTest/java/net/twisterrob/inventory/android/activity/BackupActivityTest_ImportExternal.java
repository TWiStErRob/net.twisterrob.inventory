package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import android.net.Uri;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor;
import net.twisterrob.inventory.android.test.actors.BackupActivityActor.ImportExternalActor;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.inventory.android.test.categories.On.Backup;

@RunWith(AndroidJUnit4.class)
@Category({On.Backup.Import.class, Backup.External.class})
public class BackupActivityTest_ImportExternal {
	@Rule public final ActivityTestRule<BackupActivity> activity = new InventoryActivityRule<>(BackupActivity.class);
	@Rule public final TemporaryFolder temp = new TemporaryFolder();
	@Rule public final IdlingResourceRule backupService = new BackupServiceInBackupActivityIdlingRule(activity);
	private final BackupActivityActor backup = new BackupActivityActor();

	@Before public void assertBackupActivityIsClean() {
		backup.assertEmptyState();
	}

	@Category({UseCase.Complex.class})
	@Test public void testImportCalled() throws Exception {
		ImportExternalActor importActor = backup.importExternal();
		Uri uri = importActor.mockImportFromFile(temp.newFile());
		importActor.verifyMockImport(uri);
		backup.assertImportFinished().dismiss();
	}
}
