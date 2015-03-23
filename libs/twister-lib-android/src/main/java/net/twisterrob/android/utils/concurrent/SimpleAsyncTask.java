package net.twisterrob.android.utils.concurrent;

import android.os.AsyncTask;
import android.support.annotation.*;

import net.twisterrob.android.utils.tools.AndroidTools;

public abstract class SimpleAsyncTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {
	@SafeVarargs
	@Override protected final @Nullable Result doInBackground(@Nullable Param... params) {
		return doInBackground(getSingleOrThrow("background operation", params, true));
	}

	protected abstract @Nullable Result doInBackground(@Nullable Param param);

	@SafeVarargs
	@Override protected final void onProgressUpdate(@Nullable Progress... values) {
		onProgressUpdate(getSingleOrThrow("progress update", values, true));
	}

	protected void onProgressUpdate(@Nullable Progress value) {
		// optionally overrideable
	}

	private static @Nullable <T> T getSingleOrThrow(@NonNull String type, @Nullable T[] params, boolean allowZeroLength) {
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

	private static RuntimeException invalidArg(@NonNull String message) {
		return new IllegalArgumentException(SimpleAsyncTask.class.getSimpleName() + " is for simple tasks, " + message);
	}

	@SafeVarargs
	public final void executeParallel(Param... params) {
		AndroidTools.executeParallel(this, params);
	}
	@SafeVarargs
	public final void executeSerial(Param... params) {
		AndroidTools.executeSerial(this, params);
	}
}
