package net.twisterrob.inventory.android.utils;

import java.util.*;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.IntentSender.SendIntentException;
import android.os.*;

import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

public class DriveHelper implements ConnectionCallbacks, OnConnectionFailedListener {
	private static final Logger LOG = LoggerFactory.getLogger(DriveHelper.class);

	public interface ConnectedTask {
		void execute(GoogleApiClient client);
	}

	private final Activity activity;
	private final int resolutionReqCode;
	private final List<ConnectedTask> delayed = new ArrayList<ConnectedTask>();

	private GoogleApiClient mGoogleApiClient;

	public DriveHelper(Activity activity, int resolutionReqCode) {
		this.activity = activity;
		this.resolutionReqCode = resolutionReqCode;
	}

	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	public void connect() {
		LOG.trace("connect({})", mGoogleApiClient);
		if (mGoogleApiClient == null) {
			LOG.trace("connect(building new)");
			mGoogleApiClient = createClientBuilder().build();
		}
		mGoogleApiClient.connect();
	}

	public void disconnect() {
		LOG.trace("disconnect({})", mGoogleApiClient);
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == resolutionReqCode && resultCode == Activity.RESULT_OK) {
			LOG.trace("onActivityResult: everything ok, connecting");
			mGoogleApiClient.connect();
			return true;
		}
		return false;
	}

	public void executeWhenConnected(ConnectedTask runnable) {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			runnable.execute(getGoogleApiClient());
		} else {
			delayed.add(runnable);
		}
	}

	private Builder createClientBuilder() {
		return new GoogleApiClient.Builder(activity) //
				.addApi(Drive.API) //
				.addScope(Drive.SCOPE_FILE) //
				.addScope(Drive.SCOPE_APPFOLDER) //
				.addConnectionCallbacks(this) //
				.addOnConnectionFailedListener(this) //
		;
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		LOG.info("GoogleApiClient connected");
		new Task(getGoogleApiClient()).execute(delayed.toArray(new ConnectedTask[delayed.size()]));
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		LOG.info("GoogleApiClient connection failed: {}", result);
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), activity, 0).show();
			return;
		}
		try {
			result.startResolutionForResult(activity, resolutionReqCode);
		} catch (SendIntentException e) {
			LOG.error("Exception while starting resolution activity", e);
		}
	}

	public void onConnectionSuspended(int reson) {
		LOG.info("GoogleApiClient disconnected");
	}

	private static class Task extends AsyncTask<ConnectedTask, Void, Void> {
		private final GoogleApiClient client;

		public Task(GoogleApiClient client) {
			this.client = client;
		}

		@Override
		protected Void doInBackground(ConnectedTask... params) {
			for (ConnectedTask run: params) {
				run.execute(client);
			}
			return null;
		}
	}
}
