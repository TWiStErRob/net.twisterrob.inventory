package net.twisterrob.inventory.android.space.sizes

import android.content.Context
import android.text.format.Formatter
import dagger.hilt.android.qualifiers.ApplicationContext
import net.twisterrob.android.utils.tools.TextTools
import net.twisterrob.inventory.android.components.ErrorMapper
import net.twisterrob.inventory.android.space.ManageSpaceUiState.SizesUiState
import net.twisterrob.inventory.android.space.R
import net.twisterrob.inventory.android.viewmodel.Mapper
import javax.inject.Inject

internal class SizesDomainToStateMapper @Inject constructor(
	@ApplicationContext private val context: Context,
	private val errorMapper: ErrorMapper,
) : Mapper<SizesDomain, SizesUiState> {
	override fun map(input: SizesDomain): SizesUiState =
		SizesUiState(
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

	fun loading(): SizesUiState {
		val calculating = context.getString(R.string.manage_space_calculating)
		return SizesUiState(
			imageCache = calculating,
			database = calculating,
			freelist = calculating,
			searchIndex = calculating,
			allData = calculating,
			errors = null,
		)
	}
}
