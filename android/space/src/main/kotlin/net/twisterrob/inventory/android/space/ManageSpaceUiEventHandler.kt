package net.twisterrob.inventory.android.space

import net.twisterrob.inventory.android.arch.UiEventHandler
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import javax.inject.Inject

internal class ManageSpaceUiEventHandler @Inject constructor(
	private val binding: ManageSpaceActivityBinding,
	private val viewModel: ManageSpaceViewModel,
) : UiEventHandler {
	override fun wireEvents() {
		binding.refresher.setOnRefreshListener(viewModel::loadSizes)
		binding.contents.storageSearchClear.setOnClickListener { viewModel.rebuildSearch() }
		binding.contents.storageImageCacheClear.setOnClickListener { viewModel.clearImageCache() }
		binding.contents.storageDbClear.setOnClickListener { viewModel.emptyDatabase() }
		binding.contents.storageDbDump.setOnClickListener { viewModel.dumpDatabase() }
		binding.contents.storageImagesClear.setOnClickListener { viewModel.clearImages() }
		binding.contents.storageDbTest.setOnClickListener { viewModel.resetTestData() }
		binding.contents.storageDbRestore.setOnClickListener { viewModel.restoreDatabase() }
		binding.contents.storageDbVacuum.setOnClickListener { viewModel.vacuumDatabase() }
		binding.contents.storageDbVacuumIncremental.setOnClickListener { viewModel.vacuumDatabaseIncremental() }
		binding.contents.storageAllClear.setOnClickListener { viewModel.clearData() }
		binding.contents.storageAllDump.setOnClickListener { viewModel.dumpAllData() }
	}
}
