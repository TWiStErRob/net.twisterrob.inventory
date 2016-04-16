package net.twisterrob.android.content.loader;

import android.content.Context;
import android.support.annotation.*;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Implementation of {@link AsyncTaskLoader} which contains all the resource juggling to behave nicely in the framework.
 * It's based on the application list sample at {@link android.content.AsyncTaskLoader} and {@link android.content.CursorLoader}.
 * @see <a href="https://code.google.com/p/android/issues/detail?id=14944">Honeycomb: initLoader() nor restartLoader() actually starts the loader</a>
 * @param <D> the data type to be loaded
 */
// CONSIDER post to http://stackoverflow.com/questions/10524667/android-asynctaskloader-doesnt-start-loadinbackground
public abstract class AsyncLoader<D> extends AsyncTaskLoader<D> {
	private D mData;

	public AsyncLoader(Context context) {
		super(context);
	}

	@WorkerThread
	@Override public abstract D loadInBackground();

	/**
	 * Called when there is new data to deliver to the client.
	 * The super class will take care of delivering it; the implementation here just adds a little more logic.
	 */
	@MainThread
	@Override public void deliverResult(D data) {
		if (isReset()) {
			// An async query came in while the loader is stopped. We don't need the result.
			releaseResources(data);
			return; // we're reset, no data, no more processing
		}
		D oldData = mData;
		mData = data;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately deliver its results.
			super.deliverResult(data);
		}

		// At this point we can release the resources associated with 'oldData' if needed;
		// now that the new result is delivered we know that it is no longer in use.
		if (oldData != data) {
			releaseResources(oldData);
		}
	}

	/**
	 * Starts an asynchronous load of the data.
	 * When the result is ready the callbacks will be called on the UI thread.
	 * If a previous load has been completed and is still valid the result may be passed to the callbacks immediately.
	 */
	@MainThread
	@Override protected void onStartLoading() {
		if (mData != null) {
			// If we currently have a result available, deliver it immediately.
			deliverResult(mData);
		}

		if (takeContentChanged() || mData == null || needsLoading()) {
			// If the data has changed since the last time it was loaded or is not currently available, start a load.
			forceLoad();
		}
	}

	@MainThread
	@Override protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@MainThread
	@Override public void onCanceled(D data) {
		// At this point we can release the resources associated with data
		releaseResources(data);
	}

	@MainThread
	@Override protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		releaseResources(mData);
		mData = null;
	}

	protected void releaseResources(@Nullable D data) {
		// release any resources associated with the data, e.g. close the Cursor
	}

	protected boolean needsLoading() {
		// start looking for changes and check if anything changed that invalidates the result
		// stop looking for changes in onReset
		return false;
	}
}
