package net.twisterrob.android.content.loader;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public class LoaderChain {
	public static Runnable sequence(InitAction<?>... actions) {
		return createSequence(actions);
	}

	public static Runnable sequence(Runnable atTheEnd, InitAction<?>... actions) {
		if (actions.length == 0) {
			return atTheEnd;
		}
		return createSequence(actions).finishWith(atTheEnd);
	}

	private static SequenceAction<?> createSequence(InitAction<?>... actions) {
		@SuppressWarnings({"rawtypes", "unchecked"})
		SequenceAction<?> first = new SequenceAction(actions[0]);
		SequenceAction<?> chain = first;
		for (int i = 1; i < actions.length; ++i) {
			chain = chain.then(actions[i]);
		}
		return first;
	}

	private static class SequenceAction<T> extends InitAction<T> {
		private Runnable next;

		public SequenceAction(InitAction<T> action) {
			super(action);
		}

		public <N> SequenceAction<N> then(InitAction<N> next) {
			this.next = next;
			return new SequenceAction<N>(next);
		}

		public SequenceAction<T> finishWith(Runnable r) {
			this.next = r;
			return this;
		}

		@Override
		public LoaderCallbacks<T> getCallback() {
			LoaderCallbacks<T> callbacks = super.getCallback();
			if (next != null) {
				callbacks = new ChainCallback<T>(callbacks, next);
			}
			return callbacks;
		}
	}

	public static final class ChainCallback<T> implements LoaderCallbacks<T> {
		private LoaderCallbacks<T> wrapped;
		private Runnable next;
		private ChainCallback(LoaderCallbacks<T> current, Runnable next) {
			this.wrapped = current;
			this.next = next;
		}
		public Loader<T> onCreateLoader(int id, Bundle args) {
			return wrapped.onCreateLoader(id, args);
		}
		public void onLoadFinished(Loader<T> loader, T data) {
			wrapped.onLoadFinished(loader, data);
			next.run();
		}
		public void onLoaderReset(Loader<T> loader) {
			wrapped.onLoaderReset(loader);
		}
	}
}