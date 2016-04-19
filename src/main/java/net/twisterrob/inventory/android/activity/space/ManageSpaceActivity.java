package net.twisterrob.inventory.android.activity.space;

import java.io.*;
import java.util.zip.ZipOutputStream;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Intent;
import android.os.Build.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.Constants.Pic.GlideSetup;
import net.twisterrob.inventory.android.activity.BaseActivity;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

public class ManageSpaceActivity extends BaseActivity implements TaskEndListener {
	private static final Logger LOG = LoggerFactory.getLogger(ManageSpaceActivity.class);

	TextView imageCacheSize;
	TextView databaseSize;
	TextView searchIndexSize;
	TextView allSize;

	protected void onCreate(Bundle var1) {
		super.onCreate(var1);
		setContentView(R.layout.manage_space_activity);
		setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher));
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		searchIndexSize = (TextView)this.findViewById(R.id.storage_search_size);
		findViewById(R.id.storage_search_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Re-build Search",
						"Continuing will re-build the search index, it may take a while.",
						new CleanTask() {
							@Override protected void doClean() {
								App.db().rebuildSearch();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});

		imageCacheSize = (TextView)this.findViewById(R.id.storage_imageCache_size);
		findViewById(R.id.storage_imageCache_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Clear Image Cache",
						"You're about to remove all files in the image cache. There will be no permanent loss. The cache will be re-filled as required in the future.",
						new CleanTask() {
							@Override protected void onPreExecute() {
								Glide glide = Glide.get(getApplicationContext());
								glide.clearMemory();
							}
							@Override protected void doClean() {
								Glide glide = Glide.get(getApplicationContext());
								glide.clearDiskCache();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});

		databaseSize = (TextView)this.findViewById(R.id.storage_db_size);
		findViewById(R.id.storage_db_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Empty Database",
						"All of your belongings will be permanently deleted.",
						new CleanTask() {
							@Override protected void doClean() {
								DatabaseOpenHelper helper = App.db().getHelper();
								helper.onDestroy(helper.getWritableDatabase());
								helper.onCreate(helper.getWritableDatabase());
								helper.close();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});
		findViewById(R.id.storage_db_dump).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				NoProgressTaskExecutor.create(new CleanTask() {
					@Override protected void doClean() throws IOException {
						File path = App.db().getFile();
						InputStream in = new FileInputStream(path);
						File dumpFile = new File(Paths.getPhoneHome(), "db.sqlite");
						OutputStream out = new FileOutputStream(dumpFile);
						IOTools.copyStream(in, out);
						LOG.debug("Saved DB to {}", dumpFile);
					}
				}).show(getSupportFragmentManager(), "task");
			}
		});
		findViewById(R.id.storage_images_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Clear Images",
						"Images of all your belongings will be permanently deleted, all other data is kept.",
						new CleanTask() {
							@Override protected void doClean() {
								App.db().clearImages();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});
		findViewById(R.id.storage_db_test).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Reset to Test Data",
						"All of your belongings will be permanently deleted. Some test data will be set up.",
						new CleanTask() {
							@Override protected void doClean() {
								DatabaseOpenHelper helper = App.db().getHelper();
								helper.close();
								helper.setTestMode(true);
								//noinspection resource it is closed by helper.close()
								helper.getReadableDatabase();
								helper.close();
								helper.setTestMode(false);
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});
		findViewById(R.id.storage_db_restore).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				AndroidTools
						.prompt(ManageSpaceActivity.this, new PopupCallbacks<String>() {
							@Override public void finished(final String value) {
								if (value == null) {
									return;
								}
								NoProgressTaskExecutor.create(new CleanTask() {
									@Override protected void doClean() throws Exception {
										App.db().getHelper().restore(new File(value));
									}
									@Override protected void onResult(Void ignore, Activity activity) {
										super.onResult(ignore, activity);
										LOG.debug("Restored {}", value);
									}
									@Override protected void onError(@NonNull Exception ex, Activity activity) {
										super.onError(ex, activity);
										LOG.error("Cannot restore {}", value);
									}
								}).show(getSupportFragmentManager(), "task");
							}
						})
						.setTitle("Restore DB")
						.setMessage("Please the absolute path of the .sqlite file to restore!")
						.show()
				;
			}
		});

		allSize = (TextView)this.findViewById(R.id.storage_all_size);
		findViewById(R.id.storage_all_clear).setOnClickListener(new OnClickListener() {
			@TargetApi(VERSION_CODES.KITKAT)
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Clear Data",
						"All of your belongings and user preferences will be permanently deleted. Any backups will be kept, even after you uninstall the app.",
						new CleanTask() {
							@Override protected void doClean() {
								((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});
		findViewById(R.id.storage_all_dump).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Export Data",
						"Dump the data folder to a zip file for debugging.",
						new CleanTask() {
							@Override protected void doClean() {
								zipAllData();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});
		displayedIf(findViewById(R.id.storage_all), VERSION_CODES.KITKAT <= VERSION.SDK_INT || BuildConfig.DEBUG);
	}

	@Override public void taskDone() {
		recalculate();
	}

	private void zipAllData() {
		ZipOutputStream zip = null;
		try {
			zip = new ZipOutputStream(new FileOutputStream(Paths.getExportFile(Paths.getPhoneHome())));
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
			IOTools.zip(zip, "descript.ion", IOTools.stream(description.toString()));
			zip.finish();
		} catch (IOException ex) {
			LOG.error("Cannot save data", ex);
		} finally {
			IOTools.ignorantClose(zip);
		}
	}

	@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH) void recalculate() {
		executePreferParallel(new GetFolderSizesTask(imageCacheSize),
				GlideSetup.getCacheDir(this));
		executePreferParallel(new GetFolderSizesTask(databaseSize),
				getDatabasePath(App.db().getHelper().getDatabaseName()));
		executePreferParallel(new GetFolderSizesTask(allSize),
				new File(getApplicationInfo().dataDir), getExternalCacheDir(), getExternalFilesDir(null));
		executePreferParallel(new GetSizeTask<Void>(searchIndexSize) {
			@Override protected @NonNull Long doInBackgroundSafe(Void... params) {
				return App.db().getSearchSize();
			}
		});
	}

	protected void onResume() {
		super.onResume();
		recalculate();
	}

	public static Intent launch() {
		return new Intent(App.getAppContext(), ManageSpaceActivity.class);
	}
}
