package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.text.format.Formatter
import android.widget.Toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
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
					sizes = null,
				)
			}
			val sizes = mapper.map(useCase.execute(Unit))
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
		val message: String
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
) : UseCase<Unit, SizesDomain> {
	override suspend fun execute(input: Unit): SizesDomain = withContext(Dispatchers.IO) {
		delay(1000)
		SizesDomain(
			imageCache = (Math.random() * 1000).toLong(),
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

	private fun Long.formatFileSize(): String =
		Formatter.formatFileSize(context, this)
}

internal data class SizesState(
	val imageCache: String,
	val database: String,
	val freelist: String,
	val searchIndex: String,
	val allData: String,
)
