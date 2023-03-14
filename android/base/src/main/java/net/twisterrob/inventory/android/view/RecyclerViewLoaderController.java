package net.twisterrob.inventory.android.view;

import java.io.Closeable;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

public abstract class RecyclerViewLoaderController<A extends RecyclerView.Adapter<?>, D>
		extends RecyclerViewController<A, D> implements Closeable {
	private final Context context;
	private final LoaderManagerProvider manager;

	public RecyclerViewLoaderController(@NonNull FragmentActivity activity) {
		this(activity, new ActivityLoaderManagerProvider(activity));
	}
	public RecyclerViewLoaderController(@NonNull Fragment fragment) {
		this(fragment.requireActivity(), new FragmentLoaderManagerProvider(fragment));
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
	public abstract void startLoad(@Nullable Bundle args);

	/** getLoaderManager().getLoader(id).onContentChanged(); */
	public abstract void refresh();

	/** getLoaderManager().destroyLoader(id); */
	public abstract void close();

	protected interface LoaderManagerProvider {
		@NonNull LoaderManager get();
	}

	protected static class ActivityLoaderManagerProvider implements LoaderManagerProvider {
		private final FragmentActivity activity;

		public ActivityLoaderManagerProvider(FragmentActivity activity) {
			this.activity = activity;
		}

		@Override public @NonNull LoaderManager get() {
			return LoaderManager.getInstance(activity);
		}
	}

	protected static class FragmentLoaderManagerProvider implements LoaderManagerProvider {
		private final Fragment fragment;

		public FragmentLoaderManagerProvider(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override public @NonNull LoaderManager get() {
			return LoaderManager.getInstance(fragment);
		}
	}
}
