package net.twisterrob.inventory.android.utils;

import java.util.concurrent.CountDownLatch;

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
public abstract class ApiClientAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	private GoogleApiClient mClient;

	public ApiClientAsyncTask(Context context) {
		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context) //
				.addApi(Drive.API) //
				.addScope(Drive.SCOPE_FILE);
		mClient = builder.build();
	}

	@Override
	protected final Result doInBackground(Params... params) {
		final CountDownLatch latch = new CountDownLatch(1);
		mClient.registerConnectionCallbacks(new ConnectionCallbacks() {
			@Override
			public void onConnected(Bundle arg0) {
				latch.countDown();
			}
			@Override
			public void onConnectionSuspended(int cause) {}
		});
		mClient.registerConnectionFailedListener(new OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult arg0) {
				latch.countDown();
			}
		});
		mClient.connect();
		try {
			latch.await();
		} catch (InterruptedException e) {
			return null;
		}
		if (!mClient.isConnected()) {
			return null;
		}
		try {
			return doInBackgroundConnected(params);
		} finally {
			mClient.disconnect();
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
}
