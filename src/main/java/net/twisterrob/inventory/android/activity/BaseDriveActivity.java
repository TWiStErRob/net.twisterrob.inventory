package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.slf4j.*;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.utils.*;
import net.twisterrob.inventory.android.utils.DriveHelper.ConnectedTask;
import net.twisterrob.inventory.android.utils.DriveIdDownloader.ApiClientProvider;
import net.twisterrob.java.io.IOTools;

import static net.twisterrob.inventory.android.Constants.*;
import static net.twisterrob.inventory.android.utils.DriveUtils.*;

public class BaseDriveActivity extends BaseActivity implements ApiClientProvider {
	private static final Logger LOG = LoggerFactory.getLogger(BaseDriveActivity.class);

	private static final int REQUEST_CODE_RESOLUTION = 1;
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

			String folderID = App.getPrefs().getString(Constants.Prefs.DRIVE_FOLDER_ID, null);
			if (folderID != null) {
				DriveId driveID = DriveId.decodeFromString(folderID);
				LOG.debug("Google Drive already set up with root '{}' -> '{}'", folderID, driveID.getResourceId());
				return;
			}

			try {
				setupGoogleDrive();
			} finally {
				client = null;
			}
		}

		private void setupGoogleDrive() throws IOException {
			DriveFolder inventoryFolder = getInventoryFolder();

			Editor prefs = App.getPrefEditor();
			prefs.putString(Constants.Prefs.DRIVE_FOLDER_ID, inventoryFolder.getDriveId().encodeToString());
			if (!prefs.commit()) {
				throw new IOException("Cannot write preferences");
			}

			Metadata metadata = sync(inventoryFolder.getMetadata(client));
			showMessage("Successfully connected to Drive folder: " + metadata.getTitle());
		}

		private DriveFolder getInventoryFolder() throws IOException {
			DriveFolder rootFolder = Drive.DriveApi.getRootFolder(client);

			DriveFolder inventoryFolder = DriveUtils.getExistingFolder(client, rootFolder, DEFAULT_DRIVE_FOLDER_NAME);
			if (inventoryFolder == null) {
				inventoryFolder = createInventoryFolder(rootFolder);
				createREADME(inventoryFolder);
			}

			return inventoryFolder;
		}

		private DriveFolder createInventoryFolder(DriveFolder rootFolder) {
			MetadataChangeSet folderMeta = new MetadataChangeSet.Builder() //
					.setTitle(DEFAULT_DRIVE_FOLDER_NAME) //
					.setDescription("Storage for inventory item images in Magic Home Inventory app") //
					.setViewed(true) //
					.build();

			return sync(rootFolder.createFolder(client, folderMeta));
		}

		private DriveFile createREADME(DriveFolder invetoryFolder) throws IOException {
			MetadataChangeSet fileMeta = new MetadataChangeSet.Builder() //
					.setMimeType("text/plain") //
					.setTitle("README.txt") //
					.setDescription("Read this file before you do anything to this collection") //
					.build();

			Contents fileContents = sync(Drive.DriveApi.newContents(client));
			IOTools.copyStream(getAssets().open("Drive_README.txt"), fileContents.getOutputStream());

			return sync(invetoryFolder.createFile(client, fileMeta, fileContents));
		}
	}
}
