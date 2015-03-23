package net.twisterrob.inventory.android.view;

import android.support.annotation.*;

import net.twisterrob.android.utils.concurrent.*;

public abstract class SafeSimpleAsyncTask<Param, Progress, Result>
		extends SimpleAsyncTask<Param, Progress, AsyncTaskResult<Result>> {
	@Override protected final @NonNull AsyncTaskResult<Result> doInBackground(@Nullable Param param) {
		try {
			return new AsyncTaskResult<>(doInBackgroundSafe(param));
		} catch (Exception ex) {
			return new AsyncTaskResult<>(ex);
		}
	}
	protected abstract @Nullable Result doInBackgroundSafe(@Nullable Param param) throws Exception;

	@Override protected final void onPostExecute(@NonNull AsyncTaskResult<Result> result) {
		Exception error = result.getError();
		if (error != null) {
			onError(error);
		} else {
			onResult(result.getResult());
		}
	}

	protected abstract void onResult(@Nullable Result result);
	protected abstract void onError(@NonNull Exception error);
}
