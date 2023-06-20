package net.twisterrob.inventory.android.space

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import net.twisterrob.inventory.android.arch.UiEventHandler
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import javax.inject.Inject

internal class ManageSpaceUiEventHandler @Inject constructor(
	private val lifecycle: Lifecycle,
	private val binding: ManageSpaceActivityBinding,
	private val viewModel: ManageSpaceViewModel,
) : UiEventHandler {
	override fun wireEvents() {
		lifecycle.addObserver(object : DefaultLifecycleObserver {
			override fun onResume(owner: LifecycleOwner) {
				viewModel.screenVisible()
			}
		})
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
