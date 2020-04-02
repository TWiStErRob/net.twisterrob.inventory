package net.twisterrob.android.utils.concurrent;

import android.support.annotation.*;

/**
 * An {@link android.os.AsyncTask} that uses a closure or constructor to obtain input and the same for output.
 * @see net.twisterrob.android.utils.tools.AndroidTools#executeParallel
 * @see net.twisterrob.android.utils.tools.AndroidTools#executeSerial
 */
public abstract class ClosureAsyncTask extends SafeAsyncTask<Void, Void, Void> {
	@Override protected final @Nullable Void doInBackgroundSafe(@Nullable Void... voids) throws Exception {
		doInBackgroundSafe();
		return null;
	}

	/**
	 * @throws Exception see {@link #doInBackgroundSafe(Void...)}
	 */
	@WorkerThread
	protected abstract void doInBackgroundSafe() throws Exception;

	@Override protected final void onResult(@Nullable Void aVoid, Void... voids) {
		onResult();
	}
	@UiThread
	protected abstract void onResult();

	@Override protected final void onError(@NonNull Exception ex, Void... voids) {
		onError(ex);
	}
	@UiThread
	protected abstract void onError(@NonNull Exception ex);
}
