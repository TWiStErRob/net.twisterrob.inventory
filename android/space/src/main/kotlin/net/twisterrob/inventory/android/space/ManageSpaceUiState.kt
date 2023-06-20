package net.twisterrob.inventory.android.space

import net.twisterrob.inventory.android.arch.UiState

internal data class ManageSpaceUiState(
	val isLoading: Boolean,
	val sizes: SizesUiState?,
	val confirmation: ConfirmationUiState?,
) : UiState {

	data class SizesUiState(
		val imageCache: CharSequence,
		val database: CharSequence,
		val freelist: CharSequence,
		val searchIndex: CharSequence,
		val allData: CharSequence,
		val errors: CharSequence?,
	)

	data class ConfirmationUiState(
		val title: CharSequence,
		val message: CharSequence,
	)
}

internal val ManageSpaceUiState.isModalShowing: Boolean
	get() = confirmation != null

internal val ManageSpaceUiState.isEnableButtons: Boolean
	get() = !isLoading
