package net.twisterrob.inventory.android.activity.space

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.twisterrob.inventory.android.activity.space.HomeReducer.OnLoading
import javax.inject.Inject

data class LoadTempButtonClicked(
	val city: String
) : HomeAction

internal class GetTempActionHandler @Inject constructor(
	private val getTempUseCase: GetTemperatureUseCase,
	private val tempStateMapper: TempDomainToStateMapper
) : ActionHandler<LoadTempButtonClicked, HomeState, HomeEffect, HomeReducer> {

	override fun handle(
		action: LoadTempButtonClicked,
		state: HomeState,
		effect: suspend (HomeEffect) -> Unit
	): Flow<HomeReducer> = flow {
		emit(OnLoading)
		val tempState = tempStateMapper.map(getTempUseCase.execute(action.city))
		emit(OnTempLoaded(tempState))
	}
}

class GetTemperatureUseCase @Inject constructor(
) : UseCase<String, TemperatureStatusDomain> {
	override suspend fun execute(input: String): TemperatureStatusDomain = TODO()
}

data class TemperatureStatusDomain(
	val city: String,
	val status: Int,
	val unit: String
)

internal class TempDomainToStateMapper @Inject constructor(
) : Mapper<TemperatureStatusDomain, TemperatureState> {
	override fun map(left: TemperatureStatusDomain): TemperatureState =
		TemperatureState(
			result = "Temp in ${left.city} is ${left.status} ${left.unit}"
		)
}

internal data class TemperatureState(
	internal val result: String
)

internal data class OnTempLoaded(
	private val state: TemperatureState
) : HomeReducer({
	it.copy(
		isLoading = false,
		tempStatus = state.result
	)
})
