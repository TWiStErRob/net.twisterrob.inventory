package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.*;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import androidx.annotation.*;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.*;

import net.twisterrob.android.activity.BackPressAware;
import net.twisterrob.android.app.BaseApp;
import net.twisterrob.android.content.loader.AsyncLoader;
import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.BaseComponent;
import net.twisterrob.inventory.android.Constants.*;
import net.twisterrob.inventory.android.backup.R;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.view.RecyclerViewLoaderController;

public class BackupListFragment extends BaseFragment<BackupListFragment.BackupListCallbacks> implements BackPressAware {
	private static final Logger LOG = LoggerFactory.getLogger(BackupListFragment.class);

	public interface BackupListCallbacks {
		void filePicked(@NonNull File file);
		void newIntoDir(@NonNull File targetDir);
		boolean isAllowNew();
	}

	private static final String EXTRA_PATH = "path";
	private static final String EXTRA_HISTORY = "history";

	private RecyclerViewLoaderController<ImportFilesAdapter, List<File>> controller;
	private final Deque<File> history = new ArrayDeque<>();
	private ResourcePreferences prefs;
	private TextView location;

	public BackupListFragment() {
		setDynamicResource(DYN_EventsClass, BackupListCallbacks.class);
	}

	@Override public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		prefs = ((BaseComponent.Provider)context.getApplicationContext()).getBaseComponent().prefs();
	}

	@Override public @NonNull View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
		return inflater.inflate(R.layout.fragment_backup, container, false);
	}

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		location = view.findViewById(R.id.backup_location);
		location.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				DialogTools
						.prompt(v.getContext(), getDir().getAbsolutePath(),
								new PopupCallbacks<String>() {
									@Override public void finished(String value) {
										if (value != null) {
											filePicked(new File(value), true);
										}
									}
								})
						.setTitle(R.string.backup_go_to_title)
						.setMessage(R.string.backup_go_to_message)
						.show();
			}
		});
		controller = new BackupListController();
		controller.setView((RecyclerView)view.findViewById(R.id.backups));
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			onRestoreInstanceState(savedInstanceState);
		} else {
			filePicked(getDir(), true);
		}
	}
	@Override public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRA_HISTORY, (Serializable)history);
	}
	private void onRestoreInstanceState(Bundle savedInstanceState) {
		@SuppressWarnings("unchecked")
		Collection<File> history = (Collection<File>)savedInstanceState.getSerializable(EXTRA_HISTORY);
		//noinspection ConstantConditions if we're restoring onSaveInstanceState must have filled it
		this.history.addAll(history);
		this.history.push(new File("")); // pretend current directory after restore
		backToLastValidDirectory(); // essentially backing out of it, so the fake dir won't ever be relevant
	}

	@Override public void onResume() {
		super.onResume();
		onRefresh();
	}

	public @NonNull File getDir() {
		String backupPath = prefs.getString(R.string.pref_state_backup_path, Paths.getPhoneHome().toString());
		return new File(backupPath);
	}

	private void onDirLoaded(File root) {
		prefs.setString(R.string.pref_state_backup_path, root.getAbsolutePath());
		location.setText(root.toString());
	}

	@Override public boolean onBackPressed() {
		return backToLastValidDirectory();
	}

	private boolean backToLastValidDirectory() {
		while (!history.isEmpty()) {
			history.pop();
			File dir = history.peek();
			StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
			try {
				if (IOTools.isValidDir(dir)) {
					filePicked(dir, false);
					return true;
				}
			} finally {
				StrictMode.setThreadPolicy(originalPolicy);
			}
		}
		return false;
	}

	public void filePicked(@NonNull final File file, boolean addHistory) {
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			LOG.trace("File picked (dir={}, exists={}): {}", file.isDirectory(), file.exists(), file);
			if (IOTools.isValidFile(file)) {
				eventsListener.filePicked(file);
			} else {
				File dir = file;
				while (!IOTools.isValidDir(dir)) {
					dir = dir.getParentFile();
				}
				if (dir != file) {
					BaseApp.toastUser(String.format(Locale.ROOT, "%s is not a directory, using %s.", file, dir));
				}
				if (addHistory && !dir.equals(history.peek())) {
					history.push(dir);
				}
				controller.startLoad(Intents.bundleFrom(EXTRA_PATH, dir));
			}
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	@Override public void onRefresh() {
		controller.refresh();
	}

	private class ImportFilesAdapter extends RecyclerView.Adapter<ImportFilesAdapter.ViewHolder> {
		private File parent;
		private List<File> files;

		@SuppressLint("NotifyDataSetChanged") // Everything is invalidated when setting new suggestions.
		public void setFiles(File root, List<File> files) {
			this.parent = root != null? root.getParentFile() : null;
			this.files = files;
			notifyDataSetChanged();
		}

		@Override public int getItemCount() {
			return (parent != null? 1 : 0) + (files != null? files.size() : 0);
		}
		private @NonNull File getItem(int position) {
			if (parent != null) {
				return position == 0? parent : files.get(position - 1);
			} else {
				return files.get(position);
			}
		}
		@Override public long getItemId(int position) {
			return getItem(position).hashCode();
		}
		@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			return new ViewHolder(inflater.inflate(R.layout.item_backup, parent, false));
		}

		@Override public void onBindViewHolder(ImportFilesAdapter.ViewHolder holder, int position) {
			File file = getItem(position);

			String name = file.getName();
			if (file == parent) {
				StringBuilder sb = new StringBuilder("..");
				if (TextUtils.getTrimmedLength(name) != 0) {
					sb.append(' ').append('(').append(name).append(')');
				}
				name = sb.toString();
			}
			@RawRes int icon;
			if (name.startsWith("Inventory_") && name.endsWith(".zip")) {
				icon = R.raw.item_disc;
				name = name.replaceAll("^Inventory_(.*)\\.zip$", "$1").replace('_', ' ');
			} else {
				icon = R.raw.category_unknown;
			}

			String size = null;
			StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
			try {
				if (file.isDirectory()) {
					icon = R.raw.item_box;
				} else {
					size = Formatter.formatShortFileSize(holder.count.getContext(), file.length());
				}
			} finally {
				StrictMode.setThreadPolicy(originalPolicy);
			}

			Pic.svg().load(icon).into(holder.icon);
			holder.text.setText(name);
			holder.count.setText(size);
			ViewTools.displayedIfHasText(holder.count);
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			final ImageView icon;
			final TextView text;
			final TextView count;

			public ViewHolder(View view) {
				super(view);
				icon = view.findViewById(R.id.type);
				text = view.findViewById(R.id.title);
				count = view.findViewById(R.id.count);

				view.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						int position = getAdapterPosition();
						if (position != RecyclerView.NO_POSITION) {
							filePicked(getItem(position), true);
						}
					}
				});
			}
		}
	}

	private static class FilesLoader extends AsyncLoader<List<File>> {
		private static final Pattern IMPORT_FILTER = Pattern.compile(".*\\.zip$");
		private final File root;
		public FilesLoader(Context context, File root) {
			super(context);
			this.root = root;
		}
		public File getRoot() {
			return root;
		}
		@Override public List<File> loadInBackground() {
			File[] folders = getFolders(root);
			Arrays.sort(folders);
			File[] files = getImportableFiles(root);
			Arrays.sort(files);

			List<File> result = new ArrayList<>(folders.length + files.length);
			result.addAll(Arrays.asList(folders));
			result.addAll(Arrays.asList(files));
			return result;
		}

		private @NonNull File[] getFolders(File root) {
			File[] folders = root.listFiles(new FileFilter() {
				@Override public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			return folders != null? folders : new File[0];
		}

		private @NonNull File[] getImportableFiles(File root) {
			File[] files = root.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isFile() && file.canRead() && IMPORT_FILTER.matcher(file.getName()).matches();
				}
			});
			return files != null? files : new File[0];
		}
	}

	private class BackupListController extends RecyclerViewLoaderController<ImportFilesAdapter, List<File>> {
		private static final int LOADER_ID = 1;
		public BackupListController() {
			super(BackupListFragment.this);
		}

		@Override public void startLoad(@Nullable Bundle args) {
			Loader<?> previous = getLoaderManager().getLoader(LOADER_ID);
			Loader<?> current = getLoaderManager().restartLoader(LOADER_ID, args, new FileLoaderCallbacks());
			if (current.getId() == 0 && previous != null) {
				// TODO figure out why this is needed: when the user taps on two folders at the same time
				// this method is called twice, but restartLoader doesn't like to be called in quick succession
				// see "This function does some throttling of Loaders." paragraph on LoaderManagerImpl.restartLoader
				// the first call replaces the finished loader, and starts the second loader
				// the second call cancels the second loader and queues the third loader
				// problem is that onLoadCanceled is called before info.mPendingLoader is set to the third loader
				// which leaves the second loader cancelled and the third one in initial state
				// delivering a cancellation to the second loader will start the enqueued third loader
				previous.deliverCancellation();
				// To test the effect of this call do a postDelayed(before.deliverCancellation, 5000)
				// and observe that the loading indicator will be visible for 5 seconds
			}
		}

		@Override public void refresh() {
			getLoaderManager().getLoader(LOADER_ID).onContentChanged();
		}

		@Override public void close() {
			getLoaderManager().destroyLoader(LOADER_ID);
		}

		@Override protected @NonNull ImportFilesAdapter setupList() {
			list.setLayoutManager(new LinearLayoutManager(list.getContext()));
			ImportFilesAdapter adapter = new ImportFilesAdapter();
			list.setAdapter(adapter);
			return adapter;
		}

		@Override protected void onViewSet() {
			super.onViewSet();
			ImageView fab = getFAB();
			fab.setImageResource(android.R.drawable.ic_menu_save);
		}
		@Override protected void setData(@NonNull ImportFilesAdapter adapter, List<File> data) {
			adapter.setFiles(getDir(), data);
		}

		@Override public boolean canCreateNew() {
			return eventsListener != null && eventsListener.isAllowNew();
		}
		@Override protected void onCreateNew() {
			eventsListener.newIntoDir(getDir());
		}
		private class FileLoaderCallbacks implements LoaderCallbacks<List<File>> {
			@Override public Loader<List<File>> onCreateLoader(int id, Bundle args) {
				startLoading();
				File file = (File)args.getSerializable(EXTRA_PATH);
				return new FilesLoader(getContext(), file);
			}
			@Override public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
				File root = ((FilesLoader)loader).getRoot();
				onDirLoaded(root);
				updateAdapter(data);
			}
			@Override public void onLoaderReset(Loader<List<File>> loader) {
				updateAdapter(null);
			}
		}
	}
}
