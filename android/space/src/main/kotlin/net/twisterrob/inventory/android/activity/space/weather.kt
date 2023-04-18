package net.twisterrob.inventory.android.activity.space

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.twisterrob.inventory.android.activity.space.HomeReducer.OnLoading
import javax.inject.Inject

internal data class LoadWeatherButtonClicked(
	val city: String
) : HomeAction

internal class GetWeatherActionHandler @Inject constructor(
	private val getWeatherUseCase: GetWeatherUseCase,
	private val weatherStateMapper: WeatherDomainToStateMapper
) : ActionHandler<LoadWeatherButtonClicked, HomeState, HomeEffect, HomeReducer> {

	override fun handle(
		action: LoadWeatherButtonClicked,
		state: HomeState,
		effect: suspend (HomeEffect) -> Unit
	): Flow<HomeReducer> = flow {
		emit(OnLoading)
		val weatherState = weatherStateMapper.map(getWeatherUseCase.execute(action.city))
		emit(OnWeatherLoaded(weatherState))
	}
}

class GetWeatherUseCase @Inject constructor(
) : UseCase<String, WeatherStatusDomain> {
	override suspend fun execute(input: String): WeatherStatusDomain = TODO()
}

data class WeatherStatusDomain(
	val city: String,
	val status: String
)

internal class WeatherDomainToStateMapper @Inject constructor(
) : Mapper<WeatherStatusDomain, WeatherState> {
	override fun map(left: WeatherStatusDomain): WeatherState =
		WeatherState(
			result = "Weather in ${left.city} is ${left.status}"
		)
}

internal data class WeatherState(
	val result: String
)

internal data class OnWeatherLoaded(
	private val state: WeatherState
) : HomeReducer({
	it.copy(
		isLoading = false,
		weatherStatus = state.result
	)
})
