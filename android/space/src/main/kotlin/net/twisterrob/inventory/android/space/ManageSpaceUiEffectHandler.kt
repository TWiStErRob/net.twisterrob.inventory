package net.twisterrob.inventory.android.space

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultCaller
import dagger.hilt.android.qualifiers.ActivityContext
import net.twisterrob.android.contracts.CreateOpenableDocument
import net.twisterrob.android.contracts.OpenOpenableDocument
import net.twisterrob.android.utils.tools.DialogTools
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks
import net.twisterrob.inventory.android.arch.UiEffectHandler
import javax.inject.Inject

internal class ManageSpaceUiEffectHandler @Inject constructor(
	caller: ActivityResultCaller,
	private val viewModel: ManageSpaceViewModel,
	@ActivityContext
	private val context: Context,
) : UiEffectHandler<ManageSpaceUiEffect> {

	private val dumpAll = caller.registerForActivityResult<String, Uri>(
		CreateOpenableDocument(MIME_TYPE_ZIP)
	) { result ->
		result?.let { viewModel.dumpAllData(it) }
	}
	private val dumpDB = caller.registerForActivityResult<String, Uri>(
		CreateOpenableDocument(MIME_TYPE_SQLITE)
	) { result ->
		result?.let { viewModel.dumpDatabase(it) }
	}
	private val restoreDB = caller.registerForActivityResult<Array<String>, Uri>(
		OpenOpenableDocument()
	) { result ->
		result?.let { viewModel.restoreDatabase(it) }
	}

	override suspend fun launch(effect: ManageSpaceUiEffect) {
		when (effect) {
			is ManageSpaceUiEffect.ShowToast -> {
				Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
			}

			is ManageSpaceUiEffect.PickDumpDatabaseTarget -> {
				dumpDB.launch(effect.fileName)
			}

			is ManageSpaceUiEffect.PickRestoreDatabaseSource -> {
				restoreDB.launch(arrayOf(MIME_TYPE_SQLITE))
			}

			is ManageSpaceUiEffect.PickDumpAllDataTarget -> {
				dumpAll.launch(effect.fileName)
			}

			is ManageSpaceUiEffect.PickVacuumDatabaseIncrementalBytes -> {
				DialogTools
					.pickNumber(context, 10, 0, Int.MAX_VALUE, object : PopupCallbacks<Int> {
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
		}
	}

	companion object {
		/**
		 * See [IANA](https://www.iana.org/assignments/media-types/application/vnd.sqlite3).
		 */
		private const val MIME_TYPE_SQLITE: String = "application/vnd.sqlite3"
		private const val MIME_TYPE_ZIP: String = "application/zip"
	}
}
