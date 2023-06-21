package net.twisterrob.inventory.android.backup;

import java.util.concurrent.CancellationException;

import androidx.annotation.*;

public interface ProgressDispatcher {
	/**
	 * @throws CancellationException is a good place to notify the exporter to quit immediately
	 */
	@WorkerThread
	void dispatchProgress(@NonNull Progress progress) throws CancellationException;
}
