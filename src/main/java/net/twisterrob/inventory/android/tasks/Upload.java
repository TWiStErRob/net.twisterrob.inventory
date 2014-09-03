package net.twisterrob.inventory.android.tasks;

import java.io.*;

import org.slf4j.*;

import android.app.Activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.utils.drive.*;
import net.twisterrob.inventory.android.utils.drive.DriveUtils.ContentsUtils;

import static net.twisterrob.inventory.android.utils.drive.DriveUtils.ContentsUtils.*;
import static net.twisterrob.inventory.android.utils.drive.DriveUtils.FileUtils.*;

public class Upload extends SimpleAsyncTask<File, Void, DriveFile> {
	private static final Logger LOG = LoggerFactory.getLogger(Upload.class);
	private final ApiClientProvider provider;

	public Upload(Activity activity) {
		if (activity instanceof ApiClientProvider) {
			this.provider = (ApiClientProvider)activity;
		} else {
			String clazz = activity != null? activity.getClass().toString() : null;
			throw new IllegalArgumentException(clazz + " must implement " + ApiClientProvider.class);
		}
	}

	@Override
	public DriveFile doInBackground(File file) {
		try {
			GoogleApiClient client = provider.getConnectedClient();
			if (client == null || !client.isConnected()) {
				LOG.error("Cannot upload file {} because no connected client was given", file);
				return null;
			}
			return uploadFile(client, file);
		} catch (IOException ex) {
			LOG.error("Cannot upload file {}", file, ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(DriveFile result) {
		if (result != null) {
			App.toast("Uploaded: " + result.getDriveId());
		}
	}

	private static DriveFile uploadFile(GoogleApiClient client, File file) throws IOException {
		DriveFolder folder = getDriveFolder(client);
		if (folder == null) {
			LOG.error("No Google Drive folder found, aborting {} upload", file);
			return null;
		}
		LOG.info("Uploading {} to Drive Folder {}", file, folder.getDriveId());
		MetadataChangeSet fileMeta = new MetadataChangeSet.Builder() //
				.setMimeType("image/jpeg") //
				.setTitle(file.getName()) //
				.build();
		Contents fileContents = sync(Drive.DriveApi.newContents(client));
		ContentsUtils.putToFile(fileContents, file);
		return sync(folder.createFile(client, fileMeta, fileContents));
	}

	private static DriveFolder getDriveFolder(GoogleApiClient client) {
		String driveFolderName = App.getPrefs().getString(Constants.Prefs.DRIVE_FOLDER_ID, null);
		if (driveFolderName != null) {
			DriveId folderId = DriveId.decodeFromString(driveFolderName);
			return Drive.DriveApi.getFolder(client, folderId);
		}
		return null;
	}
}