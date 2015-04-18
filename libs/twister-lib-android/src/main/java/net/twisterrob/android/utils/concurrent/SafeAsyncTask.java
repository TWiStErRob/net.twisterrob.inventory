package net.twisterrob.android.utils.concurrent;

import android.os.AsyncTask;
import android.support.annotation.*;

import net.twisterrob.android.utils.tools.AndroidTools;

public abstract class SafeAsyncTask<Param, Progress, Result>
		extends AsyncTask<Param, Progress, AsyncTaskResult<Param, Result>>
		implements Executable<Param> {
	@SafeVarargs
	@Override protected final @NonNull AsyncTaskResult<Param, Result> doInBackground(@Nullable Param... params) {
		try {
			return new AsyncTaskResult<>(doInBackgroundSafe(params), params);
		} catch (Exception ex) {
			return new AsyncTaskResult<>(ex, params);
		}
	}

	@SuppressWarnings("unchecked")
	protected abstract @Nullable Result doInBackgroundSafe(@Nullable Param... params) throws Exception;

	@Override protected final void onPostExecute(@NonNull AsyncTaskResult<Param, Result> result) {
		Exception error = result.getError();
		if (error != null) {
			onError(error, result.getParams());
		} else {
			onResult(result.getResult(), result.getParams());
		}
	}

	@SuppressWarnings("unchecked")
	protected abstract void onResult(@Nullable Result result, Param... params);
	@SuppressWarnings("unchecked")
	protected abstract void onError(@NonNull Exception ex, Param... params);

	@SafeVarargs
	public final void executeParallel(Param... params) {
		AndroidTools.executeParallel(this, params);
	}
	@SafeVarargs
	public final void executeSerial(Param... params) {
		AndroidTools.executeSerial(this, params);
	}
}
