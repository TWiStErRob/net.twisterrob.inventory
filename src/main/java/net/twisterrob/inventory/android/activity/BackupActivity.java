package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.*;

import android.content.*;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.*;
import android.text.format.Formatter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.content.loader.AsyncLoader;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.*;
import net.twisterrob.inventory.android.activity.space.ManageSpaceActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.view.RecyclerViewLoaderController;

public class BackupActivity extends BaseActivity implements OnRefreshListener {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivity.class);
	private static final String EXTRA_PATH = "path";
	private RecyclerViewLoaderController<ImportFilesAdapter, List<File>> controller;
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_backup);

		controller = new BackupListController();
		controller.setView((RecyclerView)findViewById(R.id.backups));
		filePicked(getDir());
	}

	@Override protected void onRestart() {
		super.onRestart();
		controller.refresh();
	}

	@NonNull private File getDir() {
		String backupPath = App.getSPref(R.string.pref_state_backup_path, Paths.getPhoneHome().toString());
		return new File(backupPath);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		if (!super.onCreateOptionsMenu(menu)) {
			return false;
		}
		getMenuInflater().inflate(R.menu.backup, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				File parent = getDir().getParentFile();
				if (parent != null) {
					filePicked(parent);
				} else {
					App.toastUser(getString(R.string.backup_no_parent));
				}
				return true;
			case R.id.action_export_home:
				filePicked(Paths.getPhoneHome());
				return true;
			case R.id.action_export:
				ExportFragment.create(BackupActivity.this, getSupportFragmentManager()).execute(getDir());
				return true;
			case R.id.action_manage_space:
				startActivity(ManageSpaceActivity.launch());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	public void filePicked(File file) {
		LOG.trace("File picked (dir={}, exists={}): {}", file.isDirectory(), file.exists(), file);
		if (file.isDirectory()) {
			controller.startLoad(Intents.bundleFrom(EXTRA_PATH, file));
		} else {
			ImportFragment.create(this, getSupportFragmentManager()).execute(file);
		}
	}

	public static Intent chooser() {
		return new Intent(App.getAppContext(), BackupActivity.class);
	}

	@Override public void onRefresh() {
		controller.refresh();
	}

	private class ImportFilesAdapter extends RecyclerView.Adapter<ImportFilesAdapter.ViewHolder> {
		private List<File> files;

		public void setFiles(List<File> files) {
			this.files = files;
			notifyDataSetChanged();
		}

		@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			return new ViewHolder(inflater.inflate(R.layout.item_backup, parent, false));
		}

		@Override public void onBindViewHolder(ImportFilesAdapter.ViewHolder holder, int position) {
			File file = files.get(position);

			String name = file.getName();
			String size = null;
			@RawRes int icon;
			if (name.startsWith("Inventory_") && name.endsWith(".zip")) {
				icon = R.raw.category_disc;
				name = name.replaceAll("^Inventory_(.*)\\.zip$", "$1").replace('_', ' ');
			} else {
				icon = R.raw.category_unknown;
			}

			if (file.isDirectory()) {
				icon = R.raw.category_box;
			} else {
				size = Formatter.formatShortFileSize(holder.count.getContext(), file.length());
			}

			Pic.svg().load(icon).into(holder.icon);
			holder.text.setText(name);
			holder.count.setText(size);
			AndroidTools.displayedIfHasText(holder.count);
		}

		@Override public int getItemCount() {
			return files != null? files.size() : 0;
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			final ImageView icon;
			final TextView text;
			final TextView count;

			public ViewHolder(View view) {
				super(view);
				icon = (ImageView)view.findViewById(R.id.type);
				text = (TextView)view.findViewById(R.id.title);
				count = (TextView)view.findViewById(R.id.count);

				view.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						filePicked(files.get(getAdapterPosition()));
					}
				});
			}
		}
	}

	private static class FilesLoader extends AsyncLoader<List<File>> {
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
				final Pattern pattern = Pattern.compile(".*\\.zip$");
				public boolean accept(File file) {
					return file.isFile() && file.canRead() && pattern.matcher(file.getName()).matches();
				}
			});
			return files != null? files : new File[0];
		}
	}

	private class BackupListController extends RecyclerViewLoaderController<ImportFilesAdapter, List<File>> {
		public BackupListController() {
			super(BackupActivity.this);
		}

		@Override public void startLoad(Bundle args) {
			getLoaderManager().restartLoader(1, args, new FileLoaderCallbacks());
		}
		@Override public void refresh() {
			getLoaderManager().getLoader(1).onContentChanged();
		}

		@Override protected @NonNull ImportFilesAdapter setupList() {
			list.setLayoutManager(new LinearLayoutManager(list.getContext()));
			ImportFilesAdapter adapter = new ImportFilesAdapter();
			list.setAdapter(adapter);
			return adapter;
		}

		@Override protected void setData(ImportFilesAdapter adapter, List<File> data) {
			adapter.setFiles(data);
		}

		private class FileLoaderCallbacks implements LoaderCallbacks<List<File>> {
			@Override public Loader<List<File>> onCreateLoader(int id, Bundle args) {
				startLoading();
				File file = (File)args.getSerializable(EXTRA_PATH);
				return new FilesLoader(getContext(), file);
			}
			@Override public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
				File root = ((FilesLoader)loader).getRoot();
				((TextView)findViewById(R.id.backup_location)).setText(root.toString());
				updateAdapter(data);
				App.setSPref(R.string.pref_state_backup_path, root.getAbsolutePath());
			}
			@Override public void onLoaderReset(Loader<List<File>> loader) {
				updateAdapter(null);
			}
		}
	}
}
