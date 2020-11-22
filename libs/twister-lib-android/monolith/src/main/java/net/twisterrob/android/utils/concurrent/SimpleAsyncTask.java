package net.twisterrob.android.utils.concurrent;

import android.os.AsyncTask;

import androidx.annotation.*;

import static net.twisterrob.android.utils.concurrent.SimpleAsyncTaskHelper.*;

/**
 * Simple {@link AsyncTask} implementation that converts the varargs interface to single params.
 * @see net.twisterrob.android.utils.tools.AndroidTools#executeParallel
 * @see net.twisterrob.android.utils.tools.AndroidTools#executeSerial
 */
// TODO create a non-null version
public abstract class SimpleAsyncTask<Param, Progress, Result>
		extends AsyncTask<Param, Progress, Result> {
	@WorkerThread
	@SafeVarargs
	@Override protected final @Nullable Result doInBackground(@Nullable Param... params) {
		return doInBackground(getSingleOrThrow("background operation", params, true));
	}

	@WorkerThread
	protected abstract @Nullable Result doInBackground(@Nullable Param param);

	@UiThread
	@SafeVarargs
	@Override protected final void onProgressUpdate(@Nullable Progress... values) {
		onProgressUpdate(getSingleOrThrow("progress update", values, true));
	}

	@UiThread
	protected void onProgressUpdate(@Nullable Progress value) {
		// optional override
	}
}
