package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
	homeEffectHandler: HomeEffectHandler,
	getWeatherActionHandler: GetWeatherActionHandler,
	getTempActionHandler: GetTempActionHandler
) : BaseViewModel<HomeAction, HomeState, HomeEffect, HomeReducer>(
	homeEffectHandler,
	HomeState.Initial
) {

	override val actionHandlers = mapOf(
		LoadWeatherButtonClicked::class to getWeatherActionHandler,
		LoadTempButtonClicked::class to getTempActionHandler
	)
}

internal sealed interface HomeAction

internal data class HomeState(
	val isLoading: Boolean = false,
	val weatherStatus: String = "Unknown",
	val tempStatus: String = "Unknown"
) {
	companion object {
		val Initial = HomeState()
	}
}

internal sealed class HomeEffect {
	data class ShowToast(
		val message: String
	) : HomeEffect()
}

internal sealed class HomeReducer constructor(
	reducer: Reducer<HomeState>
) : Reducer<HomeState> by reducer {

	object OnLoading : HomeReducer({
		it.copy(
			isLoading = true
		)
	})
}

internal class HomeEffectHandler @Inject constructor(
	/*@ApplicationContext*/ private val context: Context
) : EffectHandler<HomeEffect>() {

	override suspend fun handleEffect(effect: HomeEffect) {
		when (effect) {
			is HomeEffect.ShowToast -> {
				Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
			}
		}
	}
}
