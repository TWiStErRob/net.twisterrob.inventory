package net.twisterrob.android.utils.concurrent;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import net.twisterrob.android.utils.tools.AndroidTools;

import static net.twisterrob.android.utils.concurrent.SimpleAsyncTaskHelper.*;

public abstract class SimpleAsyncTask<Param, Progress, Result>
		extends AsyncTask<Param, Progress, Result>
		implements Executable<Param> {
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
		// optional override
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
