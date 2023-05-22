package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import net.twisterrob.android.utils.tools.DialogTools
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks
import net.twisterrob.android.utils.tools.ViewTools
import net.twisterrob.inventory.android.BaseComponent
import net.twisterrob.inventory.android.Constants.Paths
import net.twisterrob.inventory.android.activity.BaseActivity
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import net.twisterrob.inventory.android.view.RecyclerViewController
import org.orbitmvi.orbit.viewmodel.observe
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ManageSpaceActivity : BaseActivity(), TaskEndListener {
	private lateinit var binding: ManageSpaceActivityBinding
	private lateinit var inject: BaseComponent
	private val viewModel: ManageSpaceViewModel by viewModels()
	@Inject internal lateinit var effectHandler: ManageSpaceEffectHandler

	@Suppress("LongMethod")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		inject = BaseComponent.get(applicationContext)

		binding = setContentView(ManageSpaceActivityBinding::inflate)
		setIcon(ContextCompat.getDrawable(this, applicationInfo.icon))
		supportActionBar.setDisplayHomeAsUpEnabled(false)
		RecyclerViewController.initializeProgress(binding.refresher)

		binding.refresher.setOnRefreshListener(viewModel::loadSizes)
		binding.dialog.setPositiveButtonListener(viewModel::actionConfirmed)
		binding.dialog.setNegativeButtonListener(viewModel::actionCancelled)
		binding.dialog.setCancelListener(viewModel::actionCancelled)
		binding.contents.storageSearchClear.setOnClickListener {
			viewModel.rebuildSearch(this)
		}
		binding.contents.storageImageCacheClear.setOnClickListener { 
			viewModel.clearImageCache(Glide.get(applicationContext))
		}
		binding.contents.storageDbClear.setOnClickListener {
			viewModel.emptyDatabase(inject)
		}
		binding.contents.storageDbDump.setOnClickListener {
			viewModel.dumpDatabase(inject)
		}
		binding.contents.storageImagesClear.setOnClickListener {
			viewModel.clearImages(inject)
		}
		binding.contents.storageDbTest.setOnClickListener {
			viewModel.resetTestData(inject)
		}
		binding.contents.storageDbRestore.setOnClickListener { v ->
			val dumpFile = File(Paths.getPhoneHome(), "db.sqlite")
			val defaultPath: String = dumpFile.absolutePath
			DialogTools
				.prompt(v.context, defaultPath, object : PopupCallbacks<String> {
					override fun finished(value: String?) {
						if (value == null) {
							return
						}
						viewModel.restoreDatabase(inject, File(value))
					}
				})
				.setTitle("Restore DB")
				.setMessage("Please enter the absolute path of the .sqlite file to restore!")
				.show()
		}
		binding.contents.storageDbVacuum.setOnClickListener {
			viewModel.vacuumDatabase(inject)
		}
		binding.contents.storageDbVacuumIncremental.setOnClickListener {
			DialogTools
				.pickNumber(this, 10, 0, Int.MAX_VALUE, object : PopupCallbacks<Int> {
					override fun finished(value: Int?) {
						if (value == null) {
							return
						}
						@Suppress("MagicNumber")
						val vacuumBytes = value * 1024 * 1024
						viewModel.vacuumDatabaseIncremental(inject, vacuumBytes)
					}
				})
				.setTitle("Incremental Vacuum")
				.setMessage("How many megabytes do you want to vacuum?")
				.show()
		}
		binding.contents.storageAllClear.setOnClickListener {
			viewModel.clearData(inject)
		}
		binding.contents.storageAllDump.setOnClickListener {
			viewModel.dumpAllData(inject)
		}
		ViewTools.displayedIf(binding.contents.storageAll, inject.buildInfo().isDebug)
		viewModel.observe(this, state = ::updateUi, sideEffect = effectHandler::handleEffect)
	}

	private fun updateUi(state: ManageSpaceState) {
		LOG.trace("Updating UI with state {}", state)
		binding.refresher.isRefreshing = state.isLoading
		binding.contents.storageImageCacheSize.text = state.sizes?.imageCache
		binding.contents.storageDbSize.text = state.sizes?.database
		binding.contents.storageDbFreelistSize.text = state.sizes?.freelist
		binding.contents.storageSearchSize.text = state.sizes?.searchIndex
		binding.contents.storageAllSize.text = state.sizes?.allData
		state.confirmation?.run { binding.dialog.show(title, message) }
	}

	override fun taskDone() {
		viewModel.loadSizes()
	}

	override fun onResume() {
		super.onResume()
		viewModel.screenVisible()
	}

	companion object {
		internal val LOG = LoggerFactory.getLogger(ManageSpaceActivity::class.java)

		@JvmStatic
		fun launch(context: Context): Intent =
			Intent(context, ManageSpaceActivity::class.java)
	}
}
