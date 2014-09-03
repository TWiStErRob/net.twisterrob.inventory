package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import static com.google.android.gms.drive.OpenFileActivityBuilder.*;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.io.csv.DatabaseCSVImporter;
import net.twisterrob.inventory.android.utils.drive.DriveHelper.ConnectedTask;

import static net.twisterrob.inventory.android.utils.drive.DriveUtils.ContentsUtils.*;
import static net.twisterrob.inventory.android.utils.drive.DriveUtils.MetaDataUtils.*;
import static net.twisterrob.inventory.android.utils.drive.DriveUtils.StatusUtils.*;

public class ImportActivity extends BaseDriveActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ImportActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		googleDrive.setFinishOnCancel(true);
		googleDrive.addTaskAfterConnected(new ConnectedTask() {
			public void execute(GoogleApiClient client) {
				IntentSender intentSender = Drive.DriveApi.newOpenFileActivityBuilder() //
						.setActivityStartFolder(getRoot(client)) //
						.setMimeType(new String[]{"text/csv"}) //
						.build(client);
				try {
					startIntentSenderForResult(intentSender, RESULT_FIRST_USER, null, 0, 0, 0);
				} catch (SendIntentException ex) {
					LOG.warn("Unable to send intent", ex);
				}
			}

			private DriveId getRoot(GoogleApiClient client) {
				DriveId root = ImportActivity.this.getRoot();
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
			case RESULT_FIRST_USER:
				if (resultCode == Activity.RESULT_OK) {
					new SimpleAsyncTask<DriveId, Void, Metadata>() {
						@Override
						protected Metadata doInBackground(DriveId driveId) {
							GoogleApiClient client = getConnectedClient();
							DriveFile file = Drive.DriveApi.getFile(client, driveId);
							Metadata meta = sync(file.getMetadata(client));
							Contents contents = sync(file.openContents(client, DriveFile.MODE_READ_ONLY, null));
							try {
								new DatabaseCSVImporter().importAll(contents.getInputStream());
								return meta;
							} catch (IOException ex) {
								ex.printStackTrace();
							} finally {
								sync(file.discardContents(client, contents));
							}
							return null;
						}

						@Override
						protected void onPostExecute(Metadata result) {
							if (result != null) {
								App.toast("Successfully imported " + result.getTitle());
							} else {
								App.toast("Import from Google Drive failed");
							}
						}
					}.execute((DriveId)data.getParcelableExtra(EXTRA_RESPONSE_DRIVE_ID));
				}
				finish();
				return;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}
	public static Intent chooser() {
		Intent intent = new Intent(App.getAppContext(), ImportActivity.class);
		return intent;
	}
}
