package net.twisterrob.inventory.android.backup;

import java.util.concurrent.CancellationException;

import android.support.annotation.*;

public interface ProgressDispatcher {
	/**
	 * @throws CancellationException is a good place to notify the exporter to quit immediately
	 */
	@WorkerThread
	void dispatchProgress(@NonNull Progress progress) throws CancellationException;

	ProgressDispatcher IGNORE = new ProgressDispatcher() {
		@Override public void dispatchProgress(@NonNull Progress progress) throws CancellationException {
			// NO OP
		}
	};
}
