package net.twisterrob.inventory.android.activity.dev;

import java.io.*;

import org.slf4j.*;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEvent.Listener;

import net.twisterrob.android.utils.model.DriveHelper.ConnectedTask;
import net.twisterrob.android.utils.tools.DriveTools.FolderUtils;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BaseDriveActivity;
import net.twisterrob.inventory.android.tasks.SaveToDrive;
import net.twisterrob.inventory.android.tasks.SaveToDrive.ApiClientProvider;

import static net.twisterrob.android.utils.tools.DriveTools.IdUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.MetaBufferUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.MetaDataUtils.sync;

public class DeveloperDriveActivity extends BaseDriveActivity {
	private static final Logger LOG = LoggerFactory.getLogger(DeveloperDriveActivity.class);
	private TextView log;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LOG.trace("onCreate: {}", Thread.currentThread());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dev_drive);

		log = (TextView)findViewById(R.id.details);

		googleDrive.addTaskAfterConnected(new ConnectedTask() {
			public void execute(GoogleApiClient client) {
				LOG.trace("execute: {}", Thread.currentThread());

				DriveFolder rootFolder = Drive.DriveApi.getRootFolder(client);
				FolderUtils.dump(client, rootFolder);

				DriveFile driveFile = existingFile(client);
				log("DriveFile: " + driveFile.getDriveId() + " res: " + driveFile.getDriveId().getResourceId());

				driveFile.addChangeListener(client, new Listener<ChangeEvent>() {
					@Override
					public void onEvent(ChangeEvent event) {
						LOG.trace("onEvent: {}", Thread.currentThread());
						log("Event: " + event.getDriveId() + " res: " + event.getDriveId().getResourceId());
					}
				});

				Metadata metadata = sync(driveFile.getMetadata(client));
				log("Metadata: " + metadata.getDriveId() + " res: " + metadata.getDriveId().getResourceId());

				MetadataBuffer parents = sync(driveFile.listParents(client));
				log(parents.getCount());
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		log.append("Connecting...");
		googleDrive.startConnect();
	}

	DriveFile newFile() throws IOException {
		File file = createFile();
		log("File: " + file);

		return new SaveToDrive(new ApiClientProvider() {
			@Override public GoogleApiClient getConnectedClient() {
				return googleDrive.getConnectedClient();
			}
		}).doInBackground(file);
	}

	DriveFile existingFile(GoogleApiClient client) {
		// Metadata: DriveId:CAESABi2CCDmhsDtoVE= res: null
		// Event: DriveId:CAESHDBCNXFUTlBncV9sbDVUbEJzWjNORGRsVmtTRTAYtggg5obA7aFR res: 0B5qTNPgq_ll5TlBsZ3NDdlVkSE0
		//return Drive.DriveApi.getFile(client, DriveId.decodeFromString("DriveId:CAESABi2CCDmhsDtoVE="));
		//				Drive.DriveApi.getFile(client,
		//						DriveId.decodeFromString("DriveId:CAESHDBCNXFUTlBncV9sbDVUbEJzWjNORGRsVmtTRTAYtggg5obA7aFR"));
		DriveId driveId = sync(Drive.DriveApi.fetchDriveId(client, "0B5qTNPgq_ll5TlBsZ3NDdlVkSE0"));
		return Drive.DriveApi.getFile(client, driveId);
	}

	void log(final Object object) {
		LOG.debug(String.valueOf(object));
		runOnUiThread(new Runnable() {
			public void run() {
				log.append("\n" + object);
			}
		});
	}

	File createFile() throws IOException {
		File file = new File(getExternalCacheDir(), "temp.txt");
		file.deleteOnExit();
		FileWriter writer = new FileWriter(file);
		writer.write("stuff");
		writer.close();
		return file;
	}
}
