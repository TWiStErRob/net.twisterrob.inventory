package net.twisterrob.inventory.android.activity.space;

import java.io.*;
import java.util.zip.ZipOutputStream;

import org.slf4j.*;

import android.annotation.*;
import android.app.*;
import android.content.Intent;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build.*;
import android.os.*;
import android.os.StrictMode.ThreadPolicy;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.Constants.Pic.GlideSetup;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.db.DatabaseService;

import static net.twisterrob.android.utils.tools.AndroidTools.*;
import static net.twisterrob.android.utils.tools.DatabaseTools.*;
import static net.twisterrob.android.utils.tools.ViewTools.*;

public class ManageSpaceActivity extends BaseActivity implements TaskEndListener {
	private static final Logger LOG = LoggerFactory.getLogger(ManageSpaceActivity.class);

	TextView imageCacheSize;
	TextView databaseSize;
	TextView freelistSize;
	TextView searchIndexSize;
	TextView allSize;
	private SwipeRefreshLayout swiper;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_space_activity);
		setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher));
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		swiper = (SwipeRefreshLayout)findViewById(R.id.refresher);
		swiper.setOnRefreshListener(new OnRefreshListener() {
			@Override public void onRefresh() {
				recalculate();
			}
		});
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
								Glide.get(getApplicationContext()).clearMemory();
							}
							@Override protected void doClean() {
								Glide.get(getApplicationContext()).clearDiskCache();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});

		databaseSize = (TextView)this.findViewById(R.id.storage_db_size);
		freelistSize = (TextView)this.findViewById(R.id.storage_db_freelist_size);
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
								App.prefs().setString(R.string.pref_currentLanguage, null);
								App.prefs().setBoolean(R.string.pref_showWelcome, true);
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
						File dumpFile = getDumpFile();
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
								App.db().resetToTest();
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});
		findViewById(R.id.storage_db_restore).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				String defaultPath = getDumpFile().getAbsolutePath();
				DialogTools
						.prompt(v.getContext(), defaultPath, new PopupCallbacks<String>() {
							@Override public void finished(final String value) {
								if (value == null) {
									return;
								}
								NoProgressTaskExecutor.create(new CleanTask() {
									@Override protected void onPreExecute() {
										super.onPreExecute();
										DatabaseService.clearVacuumAlarm(getApplicationContext());
									}
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
						.setMessage("Please enter the absolute path of the .sqlite file to restore!")
						.show()
				;
			}
		});
		findViewById(R.id.storage_db_vacuum).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Vacuum the whole Database",
						"May take a while depending on database size, also requires at least the size of the database as free space.",
						new CleanTask() {
							@Override protected void doClean() {
								App.db().getWritableDatabase().execSQL("VACUUM;");
							}
						}
				).show(getSupportFragmentManager(), null);
			}
		});
		findViewById(R.id.storage_db_vacuum_incremental).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				int tenMB = 10 * 1024 * 1024;
				DialogTools
						.pickNumber(v.getContext(), tenMB, 0, Integer.MAX_VALUE, new PopupCallbacks<Integer>() {
							@Override public void finished(final Integer value) {
								if (value == null) {
									return;
								}
								NoProgressTaskExecutor.create(new CleanTask() {
									@Override protected void doClean() {
										SQLiteDatabase db = App.db().getWritableDatabase();
										long pagesToFree = value / db.getPageSize();
										Cursor vacuum =
												db.rawQuery("PRAGMA incremental_vacuum(" + pagesToFree + ");", NO_ARGS);
										DatabaseTools.consume(vacuum);
									}
								}).show(getSupportFragmentManager(), null);
							}
						})
						.setTitle("Incremental Vacuum")
						.setMessage("How many bytes do you want to vacuum?")
						.show()
				;
			}
		});

		allSize = (TextView)this.findViewById(R.id.storage_all_size);
		findViewById(R.id.storage_all_clear).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				new ConfirmedCleanAction("Clear Data",
						"All of your belongings and user preferences will be permanently deleted. Any backups will be kept, even after you uninstall the app.",
						new CleanTask() {
							@TargetApi(VERSION_CODES.KITKAT)
							@Override protected void doClean() {
								if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
									((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
								} else {
									// Best effort: clear prefs, db and Glide cache; CONSIDER deltree getFilesDir()
									App.prefs().edit().clear().apply();
									Glide.get(getApplicationContext()).clearDiskCache();
									Database db = App.db();
									File dbFile = db.getFile();
									db.getHelper().close();
									if (dbFile.exists() && !dbFile.delete()) {
										App.toast("Cannot delete database file: " + dbFile);
									}
								}
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

	private File getDumpFile() {
		return new File(Paths.getPhoneHome(), "db.sqlite");
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

	@SuppressLint("WrongThreadInterprocedural")
	void recalculate() {
		ThreadPolicy threadPolicy = StrictMode.allowThreadDiskWrites();
		try { // TODEL try to fix these somehow
			executePreferParallel(new GetFolderSizesTask(imageCacheSize),
					GlideSetup.getCacheDir(this)
			);
			executePreferParallel(new GetFolderSizesTask(databaseSize),
					// TODO illegal WrongThreadInterprocedural detection, but cannot reproduce
					getDatabasePath(App.db().getHelper().getDatabaseName())
			);
			executePreferParallel(new GetFolderSizesTask(allSize),
					new File(getApplicationInfo().dataDir),
					getExternalCacheDir(),
					getExternalFilesDir(null)
			);
			executePreferParallel(new GetSizeTask<Void>(searchIndexSize) {
				@Override protected @NonNull Long doInBackgroundSafe(Void... params) {
					return App.db().getSearchSize();
				}
			});
			executePreferParallel(new GetSizeTask<Void>(freelistSize) {
				@Override protected @NonNull Long doInBackgroundSafe(Void... params) {
					SQLiteDatabase db = App.db().getReadableDatabase();
					long count = DatabaseUtils.longForQuery(db, "PRAGMA freelist_count;", NO_ARGS);
					return count * db.getPageSize();
				}
			});
			swiper.setRefreshing(false);
		} finally {
			StrictMode.setThreadPolicy(threadPolicy);
		}
	}

	protected void onResume() {
		super.onResume();
		recalculate();
	}

	public static Intent launch() {
		return new Intent(App.getAppContext(), ManageSpaceActivity.class);
	}
}
