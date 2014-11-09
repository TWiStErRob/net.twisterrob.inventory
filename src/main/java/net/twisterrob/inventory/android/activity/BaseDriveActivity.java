package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.slf4j.*;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.google.android.gms.common.api.*;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.DriveFolder.*;

import net.twisterrob.android.utils.model.DriveHelper;
import net.twisterrob.android.utils.model.DriveHelper.ConnectedTask;
import net.twisterrob.android.utils.tools.DriveTools.FolderUtils;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.utils.ApiClientProvider;
import net.twisterrob.java.io.IOTools;

import static net.twisterrob.android.utils.tools.DriveTools.ContentsUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.FileUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.FolderUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.MetaDataUtils.sync;
import static net.twisterrob.inventory.android.Constants.*;

public class BaseDriveActivity extends BaseActivity implements ApiClientProvider {
	private static final Logger LOG = LoggerFactory.getLogger(BaseDriveActivity.class);

	private static final int REQUEST_CODE_RESOLUTION = 0xD87; // DRiVe

	protected DriveHelper googleDrive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		googleDrive = new DriveHelper(this, REQUEST_CODE_RESOLUTION);
		googleDrive.addTaskAfterConnected(new SetupGoogleDrive());
	}

	@Override
	protected void onResume() {
		App.setApiClientProvider(this);
		googleDrive.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		App.setApiClientProvider(null);
		googleDrive.onPause();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (googleDrive.onActivityResult(requestCode, resultCode, data)) {
			return;
		}
		App.setApiClientProvider(this); // we are before onResume, give a peek for fragments who may need it
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected DriveId getRoot() {
		String folderDriveId = App.getPrefs().getString(Prefs.DRIVE_FOLDER_ID, null);
		if (folderDriveId != null) {
			return DriveId.decodeFromString(folderDriveId);
		} else {
			return null;
		}
	}

	public GoogleApiClient getConnectedClient() {
		return googleDrive.getConnectedClient();
	}

	public void showMessage(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				App.toast(message);
			}
		});
	}

	private final class SetupGoogleDrive implements ConnectedTask {
		private GoogleApiClient client;

		public synchronized void execute(GoogleApiClient client) throws Exception {
			this.client = client;

			DriveId root = getRoot();
			if (root != null) {
				LOG.debug("Google Drive already set up with root '{}' -> '{}'",
						root.encodeToString(), root.getResourceId());
				return;
			}

			try {
				setupGoogleDrive();
			} finally {
				this.client = null;
			}
		}

		private void setupGoogleDrive() throws IOException {
			DriveFolder inventoryFolder = getInventoryFolder();

			Editor prefs = App.getPrefEditor();
			prefs.putString(Prefs.DRIVE_FOLDER_ID, inventoryFolder.getDriveId().encodeToString());
			if (!prefs.commit()) {
				throw new IOException("Cannot write preferences");
			}

			Metadata metadata = sync(inventoryFolder.getMetadata(client));
			showMessage("Successfully connected to Drive folder: " + metadata.getTitle()
					+ " (" + metadata.getDriveId().encodeToString()
					+ ", " + metadata.getDriveId().getResourceId() + ")");
		}
		private DriveFolder getInventoryFolder() throws IOException {
			DriveFolder rootFolder = Drive.DriveApi.getRootFolder(client);

			DriveFolder inventoryFolder = FolderUtils.getExisting(client, rootFolder, DEFAULT_DRIVE_FOLDER_NAME);
			if (inventoryFolder == null) {
				inventoryFolder = sync(createInventoryFolder(rootFolder));
				sync(createREADME(inventoryFolder));
			}

			return inventoryFolder;
		}

		private PendingResult<DriveFolderResult> createInventoryFolder(DriveFolder rootFolder) {
			MetadataChangeSet folderMeta = new MetadataChangeSet.Builder() //
					.setTitle(DEFAULT_DRIVE_FOLDER_NAME) //
					.setDescription("Storage for inventory item images in Magic Home Inventory app") //
					.setViewed(true) //
					.build();

			return rootFolder.createFolder(client, folderMeta);
		}

		private PendingResult<DriveFileResult> createREADME(DriveFolder inventoryFolder) throws IOException {
			MetadataChangeSet fileMeta = new MetadataChangeSet.Builder() //
					.setMimeType("text/plain") //
					.setTitle("README.txt") //
					.setDescription("Read this file before you do anything to this collection") //
					.build();

			Contents fileContents = sync(Drive.DriveApi.newContents(client));
			IOTools.copyStream(getAssets().open("Drive_README.txt"), fileContents.getOutputStream());

			return inventoryFolder.createFile(client, fileMeta, fileContents);
		}
	}
}
