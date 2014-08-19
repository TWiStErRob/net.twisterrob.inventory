package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import static com.google.android.gms.drive.CreateFileActivityBuilder.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Prefs;
import net.twisterrob.inventory.android.content.io.csv.DatabaseCSVExporter;
import net.twisterrob.inventory.android.utils.DriveHelper.ConnectedTask;

import static net.twisterrob.inventory.android.utils.DriveUtils.*;

public class ExportActivity extends BaseDriveActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ExportActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		googleDrive.addTaskAfterConnected(new ConnectedTask() {
			public void execute(GoogleApiClient client) throws Exception {
				String fileName = String.format(Locale.ROOT, Constants.EXPORT_FILE_NAME_FORMAT, Calendar.getInstance());
				MetadataChangeSet metadata = new MetadataChangeSet.Builder() //
						.setMimeType("text/csv") //
						.setTitle(fileName) //
						.build();
				Contents contents = sync(Drive.DriveApi.newContents(client));
				new DatabaseCSVExporter().export(contents.getOutputStream());
				IntentSender intentSender = Drive.DriveApi.newCreateFileActivityBuilder() //
						.setActivityStartFolder(getRoot(client)) //
						.setInitialMetadata(metadata) //
						.setInitialContents(contents) //
						.build(client);
				try {
					startIntentSenderForResult(intentSender, RESULT_FIRST_USER, null, 0, 0, 0);
				} catch (SendIntentException ex) {
					LOG.warn("Unable to send intent", ex);
				}
			}

			private DriveId getRoot(GoogleApiClient client) {
				String folderDriveId = App.getPrefs().getString(Prefs.DRIVE_FOLDER_ID, null);
				if (folderDriveId != null) {
					return DriveId.decodeFromString(folderDriveId);
				} else {
					return Drive.DriveApi.getRootFolder(client).getDriveId();
				}
			}
		});
	}
	@Override
	protected void onStart() {
		super.onStart();
		LOG.trace("onStart: connecting");
		googleDrive.startConnect();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case RESULT_FIRST_USER:
				if (resultCode == Activity.RESULT_OK) {
					DriveId driveId = (DriveId)data.getParcelableExtra(EXTRA_RESPONSE_DRIVE_ID);
					App.getPrefEditor().putString(Prefs.LAST_EXPORT_DRIVE_ID, driveId.encodeToString()).apply();
				}
				finish();
				return;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}
	public static Intent chooser() {
		Intent intent = new Intent(App.getAppContext(), ExportActivity.class);
		return intent;
	}
}
