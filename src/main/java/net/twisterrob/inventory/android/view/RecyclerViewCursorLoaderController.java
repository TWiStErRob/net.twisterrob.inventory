package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.*;

public abstract class RecyclerViewCursorLoaderController extends RecyclerViewCursorController {
	private final Context context;
	private final LoaderManagerProvider manager;

	public RecyclerViewCursorLoaderController(FragmentActivity activity) {
		this(activity, new ActivityLoaderManagerProvider(activity));
	}
	public RecyclerViewCursorLoaderController(Fragment fragment) {
		this(fragment.getActivity(), new FragmentLoaderManagerProvider(fragment));
	}
	private RecyclerViewCursorLoaderController(Context context, LoaderManagerProvider manager) {
		this.context = context;
		this.manager = manager;
	}

	protected Context getContext() {
		return context;
	}
	protected LoaderManager getLoaderManager() {
		return manager.get();
	}

	public abstract void startLoad(Bundle args);
	public abstract void refresh();

	protected interface LoaderManagerProvider {
		LoaderManager get();
	}

	protected static class ActivityLoaderManagerProvider implements LoaderManagerProvider {
		private final FragmentActivity activity;

		public ActivityLoaderManagerProvider(FragmentActivity activity) {
			this.activity = activity;
		}

		@Override public LoaderManager get() {
			return activity.getSupportLoaderManager();
		}
	}

	protected static class FragmentLoaderManagerProvider implements LoaderManagerProvider {
		private final Fragment fragment;

		public FragmentLoaderManagerProvider(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override public LoaderManager get() {
			return fragment.getLoaderManager();
		}
	}
}
