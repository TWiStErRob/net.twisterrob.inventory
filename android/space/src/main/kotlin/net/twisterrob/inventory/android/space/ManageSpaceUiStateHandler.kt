package net.twisterrob.inventory.android.space

import net.twisterrob.android.utils.tools.DialogTools
import net.twisterrob.inventory.android.arch.UiStateHandler
import net.twisterrob.inventory.android.space.ManageSpaceUiState.ConfirmationUiState
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import javax.inject.Inject

internal class ManageSpaceUiStateHandler @Inject constructor(
	private val binding: ManageSpaceActivityBinding,
	private val viewModel: ManageSpaceViewModel,
) : UiStateHandler<ManageSpaceUiState> {
	override fun render(state: ManageSpaceUiState) {
		binding.refresher.isRefreshing = state.isLoading
		// Note: isVisible is set from XML based on debug/release builds for each button/section.
		binding.contents.storageImageCacheActions.isEnabled = state.isEnableButtons
		binding.contents.storageDbActions.isEnabled = state.isEnableButtons
		binding.contents.storageDataActions.isEnabled = state.isEnableButtons
		binding.contents.storageIndexActions.isEnabled = state.isEnableButtons
		binding.contents.storageImageCacheSize.text = state.sizes?.imageCache
		binding.contents.storageDbSize.text = state.sizes?.database
		binding.contents.storageDbFreelistSize.text = state.sizes?.freelist
		binding.contents.storageSearchSize.text = state.sizes?.searchIndex
		binding.contents.storageAllSize.text = state.sizes?.allData
		if (state.confirmation != null) {
			showDialog(state.confirmation)
		}
	}

	private fun showDialog(confirmation: ConfirmationUiState) {
		DialogTools
			.confirm(binding.root.context) { result: Boolean? ->
				if (result == true) {
					viewModel.actionConfirmed()
				} else {
					viewModel.actionCancelled()
				}
			}
			.setTitle(confirmation.title)
			.setMessage(confirmation.message)
			.show()
	}
}
