package net.twisterrob.android.utils.concurrent;

import android.support.annotation.*;

import static net.twisterrob.android.utils.concurrent.SimpleAsyncTaskHelper.*;

public abstract class SimpleSafeAsyncTask<Param, Progress, Result>
		extends SafeAsyncTask<Param, Progress, Result>
		implements Executable<Param> {
	@SafeVarargs
	@Override protected final @Nullable Result doInBackgroundSafe(@Nullable Param... params) throws Exception {
		return doInBackground(getSingleOrThrow("background operation", params, true));
	}

	protected abstract @Nullable Result doInBackground(@Nullable Param param) throws Exception;

	@SafeVarargs
	@Override protected final void onProgressUpdate(@Nullable Progress... values) {
		onProgressUpdate(getSingleOrThrow("progress update", values, true));
	}

	// TODEL UnusedParameters: https://youtrack.jetbrains.com/issue/IDEA-154071
	// TODEL EmptyMethod: https://youtrack.jetbrains.com/issue/IDEA-154073
	@SuppressWarnings("EmptyMethod")
	protected void onProgressUpdate(@SuppressWarnings({"unused", "UnusedParameters"}) @Nullable Progress value) {
		// optional override
	}

	@SafeVarargs
	@Override protected final void onResult(@Nullable Result result, Param... params) {
		onResult(result, getSingleOrThrow("result", params, true));
	}
	protected abstract void onResult(@Nullable Result result, Param param);

	@SafeVarargs
	@Override protected final void onError(@NonNull Exception ex, Param... params) {
		onError(ex, getSingleOrThrow("error", params, true));
	}
	protected abstract void onError(@NonNull Exception ex, Param param);
}
