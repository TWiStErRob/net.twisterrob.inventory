package net.twisterrob.android.content.loader;

import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

public class InitAction<T> implements Runnable {
	private final LoaderManager manager;
	private final int id;
	private final Bundle args;
	private final LoaderCallbacks<T> callback;

	public InitAction(LoaderManager manager, int id, Bundle args, LoaderCallbacks<T> callback) {
		this.manager = manager;
		this.id = id;
		this.args = args;
		this.callback = callback;
	}

	protected InitAction(InitAction<T> action) {
		this(action.manager, action.id, action.args, action.callback);
	}

	public void run() {
		manager.initLoader(getId(), getArgs(), getCallback());
	}

	public int getId() {
		return id;
	}

	public Bundle getArgs() {
		return args;
	}

	public LoaderCallbacks<T> getCallback() {
		return callback;
	}
}