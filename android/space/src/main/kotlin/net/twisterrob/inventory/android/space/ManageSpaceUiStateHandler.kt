package net.twisterrob.inventory.android.space

import net.twisterrob.android.utils.tools.DialogTools
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import net.twisterrob.inventory.android.view.RecyclerViewController
import javax.inject.Inject

internal class ManageSpaceUiStateHandler @Inject constructor(
	private val binding: ManageSpaceActivityBinding,
	private val viewModel: ManageSpaceViewModel,
) {
	init {
		RecyclerViewController.initializeProgress(binding.refresher)
	}

	fun updateUi(state: ManageSpaceUiState) {
		binding.refresher.isRefreshing = state.isLoading
		binding.contents.storageImageCacheActions.isEnabled = !state.isLoading
		binding.contents.storageDbActions.isEnabled = !state.isLoading
		binding.contents.storageDataActions.isEnabled = !state.isLoading
		binding.contents.storageIndexActions.isEnabled = !state.isLoading
		binding.contents.storageImageCacheSize.text = state.sizes?.imageCache
		binding.contents.storageDbSize.text = state.sizes?.database
		binding.contents.storageDbFreelistSize.text = state.sizes?.freelist
		binding.contents.storageSearchSize.text = state.sizes?.searchIndex
		binding.contents.storageAllSize.text = state.sizes?.allData
		if (state.confirmation != null) {
			DialogTools
				.confirm(binding.root.context) { result: Boolean? ->
					if (result == true) {
						viewModel.actionConfirmed()
					} else {
						viewModel.actionCancelled()
					}
				}
				.setTitle(state.confirmation.title)
				.setMessage(state.confirmation.message)
				.show()
		}
	}
}
