package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.text.format.Formatter
import android.widget.Toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.twisterrob.android.utils.tools.DatabaseTools
import net.twisterrob.android.utils.tools.IOTools
import net.twisterrob.android.utils.tools.TextTools
import net.twisterrob.inventory.android.Constants.Pic.GlideSetup
import net.twisterrob.inventory.android.components.ErrorMapper
import net.twisterrob.inventory.android.content.Database
import net.twisterrob.inventory.android.space.R
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class ManageSpaceViewModel @Inject constructor(
	private val useCase: GetSizesUseCase,
	private val mapper: SizesDomainToStateMapper,
) : BaseViewModel<ManageSpaceState, ManageSpaceEffect>(
	initialState = ManageSpaceState(
		isLoading = false,
		sizes = null,
	)
) {
	fun loadSizes() {
		intent {
			reduce {
				state.copy(
					isLoading = true,
					sizes = mapper.loading(),
				)
			}
			val input = useCase.execute(Unit)
			val sizes = mapper.map(input)
			if (sizes.errors != null) {
				postSideEffect(ManageSpaceEffect.ShowToast(sizes.errors))
			}
			reduce {
				state.copy(
					isLoading = false,
					sizes = sizes,
				)
			}
		}
	}
}

internal data class ManageSpaceState(
	val isLoading: Boolean,
	val sizes: SizesState?,
)

internal sealed class ManageSpaceEffect {
	data class ShowToast(
		val message: CharSequence
	) : ManageSpaceEffect()
}

internal class ManageSpaceEffectHandler @Inject constructor(
	@ActivityContext private val context: Context
) : EffectHandler<ManageSpaceEffect> {

	override suspend fun handleEffect(effect: ManageSpaceEffect) {
		when (effect) {
			is ManageSpaceEffect.ShowToast -> {
				Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
			}
		}
	}
}

class GetSizesUseCase @Inject constructor(
	@ApplicationContext private val context: Context,
) : UseCase<Unit, SizesDomain> {

	override suspend fun execute(input: Unit): SizesDomain = withContext(Dispatchers.IO) {
		delay(1000)
		val database = Database.get(context)
		SizesDomain(
			imageCache = safe {
				dirSizes(
					GlideSetup.getCacheDir(context)
				)
			},
			database = safe {
				dirSizes(
					context.getDatabasePath(database.helper.databaseName)
				)
			},
			freelist = safe {
				database.readableDatabase.let { it.freelistCount * it.pageSize }
			},
			searchIndex = safe {
				database.searchSize
			},
			allData = safe {
				dirSizes(
					File(context.applicationInfo.dataDir),
					context.externalCacheDir,
					context.getExternalFilesDir(null),
				)
			},
		)
	}

	private fun dirSizes(vararg dirs: File?): Long =
		dirs.sumOf { if (Math.random() < 0.3) error("WHops") else IOTools.calculateSize(it) }

	private val SQLiteDatabase.freelistCount: Long
		get() = DatabaseUtils.longForQuery(
			this,
			"PRAGMA freelist_count;",
			DatabaseTools.NO_ARGS
		)

	private fun safe(block: () -> Long): Result<Long> =
		try {
			Result.success(block())
		} catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
			LOG.error("Cannot get size", ex)
			Result.failure(ex)
		}

	companion object {
		private val LOG = LoggerFactory.getLogger(GetSizesUseCase::class.java)
	}
}

data class SizesDomain(
	val imageCache: Result<Long>,
	val database: Result<Long>,
	val freelist: Result<Long>,
	val searchIndex: Result<Long>,
	val allData: Result<Long>,
)

internal class SizesDomainToStateMapper @Inject constructor(
	@ApplicationContext private val context: Context,
	private val errorMapper: ErrorMapper,
) : Mapper<SizesDomain, SizesState> {
	override fun map(input: SizesDomain): SizesState =
		SizesState(
			imageCache = input.imageCache.formatFileSize(),
			database = input.database.formatFileSize(),
			freelist = input.freelist.formatFileSize(),
			searchIndex = input.searchIndex.formatFileSize(),
			allData = input.allData.formatFileSize(),
			errors = formatError(
				listOf(
					input.imageCache,
					input.database,
					input.freelist,
					input.searchIndex,
					input.allData,
				)
			)
		)

	// TODEL make it vararg, workaround for https://youtrack.jetbrains.com/issue/KT-33565
	private fun formatError(results: List<Result<Long>>): CharSequence? =
		results
			.mapNotNull { it.exceptionOrNull() }
			.map { errorMapper.getError(it, "Cannot get size") }
			.takeIf { it.isNotEmpty() }
			?.let { TextTools.join("\n", it) }

	private fun Result<Long>.formatFileSize(): CharSequence =
		this.map { Formatter.formatFileSize(context, it) }.getOrElse { "?" }

	fun loading(): SizesState {
		val calculating = context.getString(R.string.manage_space_calculating)
		return SizesState(
			imageCache = calculating,
			database = calculating,
			freelist = calculating,
			searchIndex = calculating,
			allData = calculating,
			errors = null,
		)
	}
}

internal data class SizesState(
	val imageCache: CharSequence,
	val database: CharSequence,
	val freelist: CharSequence,
	val searchIndex: CharSequence,
	val allData: CharSequence,
	val errors: CharSequence?,
)
