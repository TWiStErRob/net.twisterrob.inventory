package net.twisterrob.inventory.android.tasks;

import java.io.*;

import org.slf4j.*;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.drive.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.utils.DriveUtils;

import static net.twisterrob.inventory.android.utils.DriveUtils.*;

public class Upload extends ApiClientAsyncTask<File, Void, DriveFile> {
	private static final Logger LOG = LoggerFactory.getLogger(Upload.class);

	public Upload(Context context) {
		super(context);
	}

	@Override
	protected DriveFile doInBackgroundConnected(File... params) {
		File file = params[0];
		try {
			return uploadFile(file);
		} catch (IOException ex) {
			LOG.error("Cannot upload file {}", file, ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(DriveFile result) {
		if (result != null) {
			Toast.makeText(App.getAppContext(), "Uploaded: " + result.getDriveId(), Toast.LENGTH_LONG).show();
		}
	}

	private DriveFile uploadFile(File file) throws IOException {
		DriveFolder folder = getDriveFolder();
		if (folder == null) {
			LOG.error("No Google Drive folder found, aborting {} upload", file);
			return null;
		}
		LOG.info("Uploading {} to Drive Folder {}", file, folder.getDriveId());
		MetadataChangeSet fileMeta = new MetadataChangeSet.Builder() //
				.setMimeType("image/jpeg") //
				.setTitle(file.getName()) //
				.build();
		Contents fileContents = sync(Drive.DriveApi.newContents(getGoogleApiClient()));
		DriveUtils.putFileIntoContents(fileContents, file);
		return sync(folder.createFile(getGoogleApiClient(), fileMeta, fileContents));
	}

	private DriveFolder getDriveFolder() {
		String driveFolderName = App.getPrefs().getString(Constants.Prefs.DRIVE_FOLDER_ID, null);
		if (driveFolderName != null) {
			DriveId folderId = DriveId.decodeFromString(driveFolderName);
			return Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
		}
		return null;
	}
}