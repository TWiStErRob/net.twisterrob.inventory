package net.twisterrob.inventory.android.activity.space

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.database.DatabaseUtils
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import net.twisterrob.android.utils.tools.AndroidTools.executePreferParallel
import net.twisterrob.android.utils.tools.DatabaseTools
import net.twisterrob.android.utils.tools.DatabaseTools.NO_ARGS
import net.twisterrob.android.utils.tools.DialogTools
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks
import net.twisterrob.android.utils.tools.IOTools
import net.twisterrob.android.utils.tools.ViewTools
import net.twisterrob.inventory.android.BaseComponent
import net.twisterrob.inventory.android.Constants.Paths
import net.twisterrob.inventory.android.Constants.Pic.GlideSetup
import net.twisterrob.inventory.android.activity.BaseActivity
import net.twisterrob.inventory.android.content.Database
import net.twisterrob.inventory.android.content.db.DatabaseService
import net.twisterrob.inventory.android.space.R
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import net.twisterrob.inventory.android.view.RecyclerViewController
import org.orbitmvi.orbit.viewmodel.observe
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("StaticFieldLeak") // TODO use coroutines or ViewModel for this activity.
@Suppress("OVERRIDE_DEPRECATION")
class ManageSpaceActivity : BaseActivity(), TaskEndListener {
	private lateinit var binding: ManageSpaceActivityBinding
	private lateinit var inject: BaseComponent
	private val viewModel: ManageSpaceViewModel by viewModels()
	@Inject internal lateinit var effectHandler: ManageSpaceEffectHandler

