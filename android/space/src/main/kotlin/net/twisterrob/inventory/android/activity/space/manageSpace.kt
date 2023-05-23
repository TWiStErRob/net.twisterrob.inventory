package net.twisterrob.inventory.android.activity.space

import android.annotation.TargetApi
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.text.format.Formatter
import android.widget.Toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.twisterrob.android.utils.tools.DatabaseTools
import net.twisterrob.android.utils.tools.IOTools
import net.twisterrob.android.utils.tools.TextTools
import net.twisterrob.inventory.android.Constants.Pic.GlideSetup
import net.twisterrob.inventory.android.activity.space.ManageSpaceState.Confirmation
import net.twisterrob.inventory.android.activity.space.ManageSpaceState.Confirmation.Result.CANCELLED
import net.twisterrob.inventory.android.activity.space.ManageSpaceState.Confirmation.Result.CONFIRMED
import net.twisterrob.inventory.android.activity.space.ManageSpaceState.SizesState
import net.twisterrob.inventory.android.components.ErrorMapper
import net.twisterrob.inventory.android.content.Database
import net.twisterrob.inventory.android.space.R
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class ManageSpaceViewModel @Inject constructor(
	private val useCase: GetSizesUseCase,
	private val mapper: SizesDomainToStateMapper,
	private val manager: InventorySpaceManager,
) : BaseViewModel<ManageSpaceState, ManageSpaceEffect>(
	initialState = ManageSpaceState(
		isLoading = false,
		sizes = null,
		confirmation = null,
	)
) {
	private val confirmations = Channel<Confirmation.Result>()
		.also { addCloseable { it.cancel(CancellationException("ManageSpaceViewModel cleared")) } }

	fun actionConfirmed() {
		intent {
			confirmations.send(Confirmation.Result.CONFIRMED)
		}
	}

	fun actionCancelled() {
		intent {
			confirmations.send(Confirmation.Result.CANCELLED)
		}
	}

	fun screenVisible() {
		intent {
			if (state.confirmation != null) {
				LOG.trace("Skipping loadSizes() because a confirmation is in progress.")
				return@intent
			}
			loadSizes()
		}
	}

	fun loadSizes(force: Boolean = false) {
		intent {
			if (state.isLoading && !force) {
				LOG.trace("Skipping loadSizes() because something is in progress.")
				return@intent
			}
			reduce {
				state.copy(
					isLoading = true,
					sizes = mapper.loading(),
				)
			}
			val input = useCase.execute(Unit)
			val sizes = mapper.map(input)
			if (sizes.errors != null) {
				postSideEffect(ManageSpaceEffect.ShowToast(sizes.errors))
			}
			reduce {
				state.copy(
					isLoading = false,
					sizes = sizes,
				)
			}
		}
	}

	fun clearImageCache() {
		intent {
			confirmedClean(
				title = "Clear Image Cache",
				message = "You're about to remove all files in the image cache. "
					+ "There will be no permanent loss. "
					+ "The cache will be re-filled as required in the future.",
				progress = { it.copy(imageCache = "Clearing… " + it.imageCache) },
				action = manager::clearImageCache,
			)
		}
	}

	fun emptyDatabase() {
		intent {
			confirmedClean(
				title = "Empty Database",
				message = "All of your belongings will be permanently deleted.",
				progress = { it.copy(database = "Emptying… " + it.database) },
				action = manager::emptyDatabase,
			)
		}
	}

	fun dumpDatabase(target: Uri) {
		intent {
			unconfirmedClean(
				progress = { it.copy(database = "Dumping… " + it.database) },
			) {
				manager.dumpDatabase(target)
			}
		}
	}

	fun clearImages() {
		intent {
			confirmedClean(
				title = "Clear Images",
				message = "Images of all your belongings will be permanently deleted, all other data is kept.",
				progress = { it.copy(database = "Clearing images…") },
				action = manager::clearImages,
			)
		}
	}

	fun resetTestData() {
		intent {
			confirmedClean(
				title = "Reset to Test Data",
				message = "All of your belongings will be permanently deleted. Some test data will be set up.",
				progress = { it.copy(database = "Resetting…") },
				action = manager::resetToTestData,
			)
		}
	}

	fun restoreDatabase(source: Uri) {
		intent {
			unconfirmedClean(
				progress = { it.copy(database = "Restoring…") },
			) {
				manager.restoreDatabase(source)
			}
		}
	}

	fun vacuumDatabase() {
		intent {
			confirmedClean(
				title = "Vacuum the Database",
				message = "May take a while depending on database size, "
					+ "also requires at least the size of the database as free space.",
				progress = { it.copy(database = "Vacuuming…") },
				action = manager::vacuumDatabase,
			)
		}
	}

	fun vacuumDatabaseIncremental(vacuumBytes: Int) {
		intent {
			unconfirmedClean(
				progress = { it.copy(database = "Vacuuming…") },
			) {
				manager.vacuumDatabaseIncremental(vacuumBytes)
			}
		}
	}

	@TargetApi(VERSION_CODES.KITKAT)
	fun clearData() {
		intent {
			confirmedClean(
				title = "Clear Data",
				message = "All of your belongings and user preferences will be permanently deleted. "
					+ "Any backups will be kept, even after you uninstall the app.",
				progress = {
					it.copy(
						database = "Clearing…",
						imageCache = "Clearing…",
						freelist = "Clearing…",
						searchIndex = "Clearing…",
						allData = "Clearing…",
					)
				},
				action = manager::clearData,
			)
		}
	}

	fun dumpAllData(target: Uri) {
		intent {
			unconfirmedClean(
				progress = {
					it.copy(
						database = "Dumping…",
						imageCache = "Dumping…",
						freelist = "Dumping…",
						searchIndex = "Dumping…",
						allData = "Dumping…",
					)
				},
			) {
				manager.dumpAllData(target)
			}
		}
	}

	fun rebuildSearch() {
		intent {
			confirmedClean(
				title = "Rebuild search index…",
				message = "Continuing will re-build the search index, it may take a while.",
				progress = { it.copy(searchIndex = "Rebuilding… " + it.searchIndex) },
				action = manager::rebuildSearch,
			)
		}
	}

	private suspend fun SimpleSyntax<ManageSpaceState, ManageSpaceEffect>.confirmedClean(
		title: CharSequence,
		message: CharSequence,
		progress: (SizesState) -> SizesState,
		action: suspend () -> Unit,
	) {
		reduce {
			state.copy(
				confirmation = Confirmation(
					title = title,
					message = message,
				),
			)
		}
		when (confirmations.receive()) {
			CONFIRMED -> {
				unconfirmedClean(progress, action)
			}
			CANCELLED -> {
				reduce {
					state.copy(
						confirmation = null,
					)
				}
			}
		}
	}

	private suspend fun SimpleSyntax<ManageSpaceState, ManageSpaceEffect>.unconfirmedClean(
		progress: (SizesState) -> SizesState,
		action: suspend () -> Unit,
	) {
		reduce {
			state.copy(
				confirmation = null,
				// STOPSHIP NoProgressTaskExecutor.create(object : CleanTask() {
				isLoading = true,
				sizes = state.sizes?.let(progress),
			)
		}
		delay(1000) // STOPSHIP remove this
		manager.killProcessesAroundManageSpaceActivity()
		try {
			action()
		} catch (ex: CancellationException) {
			throw ex
		} catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
			LOG.error("cleanTask failed", ex)
			postSideEffect(ManageSpaceEffect.ShowToast(ex.toString()))
		}
		manager.killProcessesAroundManageSpaceActivity()
		loadSizes(force = true)
	}

	companion object {
		private val LOG = LoggerFactory.getLogger(ManageSpaceViewModel::class.java)
	}
}

