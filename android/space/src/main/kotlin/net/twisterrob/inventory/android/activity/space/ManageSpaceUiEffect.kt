package net.twisterrob.inventory.android.activity.space

internal sealed class ManageSpaceUiEffect {
	data class ShowToast(
		val message: CharSequence
	) : ManageSpaceUiEffect()
}
