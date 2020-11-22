package net.twisterrob.android.utils.concurrent;

import androidx.annotation.*;

import static net.twisterrob.android.utils.concurrent.SimpleAsyncTaskHelper.*;

/**
 * Combination of {@link SimpleAsyncTask} and {@link SafeAsyncTask}: single parameter input and ability to throw
 * exceptions from background work and be notified in different UI callbacks.
 * @see net.twisterrob.android.utils.tools.AndroidTools#executeParallel
 * @see net.twisterrob.android.utils.tools.AndroidTools#executeSerial
 */
public abstract class SimpleSafeAsyncTask<Param, Progress, Result>
		extends SafeAsyncTask<Param, Progress, Result> {
	@WorkerThread
	@SafeVarargs
	@Override protected final @Nullable Result doInBackgroundSafe(@Nullable Param... params) throws Exception {
		return doInBackground(getSingleOrThrow("background operation", params, true));
	}

	@WorkerThread
	protected abstract @Nullable Result doInBackground(@Nullable Param param) throws Exception;

	@UiThread
	@SafeVarargs
	@Override protected final void onProgressUpdate(@Nullable Progress... values) {
		onProgressUpdate(getSingleOrThrow("progress update", values, true));
	}

	@UiThread
	protected void onProgressUpdate(@SuppressWarnings("unused") @Nullable Progress value) {
		// optional override
	}

	@UiThread
	@SafeVarargs
	@Override protected final void onResult(@Nullable Result result, Param... params) {
		onResult(result, getSingleOrThrow("result", params, true));
	}
	@UiThread
	protected abstract void onResult(@Nullable Result result, Param param);

	@UiThread
	@SafeVarargs
	@Override protected final void onError(@NonNull Exception ex, Param... params) {
		onError(ex, getSingleOrThrow("error", params, true));
	}
	@UiThread
	protected abstract void onError(@NonNull Exception ex, Param param);
}
