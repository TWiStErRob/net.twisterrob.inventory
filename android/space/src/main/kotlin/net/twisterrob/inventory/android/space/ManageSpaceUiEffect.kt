package net.twisterrob.inventory.android.space

import net.twisterrob.inventory.android.arch.UiEffect

internal sealed class ManageSpaceUiEffect : UiEffect {
	data class ShowToast(
		val message: CharSequence
	) : ManageSpaceUiEffect()

	data object PickRestoreDatabaseSource
		: ManageSpaceUiEffect()

	data class PickDumpDatabaseTarget(
		val fileName: String,
	) : ManageSpaceUiEffect()

	data class PickDumpAllDataTarget(
		val fileName: String,
	) : ManageSpaceUiEffect()

	data object PickVacuumDatabaseIncrementalBytes
		: ManageSpaceUiEffect()
}
