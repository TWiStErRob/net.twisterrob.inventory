package net.twisterrob.inventory.android.space

import android.net.Uri
import kotlinx.coroutines.test.runTest
import net.twisterrob.android.test.clear
import net.twisterrob.inventory.android.space.ManageSpaceUiState.ConfirmationUiState
import net.twisterrob.inventory.android.space.ManageSpaceUiState.SizesUiState
import net.twisterrob.inventory.android.space.manager.InventorySpaceManager
import net.twisterrob.inventory.android.space.sizes.GetSizesUseCase
import net.twisterrob.inventory.android.space.sizes.SizesDomain
import net.twisterrob.inventory.android.space.sizes.SizesDomainToStateMapper
import net.twisterrob.java.utils.ReflectionTools
import net.twisterrob.test.TestRuntimeException
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.orbitmvi.orbit.test.OrbitTestContext
import org.orbitmvi.orbit.test.test

/**
 * @see ManageSpaceViewModel
 */
class ManageSpaceViewModelTest {
	private val mockUseCase: GetSizesUseCase = mock()
	private val mockMapper: SizesDomainToStateMapper = mock()
	private val mockManager: InventorySpaceManager = mock()

	@Test
	fun `load sizes - success`() = runTest {
		ManageSpaceViewModel(mockUseCase, mockMapper, mockManager).test(this) {
			expectInitialState()
			val (loading, model) = mockReload()
			containerHost.loadSizes()
			expectReload(loading, model)
		}
	}

	@Test
	fun `load sizes - failure`() = runTest {
		ManageSpaceViewModel(mockUseCase, mockMapper, mockManager).test(this) {
			expectInitialState()
			val (loading, model) = mockReload()
			ReflectionTools.set(model, "errors", "test error")
			containerHost.loadSizes()
			expectState { copy(isLoading = true, sizes = loading) }
			expectSideEffect(ManageSpaceUiEffect.ShowToast("test error"))
			expectState { copy(isLoading = false, sizes = model) }
		}
	}

	@Test
	fun `restoreDatabase flow - success`() = runTest {
		ManageSpaceViewModel(mockUseCase, mockMapper, mockManager).test(this) {
			expectInitialState()
			containerHost.restoreDatabase()
			expectSideEffect(ManageSpaceUiEffect.PickRestoreDatabaseSource)
			val (loading, model) = mockReload()
			val empty = mockEmpty()
			val mockUri: Uri = mock()
			containerHost.restoreDatabase(mockUri)
			expectState {
				copy(
					isLoading = true,
					confirmation = null,
					sizes = empty.copy(database = "Restoring…"),
				)
			}
			inOrder(mockManager) {
				verify(mockManager).killProcessesAroundManageSpaceActivity()
				verify(mockManager).restoreDatabase(mockUri)
				verify(mockManager).killProcessesAroundManageSpaceActivity()
				verifyNoMoreInteractions()
			}
			expectReload(loading, model)
		}
	}

	@Test
	fun `restoreDatabase flow - failure`() = runTest {
		ManageSpaceViewModel(mockUseCase, mockMapper, mockManager).test(this) {
			expectInitialState()
			containerHost.restoreDatabase()
			expectSideEffect(ManageSpaceUiEffect.PickRestoreDatabaseSource)
			val (loading, model) = mockReload()
			val empty = mockEmpty()
			val mockUri: Uri = mock()
			whenever(mockManager.restoreDatabase(mockUri)).thenThrow(TestRuntimeException())
			containerHost.restoreDatabase(mockUri)
			expectState {
				copy(
					isLoading = true,
					confirmation = null,
					sizes = empty.copy(database = "Restoring…"),
				)
			}
			inOrder(mockManager) {
				verify(mockManager).killProcessesAroundManageSpaceActivity()
				verify(mockManager).restoreDatabase(mockUri)
				verifyNoMoreInteractions()
			}
			expectSideEffect(ManageSpaceUiEffect.ShowToast("net.twisterrob.test.TestRuntimeException: test"))
			expectReload(loading, model)
		}
	}