internal data class ManageSpaceState(
	val isLoading: Boolean,
	val sizes: SizesState?,
	val confirmation: Confirmation?,
) {

	internal data class SizesState(
		val imageCache: CharSequence,
		val database: CharSequence,
		val freelist: CharSequence,
		val searchIndex: CharSequence,
		val allData: CharSequence,
		val errors: CharSequence?,
	)

	internal data class Confirmation(
		val title: CharSequence,
		val message: CharSequence,
	) {
		enum class Result {
			CONFIRMED,
			CANCELLED,
		}
	}
}

internal sealed class ManageSpaceEffect {
	data class ShowToast(
		val message: CharSequence
	) : ManageSpaceEffect()
}

internal class ManageSpaceEffectHandler @Inject constructor(
	@ActivityContext private val context: Context
) : EffectHandler<ManageSpaceEffect> {

	override suspend fun handleEffect(effect: ManageSpaceEffect) {
		when (effect) {
			is ManageSpaceEffect.ShowToast -> {
				Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
			}
		}
	}
}

class GetSizesUseCase @Inject constructor(
	@ApplicationContext private val context: Context,
) : UseCase<Unit, SizesDomain> {

	override suspend fun execute(input: Unit): SizesDomain = withContext(Dispatchers.IO) {
		delay(1000) // STOPSHIP remove this
		val database = Database.get(context)
		SizesDomain(
			imageCache = safe {
				dirSizes(
					GlideSetup.getCacheDir(context)
				)
			},
			database = safe {
				dirSizes(
					context.getDatabasePath(database.helper.databaseName)
				)
			},
			freelist = safe {
				database.readableDatabase.let { it.freelistCount * it.pageSize }
			},
			searchIndex = safe {
				database.searchSize
			},
			allData = safe {
				dirSizes(
					File(context.applicationInfo.dataDir),
					context.externalCacheDir,
					context.getExternalFilesDir(null),
				)
			},
		)
	}

	private fun dirSizes(vararg dirs: File?): Long =
		dirs.sumOf { IOTools.calculateSize(it) }

	private val SQLiteDatabase.freelistCount: Long
		get() = DatabaseUtils.longForQuery(
			this,
			"PRAGMA freelist_count;",
			DatabaseTools.NO_ARGS
		)

	private fun safe(block: () -> Long): Result<Long> =
		try {
			Result.success(block())
		} catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
			LOG.error("Cannot get size", ex)
			Result.failure(ex)
		}

	companion object {
		private val LOG = LoggerFactory.getLogger(GetSizesUseCase::class.java)
	}
}

