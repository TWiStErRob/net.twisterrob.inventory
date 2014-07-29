package net.twisterrob.android.utils.concurrent;

import android.os.AsyncTask;

public abstract class SimpleAsyncTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {
	@Override
	protected final Result doInBackground(Param... params) {
		return doInBackground(getSingleOrThrow("background operation", params, true));
	}

	protected abstract Result doInBackground(Param param);

	@Override
	protected final void onProgressUpdate(Progress... values) {
		onProgressUpdate(getSingleOrThrow("progress update", values, true));
	}

	protected void onProgressUpdate(Progress value) {
		// optionally overrideable
	}

	private static <T> T getSingleOrThrow(String type, T[] params, boolean allowZeroLength) {
		T param;
		if (params != null) {
			if (params.length == 1) {
				param = params[0];
			} else if (allowZeroLength && params.length == 0) {
				param = null;
			} else {
				throw invalidArg(type + " had " + params.length + " parameters");
			}
		} else {
			param = null;
		}
		return param;
	}

	private static RuntimeException invalidArg(String message) {
		return new IllegalArgumentException(SimpleAsyncTask.class.getSimpleName() + " is for simple tasks, " + message);
	}
}