	@Test
	fun `clearImageCache flow - clear`() = runTest {
		ManageSpaceViewModel(mockUseCase, mockMapper, mockManager).test(this) {
			expectInitialState()
			containerHost.clearImageCache()
			expectState {
				copy(
					confirmation = ConfirmationUiState(
						"Clear Image Cache",
						"You're about to remove all files in the image cache. " +
							"There will be no permanent loss. The cache will be re-filled as required in the future.",
					),
				)
			}
			containerHost.clear()
			verifyZeroInteractions(mockManager)
		}
	}

	@Test
	fun `clearImageCache flow - cancel`() = runTest {
		ManageSpaceViewModel(mockUseCase, mockMapper, mockManager).test(this) {
			expectInitialState()
			containerHost.clearImageCache()
			expectState {
				copy(
					confirmation = ConfirmationUiState(
						"Clear Image Cache",
						"You're about to remove all files in the image cache. " +
							"There will be no permanent loss. The cache will be re-filled as required in the future.",
					),
				)
			}
			containerHost.actionCancelled()
			expectState { copy(confirmation = null) }
			verifyZeroInteractions(mockManager)
		}
	}

	@Test
	fun `clearImageCache flow - confirm`() = runTest {
		ManageSpaceViewModel(mockUseCase, mockMapper, mockManager).test(this) {
			expectInitialState()
			containerHost.clearImageCache()
			expectState {
				copy(
					confirmation = ConfirmationUiState(
						"Clear Image Cache",
						"You're about to remove all files in the image cache. " +
							"There will be no permanent loss. The cache will be re-filled as required in the future.",
					),
				)
			}
			val empty = mockEmpty()
			val (loading, model) = mockReload()
			containerHost.actionConfirmed()
			expectState {
				copy(
					isLoading = true,
					confirmation = null,
					sizes = empty.copy(imageCache = "Clearing… empty imageCache"),
				)
			}
			inOrder(mockManager) {
				verify(mockManager).killProcessesAroundManageSpaceActivity()
				verify(mockManager).clearImageCache()
				verify(mockManager).killProcessesAroundManageSpaceActivity()
				verifyNoMoreInteractions()
			}
			expectReload(loading, model)
		}
	}

	context(OrbitTestContext<ManageSpaceUiState, *, *>)
		private suspend fun expectReload(loading: SizesUiState, model: SizesUiState) {
		expectState { copy(isLoading = true, sizes = loading) }
		expectState { copy(isLoading = false, sizes = model) }
	}

	private fun mockEmpty(): SizesUiState {
		val empty = SizesUiState(
			imageCache = "empty imageCache",
			database = "empty database",
			freelist = "empty freelist",
			searchIndex = "empty searchIndex",
			allData = "empty allData",
			errors = null
		)
		whenever(mockMapper.empty()).thenReturn(empty)
		return empty
	}

	private suspend fun mockReload(): Pair<SizesUiState, SizesUiState> {
		val size: Result<Long> = Result.success(0)
		val domain = SizesDomain(size, size, size, size, size)
		whenever(mockUseCase.execute(Unit)).thenReturn(domain)

		val loading = SizesUiState(
			imageCache = "loading imageCache",
			database = "loading database",
			freelist = "loading freelist",
			searchIndex = "loading searchIndex",
			allData = "loading allData",
			errors = null
		)
		whenever(mockMapper.loading()).thenReturn(loading)

		val model = SizesUiState(
			imageCache = "size of imageCache",
			database = "size of database",
			freelist = "size of freelist",
			searchIndex = "size of searchIndex",
			allData = "size of allData",
			errors = null
		)
		whenever(mockMapper.map(domain)).thenReturn(model)
		return Pair(loading, model)
	}
}
