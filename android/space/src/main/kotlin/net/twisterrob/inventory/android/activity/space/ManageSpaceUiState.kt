package net.twisterrob.inventory.android.activity.space

internal data class ManageSpaceUiState(
	val isLoading: Boolean,
	val sizes: SizesUiState?,
	val confirmation: ConfirmationUiState?,
) {

	internal data class SizesUiState(
		val imageCache: CharSequence,
		val database: CharSequence,
		val freelist: CharSequence,
		val searchIndex: CharSequence,
		val allData: CharSequence,
		val errors: CharSequence?,
	)

	internal data class ConfirmationUiState(
		val title: CharSequence,
		val message: CharSequence,
	)
}
