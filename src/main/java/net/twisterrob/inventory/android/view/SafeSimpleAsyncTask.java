package net.twisterrob.inventory.android.view;

import net.twisterrob.android.utils.concurrent.*;

public abstract class SafeSimpleAsyncTask<Param, Progress, Result>
		extends SimpleAsyncTask<Param, Progress, AsyncTaskResult<Result>> {
	@Override protected final AsyncTaskResult<Result> doInBackground(Param param) {
		try {
			return new AsyncTaskResult<>(doInBackgroundSafe(param));
		} catch (Exception ex) {
			return new AsyncTaskResult<>(ex);
		}
	}
	protected abstract Result doInBackgroundSafe(Param param) throws Exception;

	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	@Override protected final void onPostExecute(AsyncTaskResult<Result> result) {
		if (result.getError() != null) {
			onError(result.getError());
		} else {
			onResult(result.getResult());
		}
	}

	protected abstract void onResult(Result result);
	protected abstract void onError(Exception error);
}
