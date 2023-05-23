package net.twisterrob.inventory.android.space

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultCaller
import dagger.hilt.android.qualifiers.ActivityContext
import net.twisterrob.inventory.android.content.CreateOpenableDocument
import net.twisterrob.inventory.android.content.OpenOpenableDocument
import net.twisterrob.inventory.android.viewmodel.EffectHandler
import javax.inject.Inject

internal class ManageSpaceUiEffectHandler @Inject constructor(
	caller: ActivityResultCaller,
	private val viewModel: ManageSpaceViewModel,
	@ActivityContext
	private val context: Context,
) : EffectHandler<ManageSpaceUiEffect> {

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

	override suspend fun handleEffect(effect: ManageSpaceUiEffect) {
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
