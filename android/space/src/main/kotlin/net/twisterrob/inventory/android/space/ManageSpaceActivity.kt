package net.twisterrob.inventory.android.space

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import net.twisterrob.android.utils.tools.DialogTools
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks
import net.twisterrob.android.utils.tools.ViewTools
import net.twisterrob.inventory.android.BaseComponent
import net.twisterrob.inventory.android.activity.BaseActivity
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import net.twisterrob.inventory.android.viewmodel.viewBindingInflate
import org.orbitmvi.orbit.viewmodel.observe
import javax.inject.Inject

@AndroidEntryPoint
class ManageSpaceActivity : BaseActivity() {
	private val binding: ManageSpaceActivityBinding by viewBindingInflate()
	private val viewModel: ManageSpaceViewModel by viewModels()
	@Inject internal lateinit var inject: BaseComponent
	@Inject internal lateinit var effectHandler: ManageSpaceUiEffectHandler
	@Inject internal lateinit var stateHandler: ManageSpaceUiStateHandler

	@InstallIn(ActivityComponent::class)
	@Module
	internal class HiltModule {
		@Provides
		fun provideTypedActivity(activity: Activity): ManageSpaceActivity =
			(activity as ManageSpaceActivity)

		@Provides
		fun provideBinding(activity: ManageSpaceActivity): ManageSpaceActivityBinding =
			activity.binding

		@Provides
		fun provideActivityResultCaller(activity: ManageSpaceActivity): ActivityResultCaller =
			activity

		@Provides
		fun provideViewModel(activity: ManageSpaceActivity): ManageSpaceViewModel =
			activity.viewModel
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setIcon(ContextCompat.getDrawable(this, applicationInfo.icon))
		supportActionBar.setDisplayHomeAsUpEnabled(false)

		binding.refresher.setOnRefreshListener(viewModel::loadSizes)
		binding.contents.storageSearchClear.setOnClickListener { viewModel.rebuildSearch() }
		binding.contents.storageImageCacheClear.setOnClickListener { viewModel.clearImageCache() }
		binding.contents.storageDbClear.setOnClickListener { viewModel.emptyDatabase() }
		binding.contents.storageDbDump.setOnClickListener { viewModel.dumpDatabase() }
		binding.contents.storageImagesClear.setOnClickListener { viewModel.clearImages() }
		binding.contents.storageDbTest.setOnClickListener { viewModel.resetTestData() }
		binding.contents.storageDbRestore.setOnClickListener { viewModel.restoreDatabase() }
		binding.contents.storageDbVacuum.setOnClickListener { viewModel.vacuumDatabase() }
		binding.contents.storageDbVacuumIncremental.setOnClickListener {
			DialogTools
				.pickNumber(this, 10, 0, Int.MAX_VALUE, object : PopupCallbacks<Int> {
					override fun finished(value: Int?) {
						if (value == null) {
							return
						}
						@Suppress("MagicNumber")
						val vacuumBytes = value * 1024 * 1024
						viewModel.vacuumDatabaseIncremental(vacuumBytes)
					}
				})
				.setTitle("Incremental Vacuum")
				.setMessage("How many megabytes do you want to vacuum?")
				.show()
		}
		binding.contents.storageAllClear.setOnClickListener { viewModel.clearData() }
		binding.contents.storageAllDump.setOnClickListener { viewModel.dumpAllData() }
		ViewTools.displayedIf(binding.contents.storageAll, inject.buildInfo().isDebug)
		viewModel.observe(
			lifecycleOwner = this,
			state = stateHandler::updateUi,
			sideEffect = effectHandler::handleEffect
		)
	}

	override fun onResume() {
		super.onResume()
		viewModel.screenVisible()
	}

	companion object {
		@JvmStatic
		fun launch(context: Context): Intent =
			Intent(context, ManageSpaceActivity::class.java)
	}
}
