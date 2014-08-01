package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.drive.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.tasks.ApiClientAsyncTask;

public class PickDriveFileActivity extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(PickDriveFileActivity.class);
	private EditText text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		text = new EditText(this);
		setContentView(text);

		new ApiClientAsyncTask<Void, Void, Void>(this) {
			@Override
			protected Void doInBackgroundConnected(Void... params) {
				IntentSender intentSender = Drive.DriveApi.newOpenFileActivityBuilder().build(getGoogleApiClient());
				try {
					startIntentSenderForResult(intentSender, RESULT_FIRST_USER, null, 0, 0, 0);
				} catch (SendIntentException ex) {
					LOG.warn("Unable to send intent", ex);
				}
				return null;
			}
		}.execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case RESULT_FIRST_USER:
				if (resultCode == Activity.RESULT_OK) {
					DriveId driveId = (DriveId)data.getParcelableExtra("response_drive_id");
					LOG.info("Result: {} / {}", data.getData(), AndroidTools.toString(data.getExtras()));
					text.setText(driveId.encodeToString());
				}
				return;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
