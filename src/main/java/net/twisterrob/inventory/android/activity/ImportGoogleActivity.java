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
import net.twisterrob.android.utils.model.DriveHelper.ConnectedTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;

import static net.twisterrob.android.utils.tools.DriveTools.ContentsUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.MetaDataUtils.sync;
import static net.twisterrob.android.utils.tools.DriveTools.StatusUtils.sync;

public class ImportGoogleActivity extends BaseDriveActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ImportGoogleActivity.class);
	private static final int REQUEST_PICK = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		googleDrive.setFinishOnCancel(true);
		googleDrive.addTaskAfterConnected(new ConnectedTask() {
			public void execute(GoogleApiClient client) {
				IntentSender intentSender = Drive.DriveApi
						.newOpenFileActivityBuilder()
						.setActivityStartFolder(getRoot(client))
						.setMimeType(new String[] {"text/csv"})
						.build(client);
				try {
					startIntentSenderForResult(intentSender, REQUEST_PICK, null, 0, 0, 0);
				} catch (SendIntentException ex) {
					LOG.warn("Unable to send intent", ex);
				}
			}

			private DriveId getRoot(GoogleApiClient client) {
				DriveId root = ImportGoogleActivity.this.getRoot();
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
					new SimpleAsyncTask<DriveId, Void, Metadata>() {
						@Override
						protected Metadata doInBackground(DriveId driveId) {
							GoogleApiClient client = googleDrive.getConnectedClient();
							DriveFile file = Drive.DriveApi.getFile(client, driveId);
							Metadata meta = sync(file.getMetadata(client));
							Contents contents = sync(file.openContents(client, DriveFile.MODE_READ_ONLY, null));
							try {
								IOTools.copyStream(contents.getInputStream(), null);
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
		Intent intent = new Intent(App.getAppContext(), ImportGoogleActivity.class);
		return intent;
	}
}
