package net.twisterrob.inventory.android.activity;

import java.io.*;

import org.slf4j.*;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.events.*;
import com.google.android.gms.drive.events.DriveEvent.Listener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.tasks.Upload;
import net.twisterrob.inventory.android.utils.DriveHelper.ConnectedTask;
import net.twisterrob.inventory.android.utils.*;

import static net.twisterrob.inventory.android.utils.DriveUtils.*;

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
			public void execute(GoogleApiClient client) throws IOException {
				LOG.trace("execute: {}", Thread.currentThread());

				DriveFolder rootFolder = Drive.DriveApi.getRootFolder(client);
				DriveUtils.dump(client, rootFolder);

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

			private DriveFile newFile() throws IOException {
				File file = createFile();
				log("File: " + file);

				DriveFile driveFile = new Upload(DeveloperDriveActivity.this).doInBackground(file);
				return driveFile;
			}

			private DriveFile existingFile(GoogleApiClient client) {
				// Metadata: DriveId:CAESABi2CCDmhsDtoVE= res: null
				// Event: DriveId:CAESHDBCNXFUTlBncV9sbDVUbEJzWjNORGRsVmtTRTAYtggg5obA7aFR res: 0B5qTNPgq_ll5TlBsZ3NDdlVkSE0
				//return Drive.DriveApi.getFile(client, DriveId.decodeFromString("DriveId:CAESABi2CCDmhsDtoVE="));
				//				Drive.DriveApi.getFile(client,
				//						DriveId.decodeFromString("DriveId:CAESHDBCNXFUTlBncV9sbDVUbEJzWjNORGRsVmtTRTAYtggg5obA7aFR"));
				return Drive.DriveApi.getFile(client,
						sync(Drive.DriveApi.fetchDriveId(client, "0B5qTNPgq_ll5TlBsZ3NDdlVkSE0")));
			}

			private void log(final Object object) {
				LOG.debug(String.valueOf(object));
				runOnUiThread(new Runnable() {
					public void run() {
						log.append("\n" + object);
					}
				});
			}

			private File createFile() throws IOException {
				File file = new File(getExternalCacheDir(), "temp.txt");
				file.deleteOnExit();
				FileWriter writer = new FileWriter(file);
				writer.write("stuff");
				writer.close();
				return file;
			}
		});
	}
	@Override
	protected void onStart() {
		super.onStart();
		log.append("Connecting...");
		googleDrive.startConnect();
	}
}