	@Suppress("LongMethod")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		inject = BaseComponent.get(applicationContext)
		binding = setContentView(ManageSpaceActivityBinding::inflate)
		setIcon(ContextCompat.getDrawable(this, applicationInfo.icon))
		supportActionBar.setDisplayHomeAsUpEnabled(false)
		RecyclerViewController.initializeProgress(binding.refresher)
		binding.refresher.setOnRefreshListener { viewModel.loadSizes() }
		findViewById<View>(R.id.storage_search_clear).setOnClickListener {
			ConfirmedCleanAction(
				"Re-build Search",
				"Continuing will re-build the search index, it may take a while.",
				object : CleanTask() {
					override fun doClean() {
						Database.get(applicationContext).rebuildSearch()
					}
				}
			).show(supportFragmentManager, null)
		}
		findViewById<View>(R.id.storage_imageCache_clear).setOnClickListener {
			ConfirmedCleanAction(
				"Clear Image Cache",
				"You're about to remove all files in the image cache. "
					+ "There will be no permanent loss. "
					+ "The cache will be re-filled as required in the future.",
				object : CleanTask() {
					override fun onPreExecute() {
						Glide.get(applicationContext).clearMemory()
					}

					override fun doClean() {
						Glide.get(applicationContext).clearDiskCache()
					}
				}
			).show(supportFragmentManager, null)
		}
		findViewById<View>(R.id.storage_db_clear).setOnClickListener {
			ConfirmedCleanAction(
				"Empty Database",
				"All of your belongings will be permanently deleted.",
				object : CleanTask() {
					override fun doClean() {
						val helper = Database.get(applicationContext).helper
						helper.onDestroy(helper.writableDatabase)
						helper.onCreate(helper.writableDatabase)
						helper.close()
						inject.prefs().setString(R.string.pref_currentLanguage, null)
						inject.prefs().setBoolean(R.string.pref_showWelcome, true)
					}
				}
			).show(supportFragmentManager, null)
		}
		findViewById<View>(R.id.storage_db_dump).setOnClickListener {
			NoProgressTaskExecutor.create(object : CleanTask() {
				override fun doClean() {
					val path = Database.get(applicationContext).file
					val `in`: InputStream = FileInputStream(path)
					val dumpFile: File = dumpFile
					val out: OutputStream = FileOutputStream(dumpFile)
					IOTools.copyStream(`in`, out)
					LOG.debug("Saved DB to {}", dumpFile)
				}
			}).show(supportFragmentManager, "task")
		}
		findViewById<View>(R.id.storage_images_clear).setOnClickListener {
			ConfirmedCleanAction(
				"Clear Images",
				"Images of all your belongings will be permanently deleted, all other data is kept.",
				object : CleanTask() {
					override fun doClean() {
						Database.get(applicationContext).clearImages()
					}
				}
			).show(supportFragmentManager, null)
		}
		findViewById<View>(R.id.storage_db_test).setOnClickListener {
			ConfirmedCleanAction(
				"Reset to Test Data",
				"All of your belongings will be permanently deleted. Some test data will be set up.",
				object : CleanTask() {
					override fun doClean() {
						Database.get(applicationContext).resetToTest()
					}
				}
			).show(supportFragmentManager, null)
		}
		findViewById<View>(R.id.storage_db_restore).setOnClickListener { v ->
			val defaultPath: String = dumpFile.absolutePath
			DialogTools
				.prompt(v.context, defaultPath, object : PopupCallbacks<String> {
					override fun finished(value: String?) {
						if (value == null) {
							return
						}
						NoProgressTaskExecutor.create(object : CleanTask() {
							override fun onPreExecute() {
								DatabaseService.clearVacuumAlarm(applicationContext)
							}

							override fun doClean() {
								Database.get(applicationContext)
								        .helper
								        .restore(FileInputStream(value))
							}

							override fun onResult(ignore: Void?, activity: Activity) {
								super.onResult(ignore, activity)
								LOG.debug("Restored {}", value)
							}

							override fun onError(ex: Exception, activity: Activity) {
								super.onError(ex, activity)
								LOG.error("Cannot restore {}", value)
							}
						}).show(supportFragmentManager, "task")
					}
				})
				.setTitle("Restore DB")
				.setMessage("Please enter the absolute path of the .sqlite file to restore!")
				.show()
		}
		findViewById<View>(R.id.storage_db_vacuum).setOnClickListener {
			ConfirmedCleanAction(
				"Vacuum the whole Database",
				"May take a while depending on database size, also requires at least the size of the database as free space.",
				object : CleanTask() {
					override fun doClean() {
						Database.get(applicationContext).writableDatabase.execSQL("VACUUM;")
					}
				}
			).show(supportFragmentManager, null)
		}
		findViewById<View>(R.id.storage_db_vacuum_incremental).setOnClickListener { v ->
			@Suppress("MagicNumber")
			val tenMB = 10 * 1024 * 1024
			DialogTools
				.pickNumber(v.context, tenMB, 0, Int.MAX_VALUE, object : PopupCallbacks<Int> {
					override fun finished(value: Int?) {
						if (value == null) {
							return
						}
						NoProgressTaskExecutor.create(object : CleanTask() {
							override fun doClean() {
								val db = Database.get(applicationContext).writableDatabase
								val pagesToFree = value / db.pageSize
								val vacuum =
									db.rawQuery("PRAGMA incremental_vacuum($pagesToFree);", NO_ARGS)
								DatabaseTools.consume(vacuum)
							}
						}).show(supportFragmentManager, null)
					}
				})
				.setTitle("Incremental Vacuum")
				.setMessage("How many bytes do you want to vacuum?")
				.show()
		}
		findViewById<View>(R.id.storage_all_clear).setOnClickListener {
			ConfirmedCleanAction(
				"Clear Data",
				"All of your belongings and user preferences will be permanently deleted. "
					+ "Any backups will be kept, even after you uninstall the app.",
				object : CleanTask() {
					@TargetApi(VERSION_CODES.KITKAT)
					override fun doClean() {
						if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
							(getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
						} else {
							// Best effort: clear prefs, db and Glide cache; CONSIDER deltree getFilesDir()
							inject.prefs().edit().clear().apply()
							Glide.get(applicationContext).clearDiskCache()
							val db = Database.get(applicationContext)
							val dbFile = db.file
							db.helper.close()
							if (dbFile.exists() && !dbFile.delete()) {
								inject.toaster().toast("Cannot delete database file: ${dbFile}")
							}
						}
					}
				}
			).show(supportFragmentManager, null)
		}
		findViewById<View>(R.id.storage_all_dump).setOnClickListener {
			ConfirmedCleanAction(
				"Export Data",
				"Dump the data folder to a zip file for debugging.",
				object : CleanTask() {
					override fun doClean() {
						zipAllData()
					}
				}
			).show(supportFragmentManager, null)
		}
		ViewTools.displayedIf(findViewById(R.id.storage_all), inject.buildInfo().isDebug)
		viewModel.observe(this, state = ::updateUi, sideEffect = effectHandler::handleEffect)
	}

	private fun updateUi(state: ManageSpaceState) {
		LOG.trace("Updating UI with state {}", state)
		binding.refresher.isRefreshing = state.isLoading
		binding.contents.storageImageCacheSize.text = state.sizes?.imageCache
		binding.contents.storageDbSize.text = state.sizes?.database
		binding.contents.storageDbFreelistSize.text = state.sizes?.freelist
		binding.contents.storageSearchSize.text = state.sizes?.searchIndex
		binding.contents.storageAllSize.text = state.sizes?.allData
	}

	override fun taskDone() {
		recalculate()
	}

	private val dumpFile: File
		get() = File(Paths.getPhoneHome(), "db.sqlite")

	private fun zipAllData() {
		var zip: ZipOutputStream? = null
		try {
			zip = ZipOutputStream(FileOutputStream(Paths.getExportFile(Paths.getPhoneHome())))
			val description = StringBuilder()
			if (applicationInfo.dataDir != null) {
				val internalDataDir = File(applicationInfo.dataDir)
				IOTools.zip(zip, internalDataDir, "internal")
				description.append("internal\tgetApplicationInfo().dataDir: ").append(internalDataDir).append("\n")
			}
			val externalFilesDir = getExternalFilesDir(null)
			if (externalFilesDir != null) {
				val externalDataDir = externalFilesDir.parentFile
				IOTools.zip(zip, externalDataDir, "external")
				description.append("external\tgetExternalFilesDir(null): ").append(externalDataDir).append("\n")
			}
			IOTools.zip(zip, "descript.ion", IOTools.stream(description.toString()))
			zip.finish()
		} catch (ex: IOException) {
			LOG.error("Cannot save data", ex)
		} finally {
			IOTools.ignorantClose(zip)
		}
	}

	@SuppressLint("WrongThreadInterprocedural")
	fun recalculate() {
		val threadPolicy = StrictMode.allowThreadDiskWrites()
		try { // TODEL try to fix these somehow
			executePreferParallel(
				GetFolderSizesTask(binding.contents.storageImageCacheSize),
				GlideSetup.getCacheDir(this)
			)
			executePreferParallel(
				GetFolderSizesTask(binding.contents.storageDbSize),  // TODO illegal WrongThreadInterprocedural detection, but cannot reproduce
				getDatabasePath(Database.get(applicationContext).helper.databaseName)
			)
			executePreferParallel(
				GetFolderSizesTask(binding.contents.storageAllSize),
				File(applicationInfo.dataDir),
				externalCacheDir,
				getExternalFilesDir(null)
			)
			executePreferParallel(object : GetSizeTask<Void>(binding.contents.storageSearchSize) {
				override fun doInBackgroundSafe(vararg params: Void): Long {
					return Database.get(applicationContext).searchSize
				}
			})
			executePreferParallel(object : GetSizeTask<Void>(binding.contents.storageDbFreelistSize) {
				override fun doInBackgroundSafe(vararg params: Void): Long {
					val db = Database.get(applicationContext).readableDatabase
					val count = DatabaseUtils.longForQuery(db, "PRAGMA freelist_count;", NO_ARGS)
					return count * db.pageSize
				}
			})
			binding.refresher.isRefreshing = false
		} finally {
			StrictMode.setThreadPolicy(threadPolicy)
		}
	}

	override fun onResume() {
		super.onResume()
		recalculate()
	}

	companion object {
		private val LOG = LoggerFactory.getLogger(ManageSpaceActivity::class.java)

		@JvmStatic
		fun launch(context: Context): Intent =
			Intent(context, ManageSpaceActivity::class.java)
	}
}
