package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.slf4j.*;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.utils.*;
import net.twisterrob.inventory.android.utils.DriveHelper.ConnectedTask;
import net.twisterrob.java.io.IOTools;

import static net.twisterrob.inventory.android.Constants.*;
import static net.twisterrob.inventory.android.utils.DriveUtils.*;

public class WelcomeActivity extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(WelcomeActivity.class);

	private static final int REQUEST_CODE_RESOLUTION = 1;
	private DriveHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String folderID = App.getPrefs().getString(Constants.Prefs.DRIVE_FOLDER_ID, null);
		if (folderID != null) {
			showMessage("Google Drive has been set up already");
			finish();
			return;
		}

		helper = new DriveHelper(this, REQUEST_CODE_RESOLUTION);
		helper.executeWhenConnected(new SetupGoogleDrive());
	}

	@Override
	protected void onResume() {
		super.onResume();
		helper.connect();
	}

	@Override
	protected void onPause() {
		helper.disconnect();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (helper.onActivityResult(requestCode, resultCode, data)) {
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void showMessage(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(WelcomeActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	private final class SetupGoogleDrive implements ConnectedTask {
		private GoogleApiClient client;
		public void execute(GoogleApiClient client) {
			this.client = client;
			try {
				setupGoogleDrive();
			} catch (Exception ex) {
				LOG.error("Cannot set up Google Drive", ex);
			} finally {
				client = null;
			}
		}
		private void setupGoogleDrive() throws IOException {
			DriveId rootID = getRootID();
			if (rootID == null) {
				showMessage("Cannot set up Google Drive");
				return;
			}
			Editor prefs = App.getPrefEditor();
			prefs.putString(Constants.Prefs.DRIVE_FOLDER_ID, rootID.encodeToString());
			prefs.commit();
			showMessage("File created: " + rootID);
			finish();
		}

		private DriveId getRootID() throws IOException {
			DriveFolder rootFolder = Drive.DriveApi.getRootFolder(client);

			DriveFolder inventoryRoot = DriveUtils.getExistingFolder(client, rootFolder, DEFAULT_DRIVE_FOLDER_NAME);
			if (inventoryRoot == null) {
				inventoryRoot = createInventoryRoot(rootFolder);
				createREADME(inventoryRoot);
			}

			return inventoryRoot.getDriveId();
		}

		private DriveFile createREADME(DriveFolder rootFolder) throws IOException {
			MetadataChangeSet fileMeta = new MetadataChangeSet.Builder() //
					.setMimeType("text/plain") //
					.setTitle("README.txt") //
					.setDescription("Read this file before you do anything to this collection") //
					.build();
			Contents fileContents = sync(Drive.DriveApi.newContents(client));
			IOTools.copyStream(getAssets().open("Drive_README.txt"), fileContents.getOutputStream());

			DriveFile file = sync(rootFolder.createFile(client, fileMeta, fileContents));
			return file;
		}

		private DriveFolder createInventoryRoot(DriveFolder rootFolder) {
			MetadataChangeSet folderMeta = new MetadataChangeSet.Builder() //
					.setTitle(DEFAULT_DRIVE_FOLDER_NAME) //
					.setDescription("Storage for inventory item images in Magic Home Inventory app") //
					.setViewed(true) //
					.build();
			return sync(rootFolder.createFolder(client, folderMeta));
		}
	}
}
