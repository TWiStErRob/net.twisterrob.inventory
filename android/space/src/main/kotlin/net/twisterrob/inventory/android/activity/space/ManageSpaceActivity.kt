package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import net.twisterrob.android.utils.tools.DialogTools
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks
import net.twisterrob.android.utils.tools.ViewTools
import net.twisterrob.inventory.android.BaseComponent
import net.twisterrob.inventory.android.Constants.Paths.getFileName
import net.twisterrob.inventory.android.activity.BaseActivity
import net.twisterrob.inventory.android.content.CreateOpenableDocument
import net.twisterrob.inventory.android.content.OpenOpenableDocument
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import net.twisterrob.inventory.android.view.RecyclerViewController
import org.orbitmvi.orbit.viewmodel.observe
import org.slf4j.LoggerFactory
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ManageSpaceActivity : BaseActivity() {
	private lateinit var binding: ManageSpaceActivityBinding
	private lateinit var inject: BaseComponent
	private val viewModel: ManageSpaceViewModel by viewModels()
	@Inject internal lateinit var effectHandler: ManageSpaceEffectHandler

	private val dumpAll = registerForActivityResult<String, Uri>(
		CreateOpenableDocument(MIME_TYPE_ZIP)
	) { result ->
		result?.let { viewModel.dumpAllData(inject, it) }
	}

	private val dumpDB = registerForActivityResult<String, Uri>(
		CreateOpenableDocument(MIME_TYPE_SQLITE)
	) { result ->
		result?.let { viewModel.dumpDatabase(inject, it) }
	}

	private val restoreDB = registerForActivityResult<Array<String>, Uri>(
		OpenOpenableDocument()
	) { result ->
		result?.let { viewModel.restoreDatabase(inject, it) }
	}

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
		binding.contents.storageSearchClear.setOnClickListener { viewModel.rebuildSearch(inject) }
		binding.contents.storageImageCacheClear.setOnClickListener {
			viewModel.clearImageCache(inject)
		}
		binding.contents.storageDbClear.setOnClickListener { viewModel.emptyDatabase(inject) }
		binding.contents.storageDbDump.setOnClickListener {
			dumpDB.launch(getFileName("Inventory", Calendar.getInstance(), "sqlite"))
		}
		binding.contents.storageImagesClear.setOnClickListener { viewModel.clearImages(inject) }
		binding.contents.storageDbTest.setOnClickListener { viewModel.resetTestData(inject) }
		binding.contents.storageDbRestore.setOnClickListener {
			restoreDB.launch(arrayOf(MIME_TYPE_SQLITE))
		}
		binding.contents.storageDbVacuum.setOnClickListener { viewModel.vacuumDatabase(inject) }
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
		binding.contents.storageAllClear.setOnClickListener { viewModel.clearData(inject) }
		binding.contents.storageAllDump.setOnClickListener {
			dumpAll.launch(getFileName("Inventory_dump", Calendar.getInstance(), "zip"))
		}
		ViewTools.displayedIf(binding.contents.storageAll, inject.buildInfo().isDebug)
		viewModel.observe(this, state = ::updateUi, sideEffect = effectHandler::handleEffect)
	}

	private fun updateUi(state: ManageSpaceState) {
		LOG.trace("Updating UI with state {}", state)
		binding.refresher.isRefreshing = state.isLoading
		binding.contents.storageImageCacheActions.isEnabled = !state.isLoading
		binding.contents.storageDbActions.isEnabled = !state.isLoading
		binding.contents.storageDataActions.isEnabled = !state.isLoading
		binding.contents.storageIndexActions.isEnabled = !state.isLoading
		binding.contents.storageImageCacheSize.text = state.sizes?.imageCache
		binding.contents.storageDbSize.text = state.sizes?.database
		binding.contents.storageDbFreelistSize.text = state.sizes?.freelist
		binding.contents.storageSearchSize.text = state.sizes?.searchIndex
		binding.contents.storageAllSize.text = state.sizes?.allData
		state.confirmation?.run { binding.dialog.show(title, message) }
	}

	override fun onResume() {
		super.onResume()
		viewModel.screenVisible()
	}

	companion object {
		internal val LOG = LoggerFactory.getLogger(ManageSpaceActivity::class.java)

		/**
		 * See [IANA](https://www.iana.org/assignments/media-types/application/vnd.sqlite3).
		 */
		private const val MIME_TYPE_SQLITE: String = "application/vnd.sqlite3"
		private const val MIME_TYPE_ZIP: String = "application/zip"

		@JvmStatic
		fun launch(context: Context): Intent =
			Intent(context, ManageSpaceActivity::class.java)
	}
}
