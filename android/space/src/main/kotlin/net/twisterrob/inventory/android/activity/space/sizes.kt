package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.text.format.Formatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import net.twisterrob.inventory.android.activity.space.ManageSpaceReducer.OnLoading
import javax.inject.Inject

object LoadSizes : ManageSpaceAction

internal class GetSizesActionHandler @Inject constructor(
	private val useCase: GetSizesUseCase,
	private val mapper: SizesDomainToStateMapper
) : ManageSpaceHandler<LoadSizes> {

	override fun handle(
		action: LoadSizes,
		state: ManageSpaceState,
		effect: suspend (ManageSpaceEffect) -> Unit
	): Flow<ManageSpaceReducer> = flow {
		emit(OnLoading)
		val newState = mapper.map(useCase.execute(Unit))
		emit(OnSizesLoaded(newState))
	}
}

class GetSizesUseCase @Inject constructor(
) : UseCase<Unit, SizesDomain> {
	override suspend fun execute(input: Unit): SizesDomain = withContext(Dispatchers.IO) {
		SizesDomain(
			imageCache = 1,
			database = 2,
			freelist = 3,
			searchIndex = 4,
			allData = 5,
		)
	}
}

data class SizesDomain(
	val imageCache: Long,
	val database: Long,
	val freelist: Long,
	val searchIndex: Long,
	val allData: Long,
)

internal class SizesDomainToStateMapper @Inject constructor(
	@ApplicationContext private val context: Context,
) : Mapper<SizesDomain, SizesState> {
	override fun map(input: SizesDomain): SizesState =
		SizesState(
			imageCache = input.imageCache.formatFileSize(),
			database = input.database.formatFileSize(),
			freelist = input.freelist.formatFileSize(),
			searchIndex = input.searchIndex.formatFileSize(),
			allData = input.allData.formatFileSize(),
		)

	private fun Long.formatFileSize(): String {
		return Formatter.formatFileSize(context, this)
	}
}

internal data class SizesState(
	val imageCache: String,
	val database: String,
	val freelist: String,
	val searchIndex: String,
	val allData: String,
)

internal data class OnSizesLoaded(
	private val state: SizesState
) : ManageSpaceReducer({
	it.copy(
		isLoading = false,
		sizes = state,
	)
})
