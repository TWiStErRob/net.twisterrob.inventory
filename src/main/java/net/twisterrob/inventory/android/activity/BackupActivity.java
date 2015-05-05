package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Pattern;

import android.content.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.*;
import android.text.format.Formatter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

import net.twisterrob.android.content.loader.AsyncLoader;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.activity.space.ManageSpaceActivity;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.view.RecyclerViewLoaderController;

public class BackupActivity extends BaseActivity implements OnRefreshListener {
	private RecyclerViewLoaderController<ImportFilesAdapter, File[]> controller;
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_backup);

		((TextView)findViewById(R.id.backup_location)).setText(Paths.getPhoneHome().toString());
		findViewById(R.id.btn_export).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				ExportFragment.create(BackupActivity.this, getSupportFragmentManager()).execute();
			}
		});

		controller = new BackupListController();
		controller.startLoad(null);
	}

	@Override protected void onRestart() {
		super.onRestart();
		controller.refresh();
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
			case R.id.action_manage_space:
				startActivity(ManageSpaceActivity.launch());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	public void filePicked(File file) {
		ImportFragment.create(this, getSupportFragmentManager()).execute(file);
	}

	public static Intent chooser() {
		Intent intent = new Intent(App.getAppContext(), BackupActivity.class);
		return intent;
	}

	@Override public void onRefresh() {
		controller.refresh();
	}

	private class ImportFilesAdapter extends RecyclerView.Adapter<ImportFilesAdapter.ViewHolder> {
		private File[] files;

		public void setFiles(File... files) {
			this.files = files;
			notifyDataSetChanged();
		}

		@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			return new ViewHolder(inflater.inflate(R.layout.item_backup, parent, false));
		}

		@Override public void onBindViewHolder(ImportFilesAdapter.ViewHolder holder, int position) {
			String name = files[position].getName();
			if (name.startsWith("Inventory_") && name.endsWith(".zip")) {
				name = name.replaceAll("^Inventory_(.*)\\.zip$", "$1").replace('_', ' ');
			}
			String size = Formatter.formatShortFileSize(holder.count.getContext(), files[position].length());

			holder.text.setText(name);
			holder.count.setText(size);
		}

		@Override public int getItemCount() {
			return files != null? files.length : 0;
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			TextView text;
			TextView count;

			public ViewHolder(View view) {
				super(view);
				text = (TextView)view.findViewById(R.id.title);
				count = (TextView)view.findViewById(R.id.count);

				view.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						filePicked(files[getAdapterPosition()]);
					}
				});
			}
		}
	}

	private static class FilesLoader extends AsyncLoader<File[]> {
		public FilesLoader(Context context) {
			super(context);
		}

		@Override public File[] loadInBackground() {
			File root = Paths.getPhoneHome();
			File[] files = getImportableFiles(root);
			Arrays.sort(files);
			return files;
		}

		private File[] getImportableFiles(File root) {
			return root.listFiles(new FileFilter() {
				final Pattern pattern = Pattern.compile(".*\\.zip$");
				public boolean accept(File file) {
					return file.isFile() && file.canRead() && pattern.matcher(file.getName()).matches();
				}
			});
		}
	}

	private class BackupListController extends RecyclerViewLoaderController<ImportFilesAdapter, File[]> {
		public BackupListController() {
			super(BackupActivity.this);
			setView((RecyclerView)findViewById(R.id.backups));
		}

		@Override public void startLoad(Bundle args) {
			getLoaderManager().initLoader(1, null, new FileLoaderCallbacks());
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

		@Override protected void setData(ImportFilesAdapter adapter, File[] data) {
			adapter.setFiles(data);
		}

		private class FileLoaderCallbacks implements LoaderCallbacks<File[]> {
			@Override public Loader<File[]> onCreateLoader(int id, Bundle args) {
				startLoading();
				return new FilesLoader(getContext());
			}
			@Override public void onLoadFinished(Loader<File[]> loader, File[] data) {
				updateAdapter(data);
			}
			@Override public void onLoaderReset(Loader<File[]> loader) {
				updateAdapter(null);
			}
		}
	}
}
