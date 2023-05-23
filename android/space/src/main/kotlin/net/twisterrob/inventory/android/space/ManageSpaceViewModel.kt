package net.twisterrob.inventory.android.space

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build.VERSION_CODES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import net.twisterrob.inventory.android.Constants.Paths
import net.twisterrob.inventory.android.space.ManageSpaceUiState.ConfirmationUiState
import net.twisterrob.inventory.android.space.ManageSpaceUiState.SizesUiState
import net.twisterrob.inventory.android.space.manager.InventorySpaceManager
import net.twisterrob.inventory.android.space.sizes.GetSizesUseCase
import net.twisterrob.inventory.android.space.sizes.SizesDomainToStateMapper
import net.twisterrob.inventory.android.viewmodel.BaseViewModel
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.slf4j.LoggerFactory
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions") // Blame the screen, not the ViewModel?
internal class ManageSpaceViewModel @Inject constructor(
	private val useCase: GetSizesUseCase,
	private val mapper: SizesDomainToStateMapper,
	private val manager: InventorySpaceManager,
) : BaseViewModel<ManageSpaceUiState, ManageSpaceUiEffect>(
	initialState = ManageSpaceUiState(
		isLoading = false,
		sizes = null,
		confirmation = null,
	)
) {
	private val confirmations = Channel<ConfirmationResult>()
		.also { addCloseable { it.cancel(CancellationException("ManageSpaceViewModel cleared")) } }

	private enum class ConfirmationResult {
		CONFIRMED,
		CANCELLED,
	}

	fun actionConfirmed() {
		intent {
			confirmations.send(ConfirmationResult.CONFIRMED)
		}
	}

	fun actionCancelled() {
		intent {
			confirmations.send(ConfirmationResult.CANCELLED)
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
				postSideEffect(ManageSpaceUiEffect.ShowToast(sizes.errors))
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

	fun dumpDatabase() {
		intent {
			val fileName = Paths.getFileName("Inventory", Calendar.getInstance(), "sqlite")
			postSideEffect(ManageSpaceUiEffect.PickDumpDatabaseTarget(fileName))
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

	fun restoreDatabase() {
		intent {
			postSideEffect(ManageSpaceUiEffect.PickRestoreDatabaseSource)
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

	fun vacuumDatabaseIncremental() {
		intent {
			postSideEffect(ManageSpaceUiEffect.PickVacuumDatabaseIncrementalBytes)
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

	fun dumpAllData() {
		intent {
			val fileName = Paths.getFileName("Inventory_dump", Calendar.getInstance(), "zip")
			postSideEffect(ManageSpaceUiEffect.PickDumpAllDataTarget(fileName))
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

	private suspend fun SimpleSyntax<ManageSpaceUiState, ManageSpaceUiEffect>.confirmedClean(
		title: CharSequence,
		message: CharSequence,
		progress: (SizesUiState) -> SizesUiState,
		action: suspend () -> Unit,
	) {
		reduce {
			state.copy(
				confirmation = ConfirmationUiState(
					title = title,
					message = message,
				),
			)
		}
		when (confirmations.receive()) {
			ConfirmationResult.CONFIRMED -> {
				unconfirmedClean(progress, action)
			}
			ConfirmationResult.CANCELLED -> {
				reduce {
					state.copy(
						confirmation = null,
					)
				}
			}
		}
	}

	private suspend fun SimpleSyntax<ManageSpaceUiState, ManageSpaceUiEffect>.unconfirmedClean(
		progress: (SizesUiState) -> SizesUiState,
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
			postSideEffect(ManageSpaceUiEffect.ShowToast(ex.toString()))
		}
		manager.killProcessesAroundManageSpaceActivity()
		loadSizes(force = true)
	}

	companion object {
		private val LOG = LoggerFactory.getLogger(ManageSpaceViewModel::class.java)
	}
}
