package net.twisterrob.inventory.android.activity;

import java.io.OutputStream;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.webkit.MimeTypeMap;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import static com.google.android.gms.drive.CreateFileActivityBuilder.*;

import net.twisterrob.android.utils.model.DriveHelper.ConnectedTask;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Prefs;

import static net.twisterrob.android.utils.tools.DriveTools.ContentsUtils.*;

public class ExportGoogleActivity extends BaseDriveActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ExportGoogleActivity.class);
	private static final int REQUEST_PICK = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		googleDrive.setFinishOnCancel(true);
		googleDrive.addTaskAfterConnected(new ConnectedTask() {
			public void execute(GoogleApiClient client) throws Exception {
				String fileName = Constants.Paths.getExportFileName();
				MetadataChangeSet metadata = new MetadataChangeSet.Builder()
						.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension("csv"))
						.setTitle(fileName)
						.build();
				Contents contents = sync(Drive.DriveApi.newContents(client));
				OutputStream stream = contents.getOutputStream();
				IntentSender intentSender = Drive.DriveApi
						.newCreateFileActivityBuilder()
						.setActivityStartFolder(getRoot(client))
						.setInitialMetadata(metadata)
						.setInitialContents(contents)
						.build(client);
				try {
					startIntentSenderForResult(intentSender, REQUEST_PICK, null, 0, 0, 0);
				} catch (SendIntentException ex) {
					LOG.warn("Unable to send intent", ex);
				}
			}

			private DriveId getRoot(GoogleApiClient client) {
				DriveId root = ExportGoogleActivity.this.getRoot();
				return root != null? root : Drive.DriveApi.getRootFolder(client).getDriveId();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		googleDrive.startConnect();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_PICK:
				if (resultCode == Activity.RESULT_OK) {
					DriveId driveId = data.getParcelableExtra(EXTRA_RESPONSE_DRIVE_ID);
					App.getPrefEditor().putString(Prefs.LAST_EXPORT_DRIVE_ID, driveId.encodeToString()).apply();
					App.toast("Successfully exported to " + driveId.encodeToString());
				}
				finish();
				return;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public static Intent chooser() {
		Intent intent = new Intent(App.getAppContext(), ExportGoogleActivity.class);
		return intent;
	}
}
