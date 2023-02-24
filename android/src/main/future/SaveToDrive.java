package net.twisterrob.inventory.android.tasks;

import java.io.*;

import org.slf4j.*;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.DriveTools.ContentsUtils;
import net.twisterrob.inventory.android.*;

import static net.twisterrob.android.utils.tools.DriveTools.ContentsUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.FileUtils.sync;

public class SaveToDrive extends SimpleAsyncTask<File, Void, DriveFile> {
	public interface ApiClientProvider {
		GoogleApiClient getConnectedClient();
	}

	private static final Logger LOG = LoggerFactory.getLogger(SaveToDrive.class);
	private final ApiClientProvider provider;

	public SaveToDrive(ApiClientProvider provider) {
		this.provider = provider;
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