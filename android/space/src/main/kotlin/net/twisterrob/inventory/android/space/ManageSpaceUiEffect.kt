package net.twisterrob.inventory.android.space

internal sealed class ManageSpaceUiEffect {
	data class ShowToast(
		val message: CharSequence
	) : ManageSpaceUiEffect()
}
