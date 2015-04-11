package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.zip.ZipOutputStream;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.database.Cursor;
import android.os.*;
import android.os.Build.*;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.Formatter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

public class ManageSpaceActivity extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ManageSpaceActivity.class);

	TextView imageCacheSize;
	TextView databaseSize;
	TextView searchIndexSize;
	TextView imagesSize;
	TextView allSize;

	protected void onCreate(Bundle var1) {
		super.onCreate(var1);
		this.setContentView(R.layout.manage_space_activity);

		searchIndexSize = (TextView)this.findViewById(R.id.storage_search_size);
		findViewById(R.id.storage_search_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmDialog("Re-build Search",
						"Continuing will re-build the search index, it may take a while.") {
					@Override protected void doClean() {
						App.db().rebuildSearch();
					}
				}.show(getSupportFragmentManager(), null);
			}
		});

		imageCacheSize = (TextView)this.findViewById(R.id.storage_imageCache_size);
		findViewById(R.id.storage_imageCache_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmDialog("Clear Image Cache",
						"You're about to remove all files in the image cache. There will be no permanent damage done. The cache will be filled as required in the future..") {
					@Override protected void doClean() {
						//  TODO 3.5.3 Glide.get(getApplicationContext()).clearDiskCache();
						IOTools.delete(Glide.getPhotoCacheDir(App.getAppContext()));
					}
				}.show(getSupportFragmentManager(), null);
			}
		});

		databaseSize = (TextView)this.findViewById(R.id.storage_db_size);
		findViewById(R.id.storage_db_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmDialog("Empty Database",
						"All of your belongings will be permanently deleted.") {
					@Override protected void doClean() {
						DatabaseOpenHelper helper = App.db().getHelper();
						helper.onDestroy(helper.getWritableDatabase());
						helper.onCreate(helper.getWritableDatabase());
						helper.close();
					}
				}.show(getSupportFragmentManager(), null);
			}
		});

		imagesSize = (TextView)this.findViewById(R.id.storage_images_size);
		findViewById(R.id.storage_images_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmDialog("Clear Images",
						"All images your belongings will be permanently deleted, other data is kept.") {
					@Override protected void doClean() {
						App.db().clearImages();
						IOTools.delete(Paths.getImageDirectory(App.getAppContext()));
					}
				}.show(getSupportFragmentManager(), null);
			}
		});

		allSize = (TextView)this.findViewById(R.id.storage_all_size);
		findViewById(R.id.storage_all_clear).setOnClickListener(new OnClickListener() {
			@TargetApi(VERSION_CODES.KITKAT)
			@Override public void onClick(View v) {
				new ConfirmDialog("Clear Data",
						"All of your belongings and user preferences will be permanently deleted. Any backups will be kept, even after you uninstall the app.") {
					@Override public void onClick(DialogInterface var1, int var2) {
						((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
					}
				}.show(getSupportFragmentManager(), null);
			}
		});

		findViewById(R.id.storage_all_dump).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmDialog("Export Data",
						"Dump the data folder to a zip file for debugging.") {
					@Override public void onClick(DialogInterface var1, int var2) {
						zipAllData();
					}
				}.show(getSupportFragmentManager(), null);
			}
		});
		findViewById(R.id.storage_all)
				.setVisibility(VERSION_CODES.KITKAT <= VERSION.SDK_INT || BuildConfig.DEBUG? View.VISIBLE : View.GONE);
	}

	private void zipAllData() {
		ZipOutputStream zip = null;
		try {
			zip = new ZipOutputStream(new FileOutputStream(Paths.getExportFile()));
			StringBuilder description = new StringBuilder();
			if (getApplicationInfo().dataDir != null) {
				File internalDataDir = new File(getApplicationInfo().dataDir);
				IOTools.zip(zip, internalDataDir, "internal");
				description.append("internal\tgetApplicationInfo().dataDir: ").append(internalDataDir).append("\n");
			}
			File externalFilesDir = getExternalFilesDir(null);
			if (externalFilesDir != null) {
				File externalDataDir = externalFilesDir.getParentFile();
				IOTools.zip(zip, externalDataDir, "external");
				description.append("external\tgetExternalFilesDir(null): ").append(externalDataDir).append("\n");
			}
			IOTools.zip(zip, "descript.ion", new ByteArrayInputStream(description.toString().getBytes("UTF-8")));
			zip.finish();
		} catch (IOException ex) {
			LOG.error("Cannot save data", ex);
		} finally {
			IOTools.ignorantClose(zip);
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.manage_space, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_backup:
				startActivity(BackupActivity.chooser());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	void killProcesses() {
		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		String myProcessPrefix = getApplicationInfo().processName;
		String myProcessName = AndroidTools.getActivityInfo(this, 0).processName;
		for (ActivityManager.RunningAppProcessInfo proc : am.getRunningAppProcesses()) {
			if (proc.processName.startsWith(myProcessPrefix) && !proc.processName.equals(myProcessName)) {
				Process.killProcess(proc.pid);
			}
		}
	}

	void recalculate() {
		executeParallel(new GetFolderSize(imagesSize), Paths.getImageDirectory(this));
		executeParallel(new GetFolderSize(imageCacheSize), Glide.getPhotoCacheDir(this));
		executeParallel(new GetFolderSize(databaseSize), new File(App.db().getWritableDatabase().getPath()));
		executeParallel(new GetFolderSize(allSize),
				new File(getApplicationInfo().dataDir), getExternalCacheDir(), getExternalFilesDir(null));
		executeParallel(new GetSize<Void>(searchIndexSize) {
			@Override protected Long doInBackground(Void... params) {
				// sum 0 rows is NULL, count 0 rows is 0...
				String sql = "select coalesce(sum(length(name) + length(location)), 0) + count() * 4 * 3 from Search";
				Cursor cursor = App.db().getReadableDatabase().rawQuery(sql, null);
				return (long)DatabaseTools.singleResult(cursor);
			}
		});
	}

	protected void onResume() {
		super.onResume();
		recalculate();
	}

	public static Intent launch() {
		Intent intent = new Intent(App.getAppContext(), ManageSpaceActivity.class);
		return intent;
	}

	private abstract class ConfirmDialog extends DialogFragment implements DialogInterface.OnClickListener {
		private CharSequence titleRes;
		private CharSequence textRes;

		ConfirmDialog(CharSequence titleRes, CharSequence textRes) {
			this.titleRes = titleRes;
			this.textRes = textRes;
		}

		protected void doClean() {
		}

		@Override public void onClick(DialogInterface var1, int var2) {
			killProcesses();
			doClean();
			killProcesses();
			recalculate();
		}

		@Override @NonNull public Dialog onCreateDialog(Bundle var1) {
			return new Builder(getActivity())
					.setTitle(titleRes)
					.setMessage(textRes)
					.setPositiveButton(android.R.string.ok, this)
					.setNegativeButton(android.R.string.cancel, null)
					.create();
		}
	}

	private static abstract class GetSize<Param> extends AsyncTask<Param, Long, Long> {
		private final TextView result;
		public GetSize(TextView result) {
			this.result = result;
		}
		@Override protected void onPreExecute() {
			result.setText("Calculating...");
		}

		@Override protected void onProgressUpdate(Long... values) {
			//result.setText("Calculating... (" + getSize(values[0]) + ")");
		}

		@Override protected void onPostExecute(Long size) {
			result.setText(getSize(size));
		}

		private String getSize(long size) {
			return Formatter.formatFileSize(result.getContext(), size);
		}
	}

	private static class GetFolderSize extends GetSize<File> {
		public GetFolderSize(TextView result) {
			super(result);
		}

		@Override protected Long doInBackground(File... dirs) {
			long result = 0;
			for (File dir : dirs) {
				publishProgress(result);
				result += IOTools.calculateSize(dir);
			}
			return result;
		}
	}
}
