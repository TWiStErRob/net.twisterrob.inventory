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
import net.twisterrob.android.viewbinding.viewBindingInflate
import net.twisterrob.inventory.android.activity.BaseActivity
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import net.twisterrob.inventory.android.view.RecyclerViewController
import org.orbitmvi.orbit.viewmodel.observe
import javax.inject.Inject

@AndroidEntryPoint
class ManageSpaceActivity : BaseActivity() {
	private val binding: ManageSpaceActivityBinding by viewBindingInflate()
	private val viewModel: ManageSpaceViewModel by viewModels()
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
		setupUI()
		binding.setupEventHandling(viewModel)
		viewModel.observe(
			lifecycleOwner = this,
			state = stateHandler::updateUi,
			sideEffect = effectHandler::handleEffect
		)
	}

	private fun setupUI() {
		setIcon(ContextCompat.getDrawable(this, applicationInfo.icon))
		supportActionBar.setDisplayHomeAsUpEnabled(false)
		RecyclerViewController.initializeProgress(binding.refresher)
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

private fun ManageSpaceActivityBinding.setupEventHandling(viewModel: ManageSpaceViewModel) {
	this.refresher.setOnRefreshListener(viewModel::loadSizes)
	this.contents.storageSearchClear.setOnClickListener { viewModel.rebuildSearch() }
	this.contents.storageImageCacheClear.setOnClickListener { viewModel.clearImageCache() }
	this.contents.storageDbClear.setOnClickListener { viewModel.emptyDatabase() }
	this.contents.storageDbDump.setOnClickListener { viewModel.dumpDatabase() }
	this.contents.storageImagesClear.setOnClickListener { viewModel.clearImages() }
	this.contents.storageDbTest.setOnClickListener { viewModel.resetTestData() }
	this.contents.storageDbRestore.setOnClickListener { viewModel.restoreDatabase() }
	this.contents.storageDbVacuum.setOnClickListener { viewModel.vacuumDatabase() }
	this.contents.storageDbVacuumIncremental.setOnClickListener { viewModel.vacuumDatabaseIncremental() }
	this.contents.storageAllClear.setOnClickListener { viewModel.clearData() }
	this.contents.storageAllDump.setOnClickListener { viewModel.dumpAllData() }
}
