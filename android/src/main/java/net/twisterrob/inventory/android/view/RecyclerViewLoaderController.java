package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.*;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView.Adapter;

public abstract class RecyclerViewLoaderController<A extends Adapter<?>, D> extends RecyclerViewController<A, D> {
	private final Context context;
	private final LoaderManagerProvider manager;

	public RecyclerViewLoaderController(@NonNull FragmentActivity activity) {
		this(activity, new ActivityLoaderManagerProvider(activity));
	}
	public RecyclerViewLoaderController(@NonNull Fragment fragment) {
		this(fragment.getActivity(), new FragmentLoaderManagerProvider(fragment));
	}
	private RecyclerViewLoaderController(@NonNull Context context, @NonNull LoaderManagerProvider manager) {
		this.context = context;
		this.manager = manager;
	}

	protected @NonNull Context getContext() {
		return context;
	}
	protected @NonNull LoaderManager getLoaderManager() {
		return manager.get();
	}

	@Override protected void onViewSet() {
		super.onViewSet();
		SwipeRefreshLayout progress = getProgress();
		if (progress != null) {
			progress.setOnRefreshListener(new OnRefreshListener() {
				@Override public void onRefresh() {
					refresh();
				}
			});
		}
	}

	/** getLoaderManager().initLoader(id, args, new LoaderFactory() {...}); */
	public abstract void startLoad(Bundle args);

	/** getLoaderManager().getLoader(id).onContentChanged(); */
	public abstract void refresh();

	protected interface LoaderManagerProvider {
		@NonNull LoaderManager get();
	}

	protected static class ActivityLoaderManagerProvider implements LoaderManagerProvider {
		private final FragmentActivity activity;

		public ActivityLoaderManagerProvider(FragmentActivity activity) {
			this.activity = activity;
		}

		@Override public @NonNull LoaderManager get() {
			return activity.getSupportLoaderManager();
		}
	}

	protected static class FragmentLoaderManagerProvider implements LoaderManagerProvider {
		private final Fragment fragment;

		public FragmentLoaderManagerProvider(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override public @NonNull LoaderManager get() {
			return fragment.getLoaderManager();
		}
	}
}
