package net.twisterrob.inventory.android.tasks;

import java.util.concurrent.CountDownLatch;

import org.slf4j.*;

import android.content.Context;
import android.os.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.*;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

/**
 * An AsyncTask that maintains a connected client.
 */
@SuppressWarnings("hiding")
public abstract class ApiClientAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	private static final Logger LOG = LoggerFactory.getLogger(ApiClientAsyncTask.class);

	private GoogleApiClient mClient;

	public ApiClientAsyncTask(Context context) {
		mClient = createClient(context);
	}

	@Override
	protected final Result doInBackground(Params... params) {
		GoogleApiClient client = getConnectedClient(mClient);
		if (client == null) {
			return null;
		}

		try {
			return doInBackgroundConnected(params);
		} finally {
			client.disconnect();
		}
	}

	/**
	 * Override this method to perform a computation on a background thread, while the client is connected.
	 */
	protected abstract Result doInBackgroundConnected(Params... params);

	/**
	 * Gets the GoogleApliClient owned by this async task.
	 */
	protected GoogleApiClient getGoogleApiClient() {
		return mClient;
	}

	public static GoogleApiClient createClient(Context context) {
		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context) //
				.addApi(Drive.API) //
				.addScope(Drive.SCOPE_FILE);
		return builder.build();
	}

	public static GoogleApiClient createConnectedClient(Context context) {
		return getConnectedClient(createClient(context));
	}

	public static GoogleApiClient getConnectedClient(GoogleApiClient client) {
		if (client.isConnected()) {
			return client;
		}
		final CountDownLatch latch = new CountDownLatch(1);
		client.registerConnectionCallbacks(new ConnectionCallbacks() {
			@Override
			public void onConnected(Bundle connectionHint) {
				latch.countDown();
			}
			@Override
			public void onConnectionSuspended(int cause) {
				// ignore
			}
		});
		client.registerConnectionFailedListener(new OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult result) {
				LOG.error("Cannot connect to Google Drive: {}", result.toString());
				latch.countDown();
			}
		});
		client.connect();
		try {
			latch.await();
		} catch (InterruptedException e) {
			return null;
		}
		if (!client.isConnected()) {
			return null;
		}
		return client;
	}
}
