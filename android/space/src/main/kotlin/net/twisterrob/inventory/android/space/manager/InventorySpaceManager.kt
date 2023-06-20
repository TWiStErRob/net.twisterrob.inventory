package net.twisterrob.inventory.android.space.manager

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Process
import androidx.core.content.getSystemService
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.Glide
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.twisterrob.android.utils.tools.DatabaseTools
import net.twisterrob.android.utils.tools.IOTools
import net.twisterrob.android.utils.tools.PackageManagerTools
import net.twisterrob.inventory.android.BaseComponent
import net.twisterrob.inventory.android.content.Database
import net.twisterrob.inventory.android.content.db.DatabaseService
import net.twisterrob.inventory.android.logger
import net.twisterrob.inventory.android.space.R
import java.io.File
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private val LOG = logger<InventorySpaceManager>()

@Suppress("TooManyFunctions")
internal class InventorySpaceManager @Inject constructor(
	private val inject: BaseComponent,
	private val database: Database,
) {
	suspend fun clearImageCache() {
		val glide = Glide.get(inject.applicationContext())
		withContext(Dispatchers.Main) { glide.clearMemory() }
		withContext(Dispatchers.IO) { glide.clearDiskCache() }
	}

	fun emptyDatabase() {
		database.helper.run {
			@Suppress("ConvertTryFinallyToUseCall") // AutoClosable was added in Q.
			try {
				onDestroy(writableDatabase)
				onCreate(writableDatabase)
			} finally {
				close()
			}
		}
		inject.prefs().setString(R.string.pref_currentLanguage, null)
		inject.prefs().setBoolean(R.string.pref_showWelcome, true)
	}

	fun clearImages() {
		database.clearImages()
	}

	fun resetToTestData() {
		database.resetToTest()
	}

	fun vacuumDatabase() {
		database.writableDatabase.execSQL("VACUUM;")
	}

	fun vacuumDatabaseIncremental(vacuumBytes: Int) {
		val db = database.writableDatabase
		val pagesToFree = vacuumBytes / db.pageSize
		val vacuum =
			db.rawQuery("PRAGMA incremental_vacuum($pagesToFree);", DatabaseTools.NO_ARGS)
		DatabaseTools.consume(vacuum)
	}

	suspend fun dumpDatabase(target: Uri) = withContext(Dispatchers.IO) {
		val name = DocumentFile.fromSingleUri(inject.applicationContext(), target)?.name
		LOG.debug("Saving DB to {} ({})", target, name)
		val source = database.file.inputStream()
		val out = inject.applicationContext().contentResolver.openOutputStream(target)
			?: error("Cannot open ${target} for writing.")
		IOTools.copyStream(source, out)
		LOG.debug("Saved DB to {} ({})", target, name)
	}

	suspend fun restoreDatabase(source: Uri) {
		withContext(Dispatchers.Main) {
			DatabaseService.clearVacuumAlarm(inject.applicationContext())
		}
		withContext(Dispatchers.IO) {
			try {
				val stream = inject.applicationContext().contentResolver.openInputStream(source)
				database.helper.restore(stream)
				LOG.debug("Restored {}", source)
			} catch (ex: CancellationException) {
				throw ex
			} catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
				LOG.error("Cannot restore {}", source, ex)
			}
		}
	}

	fun clearData() {
		if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
			val am = inject.applicationContext().getSystemService<ActivityManager>()!!
			am.clearApplicationUserData()
		} else {
			// Best effort: clear prefs, db and Glide cache; CONSIDER deltree getFilesDir()
			inject.prefs().edit().clear().apply()
			Glide.get(inject.applicationContext()).clearDiskCache()
			val dbFile = database.file
			database.helper.close()
			if (dbFile.exists() && !dbFile.delete()) {
				error("Cannot delete database file: ${dbFile}")
			}
		}
	}

	fun dumpAllData(target: Uri) {
		val contentResolver = inject.applicationContext().contentResolver
		ZipOutputStream(contentResolver.openOutputStream(target)).use { zip ->
			val description = StringBuilder()
			val applicationInfo = inject.applicationContext().applicationInfo
			if (applicationInfo.dataDir != null) {
				val internalDataDir = File(applicationInfo.dataDir)
				IOTools.zip(zip, internalDataDir, "internal")
				description.append("internal\tgetApplicationInfo().dataDir: ")
					.append(internalDataDir).append("\n")
			}
			val externalFilesDir = inject.applicationContext().getExternalFilesDir(null)
			if (externalFilesDir != null) {
				val externalDataDir = externalFilesDir.parentFile
				IOTools.zip(zip, externalDataDir, "external")
				description.append("external\tgetExternalFilesDir(null): ").append(externalDataDir)
					.append("\n")
			}
			IOTools.zip(zip, "descript.ion", IOTools.stream(description.toString()))
			zip.finish()
		}
	}

	fun rebuildSearch() {
		database.rebuildSearch()
	}

	fun killProcessesAroundManageSpaceActivity() {
		val context = inject.applicationContext()
		val myProcessName = context.manageSpaceActivity.processName
		val myProcessPrefix = context.applicationInfo.processName
		val am = context.getSystemService<ActivityManager>()!!
		for (proc in am.runningAppProcesses) {
			if (proc.processName.startsWith(myProcessPrefix) && proc.processName != myProcessName) {
				Process.killProcess(proc.pid)
			}
		}
	}
}

private val Context.manageSpaceActivity: ActivityInfo
	get() {
		val pm = this.packageManager
		val applicationInfo = this.applicationInfo
		val activity = ComponentName(this.packageName, applicationInfo.manageSpaceActivityName)
		return PackageManagerTools.getActivityInfo(pm, activity, 0)
	}
