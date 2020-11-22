package net.twisterrob.android.content.loader;

import java.util.*;

import org.slf4j.*;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

public class DynamicLoaderManager implements LoaderCallbacks<Object> {
	private static final Logger LOG = LoggerFactory.getLogger(DynamicLoaderManager.class);

	private final LoaderManager manager;
	private final TreeMap<Integer, Dependency<?>> loaders = new TreeMap<>();

	public DynamicLoaderManager(LoaderManager manager) {
		this.manager = manager;
	}

	public <T> Dependency<T> add(int id, Bundle args, LoaderCallbacks<T> callbacks) {
		Dependency<T> state = new Dependency<>(id, args, callbacks);
		loaders.put(id, state);
		return state;
	}

	public void startLoading() {
		for (Dependency<?> initial : loaders.values()) {
			if (initial.readyToBeExecuted()) {
				start(initial);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Loader<Object> onCreateLoader(int id, Bundle args) {
		return (Loader<Object>)loaders.get(id).callbacks.onCreateLoader(id, args);
	}

	public void onLoaderReset(Loader<Object> loader) {
		Dependency<?> state = loaders.get(loader.getId());
		assert state.loader == loader;
		@SuppressWarnings("unchecked")
		LoaderCallbacks<Object> callbacks = (LoaderCallbacks<Object>)state.callbacks;

		state.ready = false;

		callbacks.onLoaderReset(loader);
	}

	public void onLoadFinished(Loader<Object> loader, Object data) {
		Dependency<?> state = loaders.get(loader.getId());
		assert state.loader == loader;

		if (!state.ready) {
			for (Dependency<?> prev : state.producers) {
				if (!prev.isReady()) {
					return; // wait until it becomes ready, then that will trigger us again
				}
			}

			state.ready = true;
			@SuppressWarnings("unchecked")
			LoaderCallbacks<Object> callbacks = (LoaderCallbacks<Object>)state.callbacks;
			callbacks.onLoadFinished(loader, data);
		}

		LOG.debug("loadFinished #{}, coming up: {}", state.id, state.consumers);
		for (Dependency<?> next : state.consumers) {
			if (next.readyToBeExecuted()) {
				start(next);
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void start(Dependency<?> state) {
		LOG.debug("initLoader #{}", state.id);
		state.loader = (Loader)manager.initLoader(state.id, state.args, this);
	}

	@Override public @NonNull String toString() {
		StringBuilder sb = new StringBuilder(manager.toString());
		sb.append('\n');
		for (Dependency<?> state : loaders.values()) {
			sb.append(state.id);
			sb.append("\n\t").append("->").append(state.consumers);
			sb.append("\n\t").append("<-").append(state.producers);
			sb.append('\n');
		}
		return sb.toString();
	}

	public final class Dependency<T> {
		private final int id;
		private final Bundle args;
		private final LoaderCallbacks<T> callbacks;
		private Loader<T> loader;

		private final Set<Dependency<?>> producers = new HashSet<>();
		private final Set<Dependency<?>> consumers = new HashSet<>();
		private boolean ready = false;

		private Dependency(int id, Bundle args, LoaderCallbacks<T> callbacks) {
			this.id = id;
			this.args = args;
			this.callbacks = callbacks;
		}

		public boolean readyToBeExecuted() {
			for (Dependency<?> dependency : producers) {
				if (!dependency.isReady()) {
					return false;
				}
			}
			return true;
		}

		private boolean isReady() {
			return ready;
		}

		public Dependency<T> needsResultOf(int id) {
			return dependsOn(checkRegistered(id));
		}
		public Dependency<T> providesResultFor(int id) {
			return providesResultFor(checkRegistered(id));
		}
		private Dependency<?> checkRegistered(int id) {
			Dependency<?> state = loaders.get(id);
			if (state == null) {
				throw new IllegalArgumentException("Loader with ID#" + id + " is not registered yet!");
			}
			return state;
		}

		public Dependency<T> dependsOn(Dependency<?> dependency) {
			if (dependency == null || this.owner() != dependency.owner()) {
				throw new IllegalArgumentException("The dependency provided is invalid");
			}
			producers.add(dependency);
			dependency.consumers.add(this);
			return this;
		}

		public Dependency<T> providesResultFor(Dependency<?> dependency) {
			dependency.dependsOn(this);
			return this;
		}

		private DynamicLoaderManager owner() {
			return DynamicLoaderManager.this;
		}

		@Override public @NonNull String toString() {
			return String.format(Locale.ROOT, "%d(%b)", id, isReady());
		}
	}

	public static abstract class Condition implements LoaderCallbacks<Void> {
		private final Context context;

		protected Condition(Context context) {
			this.context = context;
		}

		protected abstract boolean test(int id, Bundle args);

		public final Loader<Void> onCreateLoader(int id, Bundle args) {
			if (test(id, args)) {
				return new NullLoader(context);
			} else {
				return null;
			}
		}

		public final void onLoadFinished(Loader<Void> loader, Void data) {
			// do nothing
		}
		public final void onLoaderReset(Loader<Void> loader) {
			// do nothing
		}

		private static class NullLoader extends Loader<Void> {
			public NullLoader(Context context) {
				super(context);
			}
			@Override protected void onStartLoading() {
				deliverResult(null);
			}
		}
	}
}