data class SizesDomain(
	val imageCache: Result<Long>,
	val database: Result<Long>,
	val freelist: Result<Long>,
	val searchIndex: Result<Long>,
	val allData: Result<Long>,
)

internal class SizesDomainToStateMapper @Inject constructor(
	@ApplicationContext private val context: Context,
	private val errorMapper: ErrorMapper,
) : Mapper<SizesDomain, SizesState> {
	override fun map(input: SizesDomain): SizesState =
		SizesState(
			imageCache = input.imageCache.formatFileSize(),
			database = input.database.formatFileSize(),
			freelist = input.freelist.formatFileSize(),
			searchIndex = input.searchIndex.formatFileSize(),
			allData = input.allData.formatFileSize(),
			errors = formatError(
				listOf(
					input.imageCache,
					input.database,
					input.freelist,
					input.searchIndex,
					input.allData,
				)
			)
		)

	// TODEL make it vararg, workaround for https://youtrack.jetbrains.com/issue/KT-33565
	private fun formatError(results: List<Result<Long>>): CharSequence? =
		results
			.mapNotNull { it.exceptionOrNull() }
			.map { errorMapper.getError(it, "Cannot get size") }
			.takeIf { it.isNotEmpty() }
			?.let { TextTools.join("\n", it) }

	private fun Result<Long>.formatFileSize(): CharSequence =
		this.map { Formatter.formatFileSize(context, it) }.getOrElse { "?" }

	fun loading(): SizesState {
		val calculating = context.getString(R.string.manage_space_calculating)
		return SizesState(
			imageCache = calculating,
			database = calculating,
			freelist = calculating,
			searchIndex = calculating,
			allData = calculating,
			errors = null,
		)
	}
}
