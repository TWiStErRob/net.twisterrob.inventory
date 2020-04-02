package net.twisterrob.android.utils.concurrent;

import android.os.AsyncTask;

public class BackgroundExecution extends AsyncTask<Void, Void, Void> {
	private final Runnable runnable;
	public BackgroundExecution(Runnable runnable) {
		this.runnable = runnable;
	}
	@Override protected Void doInBackground(Void... params) {
		runnable.run();
		return null;
	}
}
