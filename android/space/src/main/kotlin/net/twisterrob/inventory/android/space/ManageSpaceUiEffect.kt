package net.twisterrob.inventory.android.space

internal sealed class ManageSpaceUiEffect {
	data class ShowToast(
		val message: CharSequence
	) : ManageSpaceUiEffect()

	object PickRestoreDatabaseSource : ManageSpaceUiEffect()
	data class PickDumpDatabaseTarget(
		val fileName: String,
	) : ManageSpaceUiEffect()

	data class PickDumpAllDataTarget(
		val fileName: String,
	) : ManageSpaceUiEffect()
}
